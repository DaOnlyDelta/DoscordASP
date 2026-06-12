# DoscordASP

DoscordASP is a full-featured, Discord-inspired real-time chat application for Android. Built natively using Java, it provides an intuitive interface for users to connect, form groups, and communicate effortlessly. The app leverages clean architecture principles, modern Android components, and a robust REST API backend.

## 📱 Features

**Authentication & Onboarding**
* **Multi-stage Registration:** Secure and engaging onboarding flow including birthday validation, display name processing, username creation, and profile picture setup.
* **Token-based Login:** Secure persistent sessions via API tokens.
* **Profile Management:** Set default or custom profile pictures directly through the app.

**Messaging & Chat Rooms**
* **Direct Messaging (DMs):** One-on-one private messaging functionality.
* **Group Chats:** Create groups, add friends, manage membership, and dynamically rename group chats.
* **Real-time Interaction:** View new and older messages seamlessly with a well-designed Chat UI featuring swipe actions and message splitters.
* **Message Management:** Edit or delete sent messages with contextual popup menus.
* **Voice Messages:** Built-in audio recording capabilities using the `RECORD_AUDIO` permission.

**Social & Connections**
* **Friends System:** Add friends by their username, handle incoming friend requests, and unfriend/block users.
* **Block List:** Complete privacy control to block and unblock specific accounts.
* **Nicknames:** Assign custom nicknames to your friends for easier recognition.

## 🛠️ Tech Stack 

- **Language:** Java 11
- **Minimum SDK:** API 24 (Android 7.0)
- **Target SDK:** API 36
- **Architecture & UI:** 
  - Standard MVC/MVVM patterns.
  - View Binding for type-safe and null-safe view access.
  - Native Navigation Components & Material Design.
- **Networking:**
  - [Retrofit2](https://square.github.io/retrofit/) for REST HTTP API endpoints (`https://doscord.top/api/`).
  - [Gson](https://github.com/google/gson) for serialization/deserialization.
  - [OkHttp3](https://square.github.io/okhttp/) with **DNS over HTTPS (DoH)** configurations to ensure secure API connections.
- **Image Loading:**
  - [Glide](https://github.com/bumptech/glide) for optimized image loading and caching.
- **Animations & Background Processing:**
  - [Lottie](https://airbnb.design/lottie/) for high-quality, lightweight animations.
  - [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for deferrable, guaranteed background work.

## 📁 Project Structure

The codebase is organized cleanly by logical components:

```text
app/src/main/java/com/example/doscord/
│
├── activities/       # UI Controllers divided by context
│   ├── chatroom/     # Chat, Messaging, Friends, Groups, and Blocks
│   └── menu/         # Main launch screens, Login, and Multi-stage Registration
│
├── adapters/         # RecyclerView Adapters for lists (Chats, Friends, Messages, Requests)
│
├── api/              # Retrofit interfaces, requests, and networking configurations
│
├── models/           # Data classes and POJOs for the application domain (User, Message, Channel)
│
└── utils/            # Helper classes, Global State, Session Management, UI adjustments (Swipe)
```

## 🚀 Setup and Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/DoscordASP.git
   ```
2. **Open in Android Studio:**
   - Launch Android Studio.
   - Select `File > Open...` and choose the `DoscordASP` root directory.
3. **Sync Gradle:**
   - Wait for Gradle to fetch the dependencies listed in `gradle/libs.versions.toml` and `app/build.gradle`.
4. **Run the App:**
   - Connect an Android device with API 24+ or launch an emulator.
   - Click the **Run** button (`Shift + F10`).

## 📡 API Reference

The app interfaces with a custom backend utilizing standard REST conventions. The `ApiService` manages calls for:
- User endpoints (`/register`, `/login`, `/logout`, `/token-login`, `/check-username`)
- Social queries (`/send-friend-request`, `/handle-friend-request`, etc.)
- Messaging interactions (`/get-messages`, group commands, message mutations, uploading custom PFPs).

## 🔒 Permissions Used

- `INTERNET`: Required to communicate with the application's backend server.
- `ACCESS_NETWORK_STATE`: Required to observe and react to changing network connectivity.
- `RECORD_AUDIO`: Required to support capturing voice messages.

## 🤝 Contribution

Contributions are welcome! Please ensure any pull requests adhere to the formatting guidelines and include appropriate documentation updates.

1. Fork the Project.
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`).
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the Branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

## 📄 License

*(Add your license information here, e.g., MIT, GPLv3, etc.)*
