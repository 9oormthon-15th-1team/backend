const functions = require('firebase-functions');
const admin = require('firebase-admin');
const path = require('path');
const os = require('os');
const fs = require('fs');

// Storage 버킷 참조
const bucket = admin.storage().bucket();
const db = admin.firestore();

// ===== Storage 트리거 함수들 =====

// 파일 업로드 시 트리거
exports.onFileUpload = functions.storage.object().onFinalize(async (object) => {
  const filePath = object.name;
  const fileName = path.basename(filePath);
  const contentType = object.contentType;
  const metageneration = object.metageneration;

  console.log(`File uploaded: ${filePath}`);
  console.log(`Content Type: ${contentType}`);
  console.log(`File size: ${object.size} bytes`);

  // 메타데이터가 업데이트되면 무시 (무한루프 방지)
  if (metageneration > 1) {
    console.log('This is a metadata change event, not a new file upload.');
    return null;
  }

  try {
    // Firestore에 파일 정보 저장
    await db.collection('files').add({
      name: fileName,
      path: filePath,
      contentType: contentType,
      size: parseInt(object.size),
      bucket: object.bucket,
      timeCreated: object.timeCreated,
      uploadedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    // 이미지 파일인 경우 추가 처리
    if (contentType && contentType.startsWith('image/')) {
      console.log('Processing image file...');
      // 썸네일 생성 또는 이미지 최적화 등의 작업 수행
      // 예시: await generateThumbnail(filePath);
    }

    // 비디오 파일인 경우
    if (contentType && contentType.startsWith('video/')) {
      console.log('Processing video file...');
      // 비디오 트랜스코딩 등의 작업 수행
    }

    // 문서 파일인 경우
    if (contentType && (contentType.includes('pdf') || contentType.includes('document'))) {
      console.log('Processing document file...');
      // OCR 또는 텍스트 추출 등의 작업 수행
    }

    return null;
  } catch (error) {
    console.error('Error processing file upload:', error);
    throw error;
  }
});

// 파일 삭제 시 트리거
exports.onFileDelete = functions.storage.object().onDelete(async (object) => {
  const filePath = object.name;
  const fileName = path.basename(filePath);

  console.log(`File deleted: ${filePath}`);

  try {
    // Firestore에서 파일 정보 삭제
    const snapshot = await db.collection('files')
      .where('path', '==', filePath)
      .get();

    const batch = db.batch();
    snapshot.docs.forEach((doc) => {
      batch.delete(doc.ref);
    });

    await batch.commit();
    console.log(`Removed file record from Firestore: ${fileName}`);

    return null;
  } catch (error) {
    console.error('Error processing file deletion:', error);
    throw error;
  }
});

// ===== HTTP Callable 함수들 =====

// 파일 업로드 URL 생성
exports.getUploadUrl = functions.https.onCall(async (data, context) => {
  // 인증 확인
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'The function must be called while authenticated.'
    );
  }

  const { fileName, contentType } = data;

  if (!fileName) {
    throw new functions.https.HttpsError(
      'invalid-argument',
      'fileName is required'
    );
  }

  try {
    const userId = context.auth.uid;
    const fileExtension = path.extname(fileName);
    const timestamp = Date.now();
    const uniqueFileName = `${userId}/${timestamp}_${fileName}`;

    const file = bucket.file(uniqueFileName);

    // 서명된 업로드 URL 생성 (1시간 유효)
    const [url] = await file.getSignedUrl({
      version: 'v4',
      action: 'write',
      expires: Date.now() + 60 * 60 * 1000, // 1시간
      contentType: contentType
    });

    return {
      uploadUrl: url,
      filePath: uniqueFileName
    };
  } catch (error) {
    console.error('Error generating upload URL:', error);
    throw new functions.https.HttpsError('internal', error.message);
  }
});

// 파일 다운로드 URL 생성
exports.getDownloadUrl = functions.https.onCall(async (data, context) => {
  // 인증 확인
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'The function must be called while authenticated.'
    );
  }

  const { filePath } = data;

  if (!filePath) {
    throw new functions.https.HttpsError(
      'invalid-argument',
      'filePath is required'
    );
  }

  try {
    const file = bucket.file(filePath);

    // 파일 존재 확인
    const [exists] = await file.exists();
    if (!exists) {
      throw new functions.https.HttpsError(
        'not-found',
        'File not found'
      );
    }

    // 서명된 다운로드 URL 생성 (1시간 유효)
    const [url] = await file.getSignedUrl({
      version: 'v4',
      action: 'read',
      expires: Date.now() + 60 * 60 * 1000 // 1시간
    });

    return { downloadUrl: url };
  } catch (error) {
    console.error('Error generating download URL:', error);
    throw new functions.https.HttpsError('internal', error.message);
  }
});

// 파일 목록 조회
exports.listUserFiles = functions.https.onCall(async (data, context) => {
  // 인증 확인
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'The function must be called while authenticated.'
    );
  }

  try {
    const userId = context.auth.uid;
    const { limit = 10, orderBy = 'uploadedAt', order = 'desc' } = data;

    let query = db.collection('files')
      .where('path', '>=', `${userId}/`)
      .where('path', '<', `${userId}/\uf8ff`)
      .limit(limit);

    if (orderBy === 'uploadedAt') {
      query = query.orderBy('uploadedAt', order);
    } else if (orderBy === 'size') {
      query = query.orderBy('size', order);
    }

    const snapshot = await query.get();
    const files = snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data()
    }));

    return { files };
  } catch (error) {
    console.error('Error listing user files:', error);
    throw new functions.https.HttpsError('internal', error.message);
  }
});

// 파일 삭제
exports.deleteFile = functions.https.onCall(async (data, context) => {
  // 인증 확인
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'The function must be called while authenticated.'
    );
  }

  const { filePath } = data;

  if (!filePath) {
    throw new functions.https.HttpsError(
      'invalid-argument',
      'filePath is required'
    );
  }

  try {
    const userId = context.auth.uid;

    // 사용자가 소유한 파일인지 확인
    if (!filePath.startsWith(`${userId}/`)) {
      throw new functions.https.HttpsError(
        'permission-denied',
        'You can only delete your own files'
      );
    }

    const file = bucket.file(filePath);

    // 파일 존재 확인
    const [exists] = await file.exists();
    if (!exists) {
      throw new functions.https.HttpsError(
        'not-found',
        'File not found'
      );
    }

    // Storage에서 파일 삭제
    await file.delete();

    return { success: true, message: 'File deleted successfully' };
  } catch (error) {
    console.error('Error deleting file:', error);
    throw new functions.https.HttpsError('internal', error.message);
  }
});

// ===== HTTP REST API =====

const express = require('express');
const router = express.Router();

// 미들웨어: 인증 확인
const authenticateToken = async (req, res, next) => {
  try {
    const token = req.headers.authorization?.split('Bearer ')[1];
    if (!token) {
      return res.status(401).json({ error: 'No token provided' });
    }

    const decodedToken = await admin.auth().verifyIdToken(token);
    req.user = decodedToken;
    next();
  } catch (error) {
    return res.status(401).json({ error: 'Invalid token' });
  }
};

// GET /api/storage/files - 파일 목록 조회
router.get('/files', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.uid;
    const { limit = 10, orderBy = 'uploadedAt', order = 'desc' } = req.query;

    let query = db.collection('files')
      .where('path', '>=', `${userId}/`)
      .where('path', '<', `${userId}/\uf8ff`)
      .limit(parseInt(limit));

    if (orderBy === 'uploadedAt') {
      query = query.orderBy('uploadedAt', order);
    } else if (orderBy === 'size') {
      query = query.orderBy('size', order);
    }

    const snapshot = await query.get();
    const files = snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data()
    }));

    res.json({ files });
  } catch (error) {
    console.error('Error listing files:', error);
    res.status(500).json({ error: error.message });
  }
});

// POST /api/storage/upload-url - 업로드 URL 생성
router.post('/upload-url', authenticateToken, async (req, res) => {
  try {
    const { fileName, contentType } = req.body;

    if (!fileName) {
      return res.status(400).json({ error: 'fileName is required' });
    }

    const userId = req.user.uid;
    const timestamp = Date.now();
    const uniqueFileName = `${userId}/${timestamp}_${fileName}`;

    const file = bucket.file(uniqueFileName);

    // 서명된 업로드 URL 생성 (1시간 유효)
    const [url] = await file.getSignedUrl({
      version: 'v4',
      action: 'write',
      expires: Date.now() + 60 * 60 * 1000,
      contentType: contentType
    });

    res.json({
      uploadUrl: url,
      filePath: uniqueFileName
    });
  } catch (error) {
    console.error('Error generating upload URL:', error);
    res.status(500).json({ error: error.message });
  }
});

// POST /api/storage/download-url - 다운로드 URL 생성
router.post('/download-url', authenticateToken, async (req, res) => {
  try {
    const { filePath } = req.body;

    if (!filePath) {
      return res.status(400).json({ error: 'filePath is required' });
    }

    const file = bucket.file(filePath);

    // 파일 존재 확인
    const [exists] = await file.exists();
    if (!exists) {
      return res.status(404).json({ error: 'File not found' });
    }

    // 서명된 다운로드 URL 생성 (1시간 유효)
    const [url] = await file.getSignedUrl({
      version: 'v4',
      action: 'read',
      expires: Date.now() + 60 * 60 * 1000
    });

    res.json({ downloadUrl: url });
  } catch (error) {
    console.error('Error generating download URL:', error);
    res.status(500).json({ error: error.message });
  }
});

// DELETE /api/storage/files/:filePath - 파일 삭제
router.delete('/files/*', authenticateToken, async (req, res) => {
  try {
    const filePath = req.params[0]; // Express의 와일드카드 파라미터

    if (!filePath) {
      return res.status(400).json({ error: 'filePath is required' });
    }

    const userId = req.user.uid;

    // 사용자가 소유한 파일인지 확인
    if (!filePath.startsWith(`${userId}/`)) {
      return res.status(403).json({ error: 'You can only delete your own files' });
    }

    const file = bucket.file(filePath);

    // 파일 존재 확인
    const [exists] = await file.exists();
    if (!exists) {
      return res.status(404).json({ error: 'File not found' });
    }

    // Storage에서 파일 삭제
    await file.delete();

    res.json({ success: true, message: 'File deleted successfully' });
  } catch (error) {
    console.error('Error deleting file:', error);
    res.status(500).json({ error: error.message });
  }
});

exports.api = router;