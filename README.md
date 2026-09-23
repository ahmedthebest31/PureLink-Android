# >_ PureLink Android 🛡️

**The Ultimate URL Sanitizer (Swiss Army Knife for Privacy).**

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

### 📜 History & Smart Rules
* **Local History:** Secure, local history log (last 10 links) with instant "Copy" and "Open" actions.
* **Dynamic Rules Engine:** The app silently fetches updated tracking filters from GitHub every week via `WorkManager`.

### ⚡ System Integration
* **Invisible Share Target:** Sharing a link to PureLink triggers a transparent activity that cleans, copies, and closes instantly without disrupting your flow.
* **In-Place Text Cleaning:** Select any text or URL system-wide → Click the context menu (3 dots) → Choose "Pure Link". It instantly cleans and replaces the link right where you are typing!
* **Cloud Boost:** Converts Dropbox share links to direct `?dl=1` download links, and Google Drive `view` links to direct `uc?export=download` links.

### 🎨 UI & Accessibility
* **3-Tab Bottom Navigation:** Switch seamlessly between Dashboard (stats & history), Tools (Base64, UUID, chat), and Settings.
* **7 Themes 🎭:** Matrix Green, Amber CRT, Dracula, Monokai, Light, High Contrast, and System (follows device dark/light mode).
* **Smart Commands ⌨️:** Type `/wa`, `/tg`, `/b64e`, `/b64d`, `/uuid`, `/upper`, `/lower`, `/capitalize`, `/reverse`, `/clear`, or `/trim` followed by text for instant processing.
* **Ignore List:** Exclude specific domains from cleaning or URL processing.
* **YouTube Shorts Resolver:** Automatically converts `youtube.com/shorts/xxx` links to standard `watch?v=` format.
* **Adaptive Icons:** Modern Android 13+ adaptive launcher icons with proper foreground/background layers.
* **Full Localization:** Seamlessly switch between English and Arabic interfaces on the fly with full RTL support.
* **Jetpack Compose:** The entire UI is built with Jetpack Compose, resulting in a smoother, more responsive, and lighter interface.
* **Accessibility First ♿:** Designed by a blind developer. Enhanced TalkBack support with labeled touch targets and semantic descriptions for all interactive elements. WCAG AA contrast-compliant.

### 🛠️ Developer & Social Tools
* **WhatsApp Direct:** Copy any phone number → Open chat immediately without saving the contact.
* **Telegram Direct:** Open usernames directly.
* **Base64 & UUID:** Built-in Base64 Encoder/Decoder and one-click UUID Generator.

---

## 🛠️ Tech Stack

*   **Kotlin** 100%
*   **Jetpack Compose** (UI)
*   **Coroutines** (Concurrency)
*   **WorkManager** (Background Tasks)

---

## 📥 Download

<a href="https://play.google.com/store/apps/details?id=com.ahmedsamy.purelink">
  <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play" height="60">
</a>
&nbsp;&nbsp;
<a href="https://apt.izzysoft.de/fdroid/index/apk/com.ahmedsamy.purelink">
  <img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" alt="Get it on IzzyOnDroid" height="60">
</a>

Or get the latest APK directly from the [Releases page](https://github.com/ahmedthebest31/PureLink-Android/releases).

> **Note:** This app is fully native, lightweight (~2MB), respects your privacy, and requires no unnecessary permissions.

---

## 💻 PureLink for PC (Windows / macOS / Linux)

Looking for this power on your desktop?
Check out **PureLink Desktop**, a high-performance system tool written in **Go (Golang)**, available for **Windows, macOS, and Linux**.

* **👻 Silent & Invisible:** Runs quietly in the **System Tray** (just like a screen reader). No terminal or command prompt required.
* **⚡ Ultra Fast:** Optimized for zero resource usage. You won't feel it running.
* **⚙️ Easy Config:** Manage settings directly from the tray icon.

[**👉 Download PureLink for PC**](https://github.com/ahmedthebest31/PureLink/releases)

---

### 🤝 Contributing
Contributions to this project are welcome! If you find a bug, have an idea for an improvement, or want to contribute in any other way, please feel free to open an issue or submit a pull request.

### 📄 License
[GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html) — see the [LICENSE](LICENSE) file for details.