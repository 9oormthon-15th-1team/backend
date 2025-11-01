

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const express = require('express');

if (!admin.apps.length) {
  // Safe-guard: initialize only if not already initialized by index.js
  admin.initializeApp();
}

const router = express.Router();
const db = admin.firestore();
const serverTS = admin.firestore.FieldValue.serverTimestamp;

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

// ===== Express routes (HTTP) =====
// NOTE: index.js does `app.use('/api', pothole.api)` so these mount under `/api`
router.post('/potholes', async (req, res) => {
  try {
    const { title = '', description = '', latitude, longitude, imageUrls = [] } = req.body || {};
    if (typeof title !== 'string' || typeof description !== 'string') {
      return res.status(400).json({ message: 'title/description must be strings' });
    }
    if (!Number.isFinite(Number(latitude)) || !Number.isFinite(Number(longitude))) {
      return res.status(400).json({ message: 'latitude/longitude must be numbers' });
    }
    const created = await createDoc('potholes', {
      title,
      description,
      latitude: Number(latitude),
      longitude: Number(longitude),
      imageUrls: Array.isArray(imageUrls) ? imageUrls : []
    });
    res.status(201).json(created);
  } catch (e) {
    console.error(e);
    res.status(500).json({ message: 'Internal Server Error' });
  }
});

router.get('/potholes', async (req, res) => {
  try {
    const limit = Number(req.query.limit || 20);
    const items = await listDocs('potholes', { limit });
    res.json(items);
  } catch (e) {
    console.error(e);
    res.status(500).json({ message: 'Internal Server Error' });
  }
});

router.get('/potholes/:id', async (req, res) => {
  try {
    const item = await getDoc('potholes', req.params.id);
    if (!item) return res.sendStatus(404);
    res.json(item);
  } catch (e) {
    console.error(e);
    res.status(500).json({ message: 'Internal Server Error' });
  }
});

router.patch('/potholes/:id', async (req, res) => {
  try {
    const updated = await updateDoc('potholes', req.params.id, req.body || {});
    res.json(updated);
  } catch (e) {
    console.error(e);
    res.status(500).json({ message: 'Internal Server Error' });
  }
});

router.delete('/potholes/:id', async (req, res) => {
  try {
    await deleteDoc('potholes', req.params.id);
    res.sendStatus(204);
  } catch (e) {
    console.error(e);
    res.status(500).json({ message: 'Internal Server Error' });
  }
});

// ===== Callable Functions =====
const createPothole = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Must be authenticated');
  }
  const { title = '', description = '', latitude, longitude, imageUrls = [] } = data || {};
  if (typeof title !== 'string' || typeof description !== 'string') {
    throw new functions.https.HttpsError('invalid-argument', 'title/description must be strings');
  }
  if (!Number.isFinite(Number(latitude)) || !Number.isFinite(Number(longitude))) {
    throw new functions.https.HttpsError('invalid-argument', 'latitude/longitude must be numbers');
  }
  const created = await createDoc('potholes', {
    title,
    description,
    latitude: Number(latitude),
    longitude: Number(longitude),
    imageUrls: Array.isArray(imageUrls) ? imageUrls : []
  });
  return created;
});

const listPotholes = functions.https.onCall(async (data) => {
  const limit = Number((data && data.limit) || 20);
  const items = await listDocs('potholes', { limit });
  return items;
});

const getPothole = functions.https.onCall(async (data) => {
  if (!data || !data.id) {
    throw new functions.https.HttpsError('invalid-argument', 'id required');
  }
  const found = await getDoc('potholes', String(data.id));
  if (!found) {
    throw new functions.https.HttpsError('not-found', 'pothole not found');
  }
  return found;
});

const updatePothole = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Must be authenticated');
  }
  if (!data || !data.id) {
    throw new functions.https.HttpsError('invalid-argument', 'id required');
  }
  const id = String(data.id);
  const patch = { ...data };
  delete patch.id;
  const updated = await updateDoc('potholes', id, patch);
  return updated;
});

const deletePothole = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Must be authenticated');
  }
  if (!data || !data.id) {
    throw new functions.https.HttpsError('invalid-argument', 'id required');
  }
  await deleteDoc('potholes', String(data.id));
  return { ok: true };
});

module.exports = {
  api: router,
  createPothole,
  listPotholes,
  getPothole,
  updatePothole,
  deletePothole
};