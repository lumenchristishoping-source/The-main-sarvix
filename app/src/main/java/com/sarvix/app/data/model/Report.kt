package com.sarvix.app.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Report(
    @DocumentId
    val id: String = "",
    val reporterId: String = "",
    val reportedUserId: String = "",
    val reportedContentId: String = "", // messageId, postId, etc.
    val contentType: ReportContentType = ReportContentType.MESSAGE,
    val reason: ReportReason = ReportReason.OTHER,
    val description: String = "",
    val status: ReportStatus = ReportStatus.PENDING,
    val reviewedBy: String = "",
    val reviewNotes: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val reviewedAt: Date? = null
)

enum class ReportContentType {
    MESSAGE,
    POST,
    PROFILE,
    CHAT
}

enum class ReportReason {
    HARASSMENT,
    SPAM,
    INAPPROPRIATE_CONTENT,
    HATE_SPEECH,
    IMPERSONATION,
    OTHER
}

enum class ReportStatus {
    PENDING,
    UNDER_REVIEW,
    RESOLVED_ACTION_TAKEN,
    RESOLVED_NO_ACTION,
    DISMISSED
}