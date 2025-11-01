const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Firebase Admin 초기화
admin.initializeApp();

// Firestore 참조
const db = admin.firestore();
// ===== REST (HTTP) API with Express) =====
const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');

const app = express();
app.use(cors()); // TODO: restrict origins for production
app.use(bodyParser.json());

const pothole = require('./pothole');
app.use('/api', pothole.api);
exports.createPothole = pothole.createPothole;
exports.listPotholes = pothole.listPotholes;
exports.getPothole = pothole.getPothole;
exports.updatePothole = pothole.updatePothole;
exports.deletePothole = pothole.deletePothole;

// (Optional) simple health check
app.get('/health', (_req, res) => res.json({ ok: true }));

// Export the Express app as an HTTPS function
exports.api = functions.https.onRequest(app);

// HTTP 트리거 함수 예제
exports.helloWorld = functions.https.onRequest((req, res) => {
  res.json({ message: 'Hello from Firebase!' });
});

// HTTP Callable 함수 예제
exports.addMessage = functions.https.onCall(async (data, context) => {
  // 인증 확인
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'The function must be called while authenticated.'
    );
  }

  const text = data.text;
  
  try {
    const writeResult = await db.collection('messages').add({
      text: text,
      userId: context.auth.uid,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    return { id: writeResult.id };
  } catch (error) {
    throw new functions.https.HttpsError('internal', error.message);
  }
});

// Firestore 트리거 함수 예제 (onCreate)
exports.onUserCreate = functions.firestore
  .document('users/{userId}')
  .onCreate(async (snap, context) => {
    const newValue = snap.data();
    const userId = context.params.userId;
    
    console.log(`New user created: ${userId}`, newValue);
    
    // 예: 환영 메시지 전송 또는 추가 데이터 생성
    return db.collection('welcomeMessages').add({
      userId: userId,
      message: `Welcome ${newValue.name}!`,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
  });

// Firestore 트리거 함수 예제 (onUpdate)
exports.onUserUpdate = functions.firestore
  .document('users/{userId}')
  .onUpdate(async (change, context) => {
    const beforeData = change.before.data();
    const afterData = change.after.data();
    const userId = context.params.userId;
    
    console.log(`User updated: ${userId}`);
    console.log('Before:', beforeData);
    console.log('After:', afterData);
    
    return null;
  });

// Firestore 트리거 함수 예제 (onDelete)
exports.onUserDelete = functions.firestore
  .document('users/{userId}')
  .onDelete(async (snap, context) => {
    const deletedValue = snap.data();
    const userId = context.params.userId;
    
    console.log(`User deleted: ${userId}`, deletedValue);
    
    // 관련 데이터 정리
    return db.collection('userProfiles').doc(userId).delete();
  });

// Authentication 트리거 함수 예제
exports.onAuthUserCreate = functions.auth.user().onCreate(async (user) => {
  console.log(`New authentication user created: ${user.uid}`);
  
  // Firestore에 사용자 프로필 생성
  return db.collection('users').doc(user.uid).set({
    email: user.email,
    displayName: user.displayName || 'Anonymous',
    photoURL: user.photoURL || null,
    createdAt: admin.firestore.FieldValue.serverTimestamp()
  });
});

exports.onAuthUserDelete = functions.auth.user().onDelete(async (user) => {
  console.log(`Authentication user deleted: ${user.uid}`);
  
  // Firestore에서 사용자 데이터 삭제
  return db.collection('users').doc(user.uid).delete();
});

// Scheduled 함수 예제 (Cron job)
exports.scheduledFunction = functions.pubsub
  .schedule('every 24 hours')
  .onRun(async (context) => {
    console.log('This will be run every 24 hours!');
    
    // 예: 오래된 데이터 정리
    const oldDate = new Date();
    oldDate.setDate(oldDate.getDate() - 30);
    
    const snapshot = await db.collection('logs')
      .where('createdAt', '<', oldDate)
      .get();
    
    const batch = db.batch();
    snapshot.docs.forEach((doc) => {
      batch.delete(doc.ref);
    });
    
    await batch.commit();
    console.log(`Deleted ${snapshot.size} old logs`);
    
    return null;
  });

// Storage 트리거 함수 예제
exports.onFileUpload = functions.storage.object().onFinalize(async (object) => {
  const filePath = object.name;
  const contentType = object.contentType;
  const bucket = admin.storage().bucket(object.bucket);
  
  console.log(`File uploaded: ${filePath}`);
  console.log(`Content Type: ${contentType}`);
  
  // 이미지 처리 예제
  if (contentType && contentType.startsWith('image/')) {
    // 썸네일 생성 등의 작업 수행
    console.log('Processing image...');
  }
  
  return null;
});

// PubSub 트리거 함수 예제
exports.processPubSubMessage = functions.pubsub
  .topic('my-topic')
  .onPublish(async (message) => {
    const data = message.json;
    console.log('PubSub message received:', data);
    
    // 메시지 처리 로직
    return null;
  });
