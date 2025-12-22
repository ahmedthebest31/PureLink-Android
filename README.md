# >_ PureLink Android 🛡️

**The Swiss Army Knife for Privacy & Developers.**

PureLink is a powerful, lightweight, and accessibility-first Android utility designed to strip tracking parameters from URLs, expand shortened links, and provide essential tools for developers. Wrapped in a high-contrast **Terminal/Hacker style** interface.

---

## 🚀 Key Features

### 🛡️ Privacy & URL Sanitization
* **Deep Cleaning:** Instantly removes tracking tags like `utm_`, `fbclid`, `gclid`, `si`, and `ref` from shared links.
* **Smart Unshortener:** Safely expands shortened URLs (e.g., `bit.ly`, `is.gd`) in the background to reveal the true destination before you visit.
* **Bot-Proof Resolver:** Uses advanced User-Agent spoofing to bypass server-side bot protection when resolving links.

### 👁️ Background Monitor
* **Clipboard Guard:** Runs silently in the background to detect and clean copied links automatically.
* **Quick Settings Tile:** Toggle the monitoring service instantly from your notification shade (Quick Settings) without opening the app.
* **Haptic Feedback:** Vibrates to confirm when a link has been cleaned or processed.

### 🛠️ Developer & Social Tools
* **WhatsApp Direct:** Copy any phone number -> Open chat immediately without saving the contact.
* **Telegram Direct:** Open usernames directly.
* **Dev Utilities:** Built-in Base64 Encoder/Decoder and UUID Generator.

### ⚡ System Integration
* **Share Target:** Share links directly from other apps (YouTube, Chrome, Twitter) to PureLink for instant cleaning.
* **Process Text:** Select any text system-wide -> Click the 3 dots -> Choose "PureLink Clean".

### ♿ Accessibility First
* Designed by a blind developer for maximum efficiency with Screen Readers (TalkBack).
* High-contrast UI (Green on Black) for low-vision users and battery saving on AMOLED screens.

---

## 📥 Download Android App

[![Get it on IzzyOnDroid](fastlane/badge/izzyondroid.png)](https://apt.izzysoft.de/fdroid/index/apk/com.ahmedsamy.purelink)
[![Get it on GitHub](fastlane/badge/github.png)](https://github.com/ahmedthebest31/PureLink-Android/releases)

> **Note:** This app is fully native, lightweight (~3MB), and requires no unnecessary permissions.

---

## 💻 PureLink for PC (Windows/Linux)

Looking for this power on your desktop
Check out **PureLink Desktop**, a high-performance system tool written in **Go (Golang)**.

* **👻 Silent & Invisible:** Runs quietly in the **System Tray** (just like a screen reader). No terminal or command prompt required.
* **⚡ Ultra Fast:** Optimized for zero resource usage. You won't feel it running.
* **⚙️ Easy Config:** Manage settings directly from the tray icon.

[**👉 Download PureLink for PC**](https://github.com/ahmedthebest31/PureLink/releases)

---

### 🤝 Contributing
Contributions are welcome! Please note that this version utilizes `syscall` for Windows native features.

### 📄 License
MIT License

