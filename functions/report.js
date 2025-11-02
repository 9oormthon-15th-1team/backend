const functions = require('firebase-functions');
const admin = require('firebase-admin');
const express = require('express');

if (!admin.apps.length) {
  // Safe-guard: initialize only if not already initialized by index.js
  admin.initializeApp();
}

const router = express.Router();
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const db = getFirestore();
const bucket = admin.storage().bucket();
const serverTS = () => FieldValue.serverTimestamp();

// ===== Firestore helpers =====
const col = (name) => db.collection(name);

async function createDoc(collection, data) {
  const ref = col(collection).doc();
  const payload = { id: ref.id, createdAt: serverTS(), updatedAt: serverTS(), ...data };
  await ref.set(payload, { merge: true });
  const snap = await ref.get();
  return { id: ref.id, ...snap.data() };
}

async function getDoc(collection, id) {
  const snap = await col(collection).doc(id).get();
  return snap.exists ? snap.data() : null;
}

async function listDocs(collection, { limit = 20 } = {}) {
  const snap = await col(collection)
    .orderBy('createdAt', 'desc')
    .limit(Math.min(Number(limit) || 20, 100))
    .get();
  return snap.docs.map((d) => d.data());
}

async function updateDoc(collection, id, data) {
  const ref = col(collection).doc(id);
  await ref.set({ ...data, updatedAt: serverTS() }, { merge: true });
  const snap = await ref.get();
  return snap.data();
}

async function deleteDoc(collection, id) {
  await col(collection).doc(id).delete();
}

// ===== Helpers: Firebase Storage (replaces AWS S3) =====
/**
 * Returns { uploadUrl, readUrl, path } for a future upload.
 * - uploadUrl: signed URL with 'write' action (expires in ~15 min by default)
 * - readUrl: signed URL with 'read' action (1h validity) to preview after upload
 * - path: gs:// path
 */
async function generateUploadUrls(objectPath, contentType = 'application/octet-stream') {
  const file = bucket.file(objectPath);
  const [uploadUrl] = await file.getSignedUrl({
    action: 'write',
    expires: Date.now() + 15 * 60 * 1000, // 15 minutes
    contentType
  });
  const [readUrl] = await file.getSignedUrl({
    action: 'read',
    expires: Date.now() + 60 * 60 * 1000 // 1 hour
  });
  return { uploadUrl, readUrl, path: `gs://${bucket.name}/${objectPath}` };
}

// ===== Express routes (HTTP) =====
// NOTE: index.js should mount as: app.use('/api', report.api)

// [POST] /api/reports
router.post('/reports', async (req, res) => {
  try {
    const { potholeId, description = '', status = 'OPEN', imageUrls = [] } = req.body || {};
    if (!potholeId || typeof potholeId !== 'string') {
      return res.status(400).json({ message: 'potholeId required' });
    }
    const created = await createDoc('reports', {
      potholeId,
      description,
      status,
      imageUrls: Array.isArray(imageUrls) ? imageUrls : []
    });
    res.status(201).json(created);
  } catch (e) {
    console.error(e);
    res.status(500).json({ message: 'Internal Server Error' });
  }
});

// [GET] /api/reports
router.get('/reports', async (req, res) => {
  try {
    const limit = Number(req.query.limit || 20);
    const items = await listDocs('reports', { limit });
    res.json(items);
  } catch (e) {
    console.error(e);
    res.status(500).json({ message: 'Internal Server Error' });
  }
});

// [GET] /api/reports/:id
router.get('/reports/:id', async (req, res) => {
  try {
    const item = await getDoc('reports', req.params.id);
    if (!item) return res.sendStatus(404);
    res.json(item);
  } catch (e) {
    console.error(e);
    res.status(500).json({ message: 'Internal Server Error' });
  }
});

// [PATCH] /api/reports/:id
router.patch('/reports/:id', async (req, res) => {
  try {
    const updated = await updateDoc('reports', req.params.id, req.body || {});
    res.json(updated);
  } catch (e) {
    console.error(e);
    res.status(500).json({ message: 'Internal Server Error' });
  }
});

// [DELETE] /api/reports/:id
router.delete('/reports/:id', async (req, res) => {
  try {
    await deleteDoc('reports', req.params.id);
    res.sendStatus(204);
  } catch (e) {
    console.error(e);
    res.status(500).json({ message: 'Internal Server Error' });
  }
});

// [POST] /api/reports/:id/images/upload-url
// Body: { filename, contentType }
// Returns signed URLs to upload to Firebase Storage (no AWS S3)
router.post('/reports/:id/images/upload-url', async (req, res) => {
  try {
    const { filename, contentType = 'application/octet-stream' } = req.body || {};
    if (!filename || typeof filename !== 'string') {
      return res.status(400).json({ message: 'filename required' });
    }
    const reportId = req.params.id;
    const objectPath = `reports/${reportId}/${Date.now()}_${filename}`;
    const urls = await generateUploadUrls(objectPath, contentType);
    res.status(201).json(urls);
  } catch (e) {
    console.error(e);
    res.status(500).json({ message: 'Internal Server Error' });
  }
});

// ===== Callable Functions =====
const createReport = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Must be authenticated');
  }
  const { potholeId, description = '', status = 'OPEN', imageUrls = [] } = data || {};
  if (!potholeId || typeof potholeId !== 'string') {
    throw new functions.https.HttpsError('invalid-argument', 'potholeId required');
  }
  const created = await createDoc('reports', {
    potholeId,
    description,
    status,
    imageUrls: Array.isArray(imageUrls) ? imageUrls : []
  });
  return created;
});

const listReports = functions.https.onCall(async (data) => {
  const limit = Number((data && data.limit) || 20);
  const items = await listDocs('reports', { limit });
  return items;
});

const getReport = functions.https.onCall(async (data) => {
  if (!data || !data.id) {
    throw new functions.https.HttpsError('invalid-argument', 'id required');
  }
  const found = await getDoc('reports', String(data.id));
  if (!found) {
    throw new functions.https.HttpsError('not-found', 'report not found');
  }
  return found;
});

const updateReport = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Must be authenticated');
  }
  if (!data || !data.id) {
    throw new functions.https.HttpsError('invalid-argument', 'id required');
  }
  const id = String(data.id);
  const patch = { ...data };
  delete patch.id;
  const updated = await updateDoc('reports', id, patch);
  return updated;
});

const deleteReport = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Must be authenticated');
  }
  if (!data || !data.id) {
    throw new functions.https.HttpsError('invalid-argument', 'id required');
  }
  await deleteDoc('reports', String(data.id));
  return { ok: true };
});

// Request signed upload URL for a report image (Callable)
// data: { reportId, filename, contentType }
const createReportImageUploadUrl = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Must be authenticated');
  }
  const { reportId, filename, contentType = 'application/octet-stream' } = data || {};
  if (!reportId || !filename) {
    throw new functions.https.HttpsError('invalid-argument', 'reportId and filename required');
  }
  const objectPath = `reports/${reportId}/${Date.now()}_${filename}`;
  const urls = await generateUploadUrls(objectPath, contentType);
  return urls;
});

module.exports = {
  api: router,
  createReport,
  listReports,
  getReport,
  updateReport,
  deleteReport,
  createReportImageUploadUrl
};
