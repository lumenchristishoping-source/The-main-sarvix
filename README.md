# Sarvix - Global Communication Clarity Platform

Sarvix is an Android mobile application designed to reduce misunderstandings in text messaging by adding tone, intent, and emotional context tools.

## Features

### Core Communication
- **Real-time Messaging** with Firebase Firestore
- **Intent Tags**: Joke, Serious, Advice, Vent, Rant
- **Clarify Button**: Request clarification on received messages (max 5 per 24 hours)
- **Translation**: Auto-translation for international conversations

### Profile System
- **@handle Username Format**
- **Manual Mood Selection** (NOT auto-detected)
- **Interest Tags** for matching
- **Country & Language** selection

### Matching System
- **Global Matches**: Connect with users worldwide
- **Local Matches**: Find users in your country
- **Balanced Mutual Interest Formula**: Shared Interests / Total Unique Interests
- **Match Percentage Display**
- **Fallback to Global**: When local matches < 3

### Sarvix Reads
- **International Read**: Global text dialogue with auto-translation
- **Local Read**: Country-based community space
- **Text Posts** and **Video Posts** (max 30 seconds)
- **No Autoplay Videos**
- **No Follower Counts**
- **No Public Like Counts**

### UI/UX
- **Minimal Interface**
- **Text-First Design**
- **Left Vertical Navigation Menu** (no bottom navigation bar)
- **Material Design 3**

### Privacy
- **Report Messages** functionality
- **No Heavy AI Surveillance**

### Monetization (Premium)
- Motion Emojis
- Animation Effects
- Advanced AI Tools
- **Core Features FREE**

## Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI Framework**: Jetpack Compose
- **Dependency Injection**: Hilt
- **Backend**: Firebase
  - Authentication
  - Firestore Database
  - Cloud Storage
  - Cloud Messaging
- **Translation**: ML Kit
- **Image Loading**: Coil

## Project Structure

```
app/src/main/java/com/sarvix/app/
├── data/
│   ├── model/          # Data classes
│   └── repository/     # Data access layer
├── di/
│   └── AppModule.kt    # Hilt DI configuration
├── service/
│   └── SarvixMessagingService.kt  # FCM notifications
├── ui/
│   ├── components/     # Reusable UI components
│   ├── navigation/     # Navigation setup
│   ├── screens/        # Screen composables
│   └── theme/          # Material Theme 3
├── utils/
│   ├── Resource.kt     # API state wrapper
│   └── ValidationUtils.kt
├── viewmodel/          # MVVM ViewModels
├── MainActivity.kt
└── SarvixApplication.kt
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Firebase account

### Setup

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/sarvix.git
cd sarvix
```

2. **Firebase Setup**
- Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com/)
- Add an Android app with package name `com.sarvix.app`
- Download `google-services.json` and place it in `app/`
- Enable Authentication (Email/Password), Firestore, Storage, and Cloud Messaging

3. **Configure Firestore Security Rules**
See `DATABASE_SCHEMA.md` for complete security rules.

4. **Build and Run**
```bash
./gradlew assembleDebug
```

## Database Schema

See `DATABASE_SCHEMA.md` for complete Firestore collection structure and relationships.

## Architecture

### MVVM Pattern
- **Model**: Data classes representing entities
- **View**: Jetpack Compose UI screens
- **ViewModel**: State holders with business logic

### Repository Pattern
Each feature has a dedicated repository:
- `AuthRepository`: Authentication
- `ProfileRepository`: User profiles
- `ChatRepository`: Messaging
- `MatchRepository`: User matching
- `PostRepository`: Sarvix Reads
- `TranslationRepository`: ML Kit translation

## Key Features Implementation

### Clarify System
- Maximum 5 clarifications per user per 24 hours
- Stored in `clarify_limits` collection
- Reset automatically after 24 hours

### Translation
- ML Kit on-device translation
- Supports 50+ languages
- Toggle between original and translated text

### Matching Algorithm
```
Match Percentage = (Shared Interests / Total Unique Interests) × 100
```

### Intent Tags
Visual indicators above message bubbles:
- 😂 Joke
- 😐 Serious
- 💡 Advice
- 😤 Vent
- 😠 Rant

## Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## Building Release

```bash
# Create keystore
keytool -genkey -v -keystore sarvix.keystore -alias sarvix -keyalg RSA -keysize 2048 -validity 10000

# Build release APK
./gradlew assembleRelease
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

Copyright © 2024 Sarvix. All rights reserved.

## Support

For support, email support@sarvix.app or join our community chat.