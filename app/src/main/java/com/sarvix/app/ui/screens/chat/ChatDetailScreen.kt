package com.sarvix.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sarvix.app.data.model.*
import com.sarvix.app.ui.components.getMoodColor
import com.sarvix.app.ui.theme.*
import com.sarvix.app.utils.Resource
import com.sarvix.app.viewmodel.ChatViewModel
import com.sarvix.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatDetailScreen(
    chatId: String,
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val messagesState by viewModel.messagesState.collectAsState()
    val clarifyLimitState by viewModel.clarifyLimitState.collectAsState()
    val selectedIntent by viewModel.selectedIntent.collectAsState()
    val currentUserMood by profileViewModel.currentUserMood.collectAsState()
    var messageText by remember { mutableStateOf("") }
    var showIntentSelector by remember { mutableStateOf(false) }
    var showMoodSelector by remember { mutableStateOf(false) }
    var showEmojiWheel by remember { mutableStateOf(false) }
    var selectedEmoji by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = chatId) {
        viewModel.loadMessages(chatId)
        viewModel.loadClarifyLimit()
        viewModel.markMessagesAsRead(chatId)
        profileViewModel.loadCurrentUserMood()
    }
    LaunchedEffect(messagesState) {
        if (messagesState is Resource.Success) {
            val messages = (messagesState as Resource.Success<List<Message>>).data
            messages?.let {
                if (it.isNotEmpty()) {
                    scope.launch { listState.animateScrollToItem(it.size - 1) }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Messages area
        Box(modifier = Modifier.weight(1f)) {
            when (val state = messagesState) {
                is Resource.Loading -> {
                    // Blank - no spinner
                }
                is Resource.Success -> {
                    val messages = state.data ?: emptyList()
                    if (messages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Start a conversation",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState,
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(messages) { message ->
                                MessageBubble(
                                    message = message,
                                    isFromMe = message.senderId == viewModel.currentUserId,
                                    clarifyLimit = (clarifyLimitState as? Resource.Success)?.data,
                                    onRequestClarification = {
                                        viewModel.requestClarification(message.id)
                                    },
                                    onTranslate = {
                                        viewModel.translateMessage(message.id, "en")
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message ?: "Failed to load messages",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Error
                        )
                    }
                }
                null -> {}
            }
        }

        // Mood Selector Popup
        if (showMoodSelector) {
            MoodSelectorPopup(
                currentMood = currentUserMood,
                onMoodSelected = {
                    profileViewModel.updateMood(it)
                    showMoodSelector = false
                },
                onDismiss = { showMoodSelector = false }
            )
        }

        // Emoji Wheel Popup (premium placeholder)
        if (showEmojiWheel) {
            EmojiWheelPopup(
                onEmojiSelected = {
                    selectedEmoji = it
                    showEmojiWheel = false
                },
                onDismiss = { showEmojiWheel = false }
            )
        }

        // Intent Tag Selector
        if (showIntentSelector) {
            IntentTagSelector(
                selectedIntent = selectedIntent,
                onIntentSelected = {
                    viewModel.setIntentTag(it)
                    showIntentSelector = false
                },
                onDismiss = { showIntentSelector = false }
            )
        }

        // Selected Intent Display
        selectedIntent?.let { intent ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = getIntentColor(intent).copy(alpha = 0.2f),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "${intent.emoji} ${intent.displayName}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                IconButton(
                    onClick = { viewModel.setIntentTag(null) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text(
                        text = "x",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        }

        // FIXED: Message Input Bar with mood switch and emoji wheel
        ChatInputBar(
            value = messageText,
            onValueChange = { messageText = it },
            onSend = {
                if (messageText.isNotBlank()) {
                    val messages = (messagesState as? Resource.Success)?.data
                    val currentUserId = viewModel.currentUserId
                    val receiverId = messages?.firstOrNull { it.senderId != currentUserId }?.senderId
                        ?: messages?.firstOrNull { it.receiverId != currentUserId }?.receiverId
                        ?: ""
                    viewModel.sendMessage(messageText, receiverId)
                    messageText = ""
                }
            },
            onShowIntentSelector = { showIntentSelector = true },
            onShowMoodSelector = { showMoodSelector = true },
            onShowEmojiWheel = { showEmojiWheel = true },
            selectedEmoji = selectedEmoji,
            currentMood = currentUserMood
        )
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isFromMe: Boolean,
    clarifyLimit: ClarifyLimit?,
    onRequestClarification: () -> Unit,
    onTranslate: () -> Unit
) {
    var showClarification by remember { mutableStateOf(message.clarifications.isNotEmpty()) }
    var showTranslation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        // Intent Tag
        message.intentTag?.let { intent ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = getIntentColor(intent).copy(alpha = 0.2f),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = "${intent.emoji} ${intent.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Message Bubble with mood gradient for sent messages
        val bubbleColor = if (isFromMe) ChatBubbleSent else ChatBubbleReceived
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            ),
            color = bubbleColor,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val displayContent = if (showTranslation && message.translatedContent != null) {
                    message.translatedContent
                } else {
                    message.content
                }
                Text(
                    text = displayContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFromMe) ChatTextSent else ChatTextReceived
                )
                if (message.isTranslated && showTranslation) {
                    Text(
                        text = "Translated",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFromMe) ChatTextSent.copy(alpha = 0.7f) else OnSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Timestamp
        Text(
            text = message.timestamp?.let {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
            } ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )

        // Clarify & Translation row (only for received messages)
        if (!isFromMe) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val hasClarification = message.clarifications.isNotEmpty()
                val isLimitReached = clarifyLimit?.isLimitReached() ?: false

                if (hasClarification) {
                    TextButton(
                        onClick = { showClarification = !showClarification },
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = if (showClarification) "Hide" else "Show Clarification",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentCyan
                        )
                    }
                } else {
                    TextButton(
                        onClick = {
                            if (!isLimitReached) {
                                onRequestClarification()
                                showClarification = true
                            }
                        },
                        enabled = !isLimitReached,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = if (isLimitReached) "Limit Reached" else "Clarify",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isLimitReached) OnSurfaceVariant else AccentCyan
                        )
                    }
                    val remaining = clarifyLimit?.getRemainingCount() ?: 5
                    if (!isLimitReached) {
                        Text(
                            text = "($remaining left)",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                    }
                }

                // Translation toggle
                if (message.sourceLanguage.isNotEmpty() && message.sourceLanguage != "en") {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showTranslation = !showTranslation },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Translate",
                            modifier = Modifier.size(18.dp),
                            tint = if (showTranslation) AccentCyan else OnSurfaceVariant
                        )
                    }
                }
            }
        }

        // Show Clarification
        if (showClarification && message.clarifications.isNotEmpty()) {
            val latestClarification = message.clarifications.last()
            ClarificationCard(clarification = latestClarification)
        }
    }
}

@Composable
fun ClarificationCard(clarification: Clarification) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(top = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Primary.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Clarification",
                style = MaterialTheme.typography.labelMedium,
                color = PrimaryLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = clarification.response,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = "Tone: ${clarification.tone}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Intent: ${clarification.intent}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun IntentTagSelector(
    selectedIntent: IntentTag?,
    onIntentSelected: (IntentTag?) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Surface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Select Intent Tag",
                style = MaterialTheme.typography.titleSmall,
                color = OnSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IntentTag.entries.forEach { intent ->
                    val isSelected = selectedIntent == intent
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onIntentSelected(intent) }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) getIntentColor(intent) else SurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = intent.emoji,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = intent.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) getIntentColor(intent) else OnSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { onIntentSelected(null) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Clear", color = OnSurfaceVariant)
            }
        }
    }
}

// === FIXED: Mood Selector Popup ===
@Composable
fun MoodSelectorPopup(
    currentMood: MoodStatus,
    onMoodSelected: (MoodStatus) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Surface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.titleSmall,
                color = OnSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            // Grid of moods
            val moods = MoodStatus.entries
            for (i in moods.indices step 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (j in 0 until 4) {
                        val idx = i + j
                        if (idx < moods.size) {
                            val mood = moods[idx]
                            val isSelected = currentMood == mood
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onMoodSelected(mood) }
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) getMoodColor(mood).copy(alpha = 0.2f)
                                        else androidx.compose.ui.graphics.Color.Transparent
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = mood.emoji,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = mood.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) getMoodColor(mood) else OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("Cancel", color = OnSurfaceVariant)
            }
        }
    }
}

// === Premium Emoji Wheel Popup (placeholder) ===
@Composable
fun EmojiWheelPopup(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val commonEmojis = listOf("xD", "xP", "xO", "B)", ":)", ";)", ":D", ":'D", ":3", "x3", ":|", ":/")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Surface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Motion Emojis",
                style = MaterialTheme.typography.titleSmall,
                color = OnSurface,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Premium Feature - Free Preview",
                style = MaterialTheme.typography.labelSmall,
                color = AccentCyan,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            // Circular arrangement of emojis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                commonEmojis.forEach { emoji ->
                    Surface(
                        shape = CircleShape,
                        color = SurfaceVariant,
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onEmojiSelected(emoji) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = emoji,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurface
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("Close", color = OnSurfaceVariant)
            }
        }
    }
}

// === FIXED: Chat Input Bar with mood switch and emoji wheel ===
@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onShowIntentSelector: () -> Unit,
    onShowMoodSelector: () -> Unit,
    onShowEmojiWheel: () -> Unit,
    selectedEmoji: String,
    currentMood: MoodStatus
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp,
        color = Surface
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Mood Switch Button - TOP LEFT of keyboard area
                IconButton(
                    onClick = onShowMoodSelector,
                    modifier = Modifier.size(36.dp)
                ) {
                    Text(
                        text = currentMood.emoji,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Intent Tag Button
                IconButton(
                    onClick = onShowIntentSelector,
                    modifier = Modifier.size(36.dp)
                ) {
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.titleLarge,
                        color = AccentCyan
                    )
                }

                // Emoji Wheel Button (premium placeholder)
                IconButton(
                    onClick = onShowEmojiWheel,
                    modifier = Modifier.size(36.dp)
                ) {
                    Text(
                        text = if (selectedEmoji.isNotEmpty()) selectedEmoji else "xD",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selectedEmoji.isNotEmpty()) AccentPink else OnSurfaceVariant
                    )
                }
            }

            // Main input row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text("Type a message...", color = OnSurfaceVariant) },
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryLight,
                        unfocusedBorderColor = DividerColor
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                // FIXED: Gradient Send Button
                IconButton(
                    onClick = onSend,
                    enabled = value.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (value.isNotBlank()) {
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(GradientPurple, AccentPink, AccentCyan)
                                )
                            } else {
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(SurfaceVariant, SurfaceVariant)
                                )
                            }
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = OnPrimary
                    )
                }
            }
        }
    }
}

fun getIntentColor(intent: IntentTag): androidx.compose.ui.graphics.Color {
    return when (intent) {
        IntentTag.JOKE -> IntentJoke
        IntentTag.SERIOUS -> IntentSerious
        IntentTag.ADVICE -> IntentAdvice
        IntentTag.VENT -> IntentVent
        IntentTag.RANT -> IntentRant
    }
}
