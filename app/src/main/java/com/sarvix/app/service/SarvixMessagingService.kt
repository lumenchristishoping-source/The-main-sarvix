package com.sarvix.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sarvix.app.MainActivity
import com.sarvix.app.R
import timber.log.Timber

class SarvixMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID_MESSAGES = "sarvix_messages"
        const val CHANNEL_ID_MATCHES = "sarvix_matches"
        const val CHANNEL_ID_GENERAL = "sarvix_general"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New FCM token: $token")
        // Send token to server
        sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Timber.d("Message received from: ${remoteMessage.from}")
        
        // Handle data payload
        remoteMessage.data.let { data ->
            when (data["type"]) {
                "message" -> handleMessageNotification(data)
                "match" -> handleMatchNotification(data)
                else -> handleGeneralNotification(data)
            }
        }
        
        // Handle notification payload
        remoteMessage.notification?.let {
            showNotification(
                title = it.title ?: "Sarvix",
                body = it.body ?: "",
                channelId = CHANNEL_ID_GENERAL
            )
        }
    }

    private fun handleMessageNotification(data: Map<String, String>) {
        val senderName = data["senderName"] ?: "Someone"
        val message = data["message"] ?: "Sent you a message"
        val chatId = data["chatId"] ?: ""
        
        showNotification(
            title = senderName,
            body = message,
            channelId = CHANNEL_ID_MESSAGES,
            data = mapOf("chatId" to chatId)
        )
    }

    private fun handleMatchNotification(data: Map<String, String>) {
        val matcherName = data["matcherName"] ?: "Someone"
        
        showNotification(
            title = "New Match!",
            body = "$matcherName wants to connect with you",
            channelId = CHANNEL_ID_MATCHES
        )
    }

    private fun handleGeneralNotification(data: Map<String, String>) {
        val title = data["title"] ?: "Sarvix"
        val body = data["body"] ?: ""
        
        showNotification(
            title = title,
            body = body,
            channelId = CHANNEL_ID_GENERAL
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        channelId: String,
        data: Map<String, String> = emptyMap()
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_MESSAGES,
                    "Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for new messages"
                    setSound(
                        android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                },
                NotificationChannel(
                    CHANNEL_ID_MATCHES,
                    "Matches",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for new matches"
                },
                NotificationChannel(
                    CHANNEL_ID_GENERAL,
                    "General",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General notifications"
                }
            )
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(channels)
        }
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Implement token registration with your backend
        // This should send the FCM token to your server for the current user
    }
}