# Sarvix Android App - Setup Instructions

## Overview
Sarvix is a global communication clarity platform designed to reduce misunderstandings in text messaging by adding tone, intent, and emotional context tools.

## Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK 34
- Firebase account
- Google Cloud account (for ML Kit Translation)

## Project Structure

```
SarvixApp/
├── app/
│   ├── src/main/java/com/sarvix/app/
│   │   ├── data/
│   │   │   ├── model/          # Data classes (User, Message, Match, etc.)
│   │   │   └── repository/     # Repository classes for data access
│   │   ├── di/
│   │   │   └── AppModule.kt    # Hilt dependency injection
│   │   ├── service/
│   │   │   └── SarvixMessagingService.kt  # FCM notifications
│   │   ├── ui/
│   │   │   ├── components/     # Reusable UI components
│   │   │   ├── navigation/     # Navigation setup
│   │   │   ├── screens/        # Screen composables
│   │   │   └── theme/          # Material Theme 3
│   │   ├── utils/
│   │   │   ├── Resource.kt     # Sealed class for API states
│   │   │   └── ValidationUtils.kt
│   │   ├── viewmodel/          # MVVM ViewModels
│   │   ├── MainActivity.kt
│   │   └── SarvixApplication.kt
│   ├── src/main/res/           # Android resources
│   ├── build.gradle.kts
│   └── google-services.json    # Firebase config (replace with yours)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Setup Steps

### 1. Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project named "Sarvix"
3. Add an Android app with package name: `com.sarvix.app`
4. Download `google-services.json` and replace the placeholder in `app/`
5. Enable the following Firebase services:
   - **Authentication**: Email/Password provider
   - **Firestore Database**: Create in production mode
   - **Storage**: For profile pictures and videos
   - **Cloud Messaging**: For push notifications

### 2. Firestore Database Security Rules

Go to Firestore Database > Rules and paste:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return request.auth.uid == userId;
    }
    
    // Users collection
    match /users/{userId} {
      allow read: if isAuthenticated();
      allow create: if isOwner(userId);
      allow update: if isOwner(userId);
      allow delete: if isOwner(userId);
    }
    
    // Chats collection
    match /chats/{chatId} {
      allow read: if isAuthenticated() && 
        request.auth.uid in resource.data.participants;
      allow create: if isAuthenticated();
      allow update: if isAuthenticated() && 
        request.auth.uid in resource.data.participants;
    }
    
    // Messages collection
    match /messages/{messageId} {
      allow read: if isAuthenticated() && 
        (request.auth.uid == resource.data.senderId || 
         request.auth.uid == resource.data.receiverId);
      allow create: if isAuthenticated() && 
        request.auth.uid == request.resource.data.senderId;
      allow update: if isAuthenticated() && 
        (request.auth.uid == resource.data.senderId || 
         request.auth.uid == resource.data.receiverId);
    }
    
    // Matches collection
    match /matches/{matchId} {
      allow read: if isAuthenticated() && 
        (request.auth.uid == resource.data.userId || 
         request.auth.uid == resource.data.matchedUserId);
      allow create: if isAuthenticated();
      allow update: if isAuthenticated() && 
        (request.auth.uid == resource.data.userId || 
         request.auth.uid == resource.data.matchedUserId);
    }
    
    // Posts collection
    match /posts/{postId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated() && 
        request.auth.uid == request.resource.data.authorId;
      allow update: if isAuthenticated() && 
        request.auth.uid == resource.data.authorId;
      allow delete: if isAuthenticated() && 
        request.auth.uid == resource.data.authorId;
    }
    
    // Clarifications collection
    match /clarifications/{clarificationId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated();
    }
    
    // Clarify limits collection
    match /clarify_limits/{userId} {
      allow read: if isAuthenticated() && isOwner(userId);
      allow write: if isAuthenticated() && isOwner(userId);
    }
    
    // Reports collection (admin only writes)
    match /reports/{reportId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated();
      allow update: if false; // Only via Admin SDK
      allow delete: if false;
    }
  }
}
```

### 3. Firebase Storage Rules

Go to Storage > Rules and paste:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /profile_pictures/{userId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
        request.auth.uid == userId &&
        request.resource.size < 5 * 1024 * 1024 && // 5MB max
        request.resource.contentType.matches('image/.*');
    }
    
    match /videos/{videoId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null &&
        request.resource.size < 50 * 1024 * 1024 && // 50MB max
        request.resource.contentType.matches('video/.*');
    }
  }
}
```

### 4. ML Kit Translation Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your Firebase project
3. Enable the **Cloud Translation API**
4. No additional API key needed - ML Kit uses Firebase App Check

### 5. Build and Run

1. Open the project in Android Studio
2. Sync project with Gradle files
3. Create an emulator or connect a physical device (API 26+)
4. Run the app

## Core Features Implemented

### Authentication
- Email/password login and signup
- Username validation (@handle format required)
- Password reset functionality

### Profile Setup (Mandatory after signup)
- Display name
- Bio (optional)
- Mood selection (manual only - NOT auto-detected)
- At least 3 interests selection
- Country selection
- Language selection

### Chat System
- Real-time messaging with Firestore
- Intent tags (Joke, Serious, Advice, Vent, Rant)
- Clarify button under received messages (max 5 per 24 hours)
- Translation toggle for foreign language messages

### Matching System
- Global and Local match suggestions
- Balanced mutual interest formula: Shared Interests / Total Unique Interests
- Match percentage display
- Fallback to global if local matches < 3
- Maximum 15 suggestions per section

### Sarvix Reads
- International Read (auto-translation enabled)
- Local Read (country-based)
- Text posts
- Video posts (max 30 seconds, no autoplay)
- No follower counts
- No public like counts

### UI Design
- Minimal interface
- Text-first design
- Left vertical navigation menu (no bottom navigation bar)
- Clean, modern Material Design 3

### Privacy
- Report messages functionality
- Reports stored in admin database
- No heavy AI surveillance

### Monetization (Premium Features)
- Motion emojis
- Animation effects
- Advanced AI tools
- Core communication features are FREE

## Architecture

### MVVM Pattern
- **Model**: Data classes in `data/model/`
- **View**: Composable screens in `ui/screens/`
- **ViewModel**: State management in `viewmodel/`

### Repository Pattern
- `AuthRepository`: Authentication operations
- `ProfileRepository`: User profile management
- `ChatRepository`: Messaging and chat operations
- `MatchRepository`: User matching logic
- `PostRepository`: Sarvix Reads content
- `TranslationRepository`: ML Kit translation

### Dependency Injection
- Hilt for dependency injection
- Singleton scope for repositories
- ViewModel scope for ViewModels

## Database Schema

See `DATABASE_SCHEMA.md` for complete Firestore collection structure.

## Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

## Building Release APK

1. Create a keystore:
```bash
keytool -genkey -v -keystore sarvix.keystore -alias sarvix -keyalg RSA -keysize 2048 -validity 10000
```

2. Configure signing in `app/build.gradle.kts`

3. Build release:
```bash
./gradlew assembleRelease
```

## Troubleshooting

### Common Issues

1. **Firebase Authentication fails**
   - Ensure `google-services.json` is correct
   - Check Email/Password provider is enabled in Firebase Console

2. **Firestore permission denied**
   - Verify security rules are deployed
   - Check user is authenticated

3. **Translation not working**
   - Enable Cloud Translation API in Google Cloud Console
   - Ensure device has internet connection

4. **FCM notifications not received**
   - Add SHA-1 fingerprint in Firebase Console
   - Check notification permissions on device

## License

Copyright © 2024 Sarvix. All rights reserved.

## Support

For issues and feature requests, please contact support@sarvix.app