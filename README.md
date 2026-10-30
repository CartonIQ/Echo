# 🎙️ Echo - Spy Mic & Audio Amplifier

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg?style=for-the-badge&logo=kotlin)
![Android](https://img.shields.io/badge/Android-API%2024%2B-green.svg?style=for-the-badge&logo=android)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-orange.svg?style=for-the-badge&logo=jetpack-compose)
![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)
![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline-success.svg?style=for-the-badge)

**Echo** is a premium, real-time audio amplifier and spy microphone app for Android. Turn your phone into a high-tech listening device, a personal hearing aid, or use it to listen to conversations from across the room. Built with a stunning "Holographic Audiophile" UI.

No accounts. No cloud. Just pure, real-time audio processing.

---

## 🚀 What is Echo?

Echo creates a **Live Audio Loop**. It captures the audio from your microphone (`AudioRecord`) and instantly plays it through your speaker or headphones (`AudioTrack`) with near-zero latency.
*   **As a Hearing Aid:** Amplify the TV or a conversation without buying expensive hardware.
*   **As a Spy Mic:** Put the phone in a room, connect Bluetooth headphones, and listen from another room.
*   **As a Studio Mic:** Use the DSP effects to filter out background noise and boost voice clarity.

---

## ✨ Core Features

*   🎙️ **Real-Time Audio Engine:** Ultra-low latency audio capture and playback using Coroutines and native Android Audio APIs.
*   🎛️ **Tactile Rotary Dials:** Custom-built Compose components to adjust "Mic Gain" and "Clarity Boost" with haptic feedback.
*   📊 **Holographic Audio Orb:** A real-time canvas visualizer that pulses and reacts to sound waves in the room.
*   🔒 **Foreground Service:** Keeps the microphone active even if you lock the screen or switch apps, with a persistent notification.
*   🚀 **Studio DSP (Rewarded Ad):** Watch a 30-second ad to unlock professional-grade noise cancellation and audio clarity for 1 hour.
*   🩺 **Medical Disclaimer:** Built-in safety warnings to comply with Google Play policies regarding hearing aid applications.

---

## ⚠️ IMPORTANT: How to Use (Avoid Feedback)

**You MUST use headphones or earbuds for the best experience.**
If you use the phone's main speaker while holding the phone, the microphone will hear the speaker, creating an infinite feedback loop (a loud screeching noise). Echo warns you about this. Use wired or Bluetooth headphones to hear crystal-clear amplified audio!

---

## 🛠️ Tech Stack & Architecture

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM + Clean Architecture
*   **Audio Engine:** `AudioRecord` (Mic) & `AudioTrack` (Speaker) running on `Dispatchers.IO` threads.
*   **Background Processing:** Android Foreground Service (`FOREGROUND_SERVICE_MICROPHONE`).
*   **UI Components:** Custom Compose Canvas for the Audio Orb and Rotary Dials.
*   **Monetization:** Google AdMob (Banner, Interstitial, Rewarded) & UMP for EU Consent.
*   **Min SDK:** 24 (Android 7.0) | **Target SDK:** 34 (Android 14)

---

## 🔐 Privacy Policy

Echo is built with privacy as the #1 priority:
1.  **100% Offline:** The app requires zero internet connection to amplify audio.
2.  **No Data Collection:** We do not record, save, or transmit any audio. All audio is processed in real-time inside your device's volatile RAM.
3.  **No Accounts:** There is no sign-up, no tracking, and no analytics.
4.  **Ads:** We use Google AdMob, which may collect your device's advertising ID to serve personalized ads.

---

## 🚀 Build By Mohd Arsh /CartonIQ 

## 👨‍💻 Developer

**Mohd Arsh (CartoniQ)**
*   GitHub: [@CartonIQ](https://github.com/CartonIQ)
*   Android App Developer | Kotlin | Java | Termux 🚀

## ⚖️ License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
