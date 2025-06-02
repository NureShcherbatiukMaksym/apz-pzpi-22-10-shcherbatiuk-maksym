// src/firebase.js
// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries
import { getAuth, GoogleAuthProvider } from 'firebase/auth';
// Your web app's Firebase configuration
const firebaseConfig = {
    apiKey: "AIzaSyCtSG7Zc-uK7B3RFquGfog7OG_K2tk770I",
    authDomain: "soilscout-e77b9.firebaseapp.com",
    projectId: "soilscout-e77b9",
    storageBucket: "soilscout-e77b9.firebasestorage.app",
    messagingSenderId: "444351979020",
    appId: "1:444351979020:web:fb02003c13c84fd37b5829"
};

const app = initializeApp(firebaseConfig);

const auth = getAuth(app);

const googleProvider = new GoogleAuthProvider();

export { auth, googleProvider };
