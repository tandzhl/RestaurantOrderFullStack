// src/firebase.js
import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";
import { getAnalytics } from "firebase/analytics";

// 🔑 Config Firebase của bạn
const firebaseConfig = {
  apiKey: "AIzaSyCmp7oYVh6WYBElIPcgajYEjj4ctcXJtac",
  authDomain: "restaurant-firebase-db423.firebaseapp.com",
  projectId: "restaurant-firebase-db423",
  storageBucket: "restaurant-firebase-db423.firebasestorage.app",
  messagingSenderId: "623265947030",
  appId: "1:623265947030:web:5d8b80c339392cb291d1b7",
  measurementId: "G-QFGE0NZC84"
};

// 🚀 Khởi tạo Firebase
const app = initializeApp(firebaseConfig);

// 📊 Analytics
export const analytics = getAnalytics(app);

// 🔥 Firestore
export const db = getFirestore(app);
