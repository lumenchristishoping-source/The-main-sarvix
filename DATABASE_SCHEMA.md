# Sarvix Database Schema

## Firestore Collections

### 1. users
Stores user profile information.

```json
{
  "id": "string (document ID, matches Firebase Auth UID)",
  "email": "string",
  "username": "string (must start with @)",
  "displayName": "string",
  "bio": "string (max 500 chars)",
  "profilePictureUrl": "string (URL to Firebase Storage)",
  "mood": "string (enum: NEUTRAL, HAPPY, EXCITED, CALM, THOUGHTFUL, TIRED, STRESSED, INSPIRED, FOCUSED, SOCIAL, CREATIVE, REFLECTIVE)",
  "interests": ["array of strings"],
  "country": "string (full country name)",
  "countryCode": "string (ISO 3166-1 alpha-2)",
  "language": "string (full language name)",
  "languageCode": "string (ISO 639-1)",
  "isProfileComplete": "boolean",
  "isOnline": "boolean",
  "lastSeen": "timestamp",
  "createdAt": "timestamp",
  "fcmToken": "string (Firebase Cloud Messaging token)",
  "clarifyCountResetTime": "timestamp",
  "dailyClarifyCount": "number (default: 0)"
}
```

**Indexes:**
- `username` (ascending) - for username search
- `countryCode` (ascending), `isProfileComplete` (ascending) - for local matching
- `isProfileComplete` (ascending) - for global matching

---

### 2. chats
Stores chat conversation metadata.

```json
{
  "id": "string (document ID, auto-generated)",
  "participants": ["array of user IDs"],
  "participantUsernames": {
    "userId1": "username1",
    "userId2": "username2"
  },
  "participantPhotos": {
    "userId1": "photoUrl1",
    "userId2": "photoUrl2"
  },
  "lastMessage": "string",
  "lastMessageTimestamp": "timestamp",
  "lastMessageSenderId": "string",
  "unreadCount": {
    "userId1": 0,
    "userId2": 1
  },
  "isActive": "boolean",
  "createdAt": "timestamp"
}
```

**Indexes:**
- `participants` (array contains), `lastMessageTimestamp` (descending)

---

### 3. messages
Stores individual chat messages.

```json
{
  "id": "string (document ID, auto-generated)",
  "chatId": "string (reference to chats collection)",
  "senderId": "string",
  "receiverId": "string",
  "content": "string (message text)",
  "originalContent": "string (original text before translation)",
  "intentTag": "string (enum: JOKE, SERIOUS, ADVICE, VENT, RANT, or null)",
  "isTranslated": "boolean",
  "translatedContent": "string",
  "sourceLanguage": "string (ISO 639-1)",
  "targetLanguage": "string (ISO 639-1)",
  "clarifications": [
    {
      "id": "string",
      "messageId": "string",
      "requestedBy": "string (user ID)",
      "response": "string (clarification text)",
      "tone": "string",
      "intent": "string",
      "createdAt": "timestamp"
    }
  ],
  "isRead": "boolean",
  "isDeleted": "boolean",
  "timestamp": "timestamp"
}
```

**Indexes:**
- `chatId` (ascending), `timestamp` (ascending)
- `receiverId` (ascending), `isRead` (ascending)

---

### 4. matches
Stores user matching information.

```json
{
  "id": "string (document ID, auto-generated)",
  "userId": "string (user who initiated the match)",
  "matchedUserId": "string (target user)",
  "matchType": "string (enum: GLOBAL, LOCAL)",
  "sharedInterests": ["array of strings"],
  "totalUniqueInterests": "number",
  "matchPercentage": "number (0-100)",
  "isMutual": "boolean",
  "status": "string (enum: PENDING, ACCEPTED, DECLINED, BLOCKED)",
  "createdAt": "timestamp",
  "matchedAt": "timestamp"
}
```

**Indexes:**
- `userId` (ascending), `status` (ascending), `matchedAt` (descending)
- `matchedUserId` (ascending), `userId` (ascending)

---

### 5. posts
Stores Sarvix Reads content (text and video posts).

```json
{
  "id": "string (document ID, auto-generated)",
  "authorId": "string",
  "authorUsername": "string",
  "authorProfilePicture": "string",
  "content": "string (text content or caption)",
  "translatedContent": "string",
  "sourceLanguage": "string (ISO 639-1)",
  "type": "string (enum: TEXT, VIDEO)",
  "readSpace": "string (enum: INTERNATIONAL, LOCAL)",
  "isDeleted": "boolean",
  "timestamp": "timestamp",
  
  // Video-specific fields (only for VIDEO type)
  "videoUrl": "string",
  "thumbnailUrl": "string",
  "duration": "number (seconds, max 30)"
}
```

**Indexes:**
- `readSpace` (ascending), `isDeleted` (ascending), `timestamp` (descending)
- `authorId` (ascending), `isDeleted` (ascending), `timestamp` (descending)

---

### 6. clarify_limits
Tracks daily clarification usage per user.

```json
{
  "id": "string (document ID, matches user ID)",
  "userId": "string",
  "dailyCount": "number (default: 0, max: 5)",
  "maxDaily": "number (default: 5)",
  "resetTime": "timestamp"
}
```

---

### 7. reports
Stores user reports for admin review.

```json
{
  "id": "string (document ID, auto-generated)",
  "reporterId": "string",
  "reportedUserId": "string",
  "reportedContentId": "string (messageId, postId, etc.)",
  "contentType": "string (enum: MESSAGE, POST, PROFILE, CHAT)",
  "reason": "string (enum: HARASSMENT, SPAM, INAPPROPRIATE_CONTENT, HATE_SPEECH, IMPERSONATION, OTHER)",
  "description": "string",
  "status": "string (enum: PENDING, UNDER_REVIEW, RESOLVED_ACTION_TAKEN, RESOLVED_NO_ACTION, DISMISSED)",
  "reviewedBy": "string (admin user ID)",
  "reviewNotes": "string",
  "createdAt": "timestamp",
  "reviewedAt": "timestamp"
}
```

**Indexes:**
- `status` (ascending), `createdAt` (descending)
- `reporterId` (ascending), `createdAt` (descending)

---

### 8. post_interactions (Optional)
Tracks post views and shares (no public like counts).

```json
{
  "id": "string (document ID, auto-generated)",
  "postId": "string",
  "userId": "string",
  "interactionType": "string (enum: VIEW, SHARE)",
  "timestamp": "timestamp"
}
```

---

## Collection Relationships

```
users (1)
  ├── chats (N) via participants array
  ├── messages (N) via senderId/receiverId
  ├── matches (N) via userId/matchedUserId
  ├── posts (N) via authorId
  └── clarify_limits (1) via userId

chats (1)
  └── messages (N) via chatId

posts (1)
  └── post_interactions (N) via postId
```

---

## Security Rules Summary

| Collection | Read | Create | Update | Delete |
|------------|------|--------|--------|--------|
| users | Auth | Own | Own | Own |
| chats | Participant | Auth | Participant | - |
| messages | Sender/Receiver | Sender | Sender/Receiver | - |
| matches | Involved | Auth | Involved | - |
| posts | Auth | Author | Author | Author |
| clarify_limits | Own | Own | Own | - |
| reports | Auth | Auth | Admin only | Admin only |

---

## Query Patterns

### Get User's Chats
```javascript
db.collection('chats')
  .where('participants', 'array-contains', userId)
  .orderBy('lastMessageTimestamp', 'desc')
```

### Get Chat Messages
```javascript
db.collection('messages')
  .where('chatId', '==', chatId)
  .orderBy('timestamp', 'asc')
```

### Get Match Suggestions (Local)
```javascript
db.collection('users')
  .where('countryCode', '==', userCountryCode)
  .where('isProfileComplete', '==', true)
  .limit(50)
```

### Get Posts (International)
```javascript
db.collection('posts')
  .where('readSpace', '==', 'INTERNATIONAL')
  .where('isDeleted', '==', false)
  .orderBy('timestamp', 'desc')
  .limit(20)
```

### Get User's Clarify Limit
```javascript
db.collection('clarify_limits')
  .doc(userId)
  .get()
```

---

## Data Retention Policies

1. **Messages**: Retained indefinitely (user can delete own messages)
2. **Deleted Posts**: Soft delete (isDeleted flag), retained for 30 days
3. **Reports**: Retained for 1 year after resolution
4. **Clarify Limits**: Reset automatically every 24 hours
5. **Inactive Users**: Flagged after 90 days of inactivity

---

## Backup Strategy

1. **Automated Backups**: Enable Firebase automated daily backups
2. **Export**: Weekly export of critical collections (users, reports)
3. **Point-in-time Recovery**: Enabled for Firestore

---

## Scaling Considerations

1. **Sharding**: Consider sharding messages by month for high-volume chats
2. **Caching**: Implement client-side caching for user profiles
3. **Pagination**: All list queries use pagination (limit 20)
4. **Indexing**: Composite indexes configured for common query patterns