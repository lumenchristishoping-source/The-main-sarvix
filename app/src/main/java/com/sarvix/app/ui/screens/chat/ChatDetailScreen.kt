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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
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
import com.sarvix.app.data.model.Clarification
import com.sarvix.app.data.model.ClarifyLimit
import com.sarvix.app.data.model.IntentTag
import com.sarvix.app.data.model.Message
import com.sarvix.app.ui.theme.*
import com.sarvix.app.utils.Resource
import com.sarvix.app.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messagesState by viewModel.messagesState.collectAsState()
    val sendMessageState by viewModel.sendMessageState.collectAsState()
    val clarifyLimitState by viewModel.clarifyLimitState.collectAsState()
    val selectedIntent by viewModel.selectedIntent.collectAsState()
    
    var messageText by remember { mutableStateOf("") }
    var showIntentSelector by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(key1 = chatId) {
        viewModel.loadMessages(chatId)
        viewModel.loadClarifyLimit()
        viewModel.markMessagesAsRead(chatId)
    }
    
    // Scroll to bottom when new messages arrive
    LaunchedEffect(messagesState) {
        if (messagesState is Resource.Success) {
            val messages = (messagesState as Resource.Success<List<Message>>).data
            messages?.let {
                if (it.isNotEmpty()) {
                    scope.launch {
                        listState.animateScrollToItem(it.size - 1)
                    }
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile picture placeholder
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = SurfaceVariant
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp),
                                tint = OnSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Chat",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Online",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Show options */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column {
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
                            color = getIntentColor(intent),
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
                                text = "×",
                                style = MaterialTheme.typography.titleMedium,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
                
                // Message Input
                MessageInputBar(
                    value = messageText,
                    onValueChange = { messageText = it },
                    onSend = {
                        if (messageText.isNotBlank()) {
                            // Get receiver ID from messages
                            val messages = (messagesState as? Resource.Success)?.data
                            val receiverId = messages?.firstOrNull { it.senderId != viewModel.currentChatId.value }?.senderId
                                ?: messages?.firstOrNull { it.receiverId != viewModel.currentChatId.value }?.receiverId
                                ?: ""
                            
                            viewModel.sendMessage(messageText, receiverId)
                            messageText = ""
                        }
                    },
                    onShowIntentSelector = { showIntentSelector = true }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = messagesState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
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
                            contentPadding = PaddingValues(16.dp),
                            reverseLayout = false
                        ) {
                            items(messages) { message ->
                                MessageBubble(
                                    message = message,
                                    isFromMe = message.senderId == chatId, // Adjust based on actual user ID
                                    clarifyLimit = (clarifyLimitState as? Resource.Success)?.data,
                                    onRequestClarification = {
                                        viewModel.requestClarification(message.id)
                                    },
                                    onTranslate = {
                                        viewModel.translateMessage(message.id, "en") // Use user's language
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
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                null -> {}
            }
        }
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
    var showClarification by remember { mutableStateOf(false) }
    var showTranslation by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        // Intent Tag (if present)
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
        
        // Message Bubble
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            ),
            color = if (isFromMe) ChatBubbleSent else ChatBubbleReceived,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Translated or Original Content
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
                
                // Translation indicator
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
        
        // Clarify Button (only for received messages)
        if (!isFromMe) {
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clarify Button
                val remainingClarifications = clarifyLimit?.getRemainingCount() ?: 5
                val isLimitReached = clarifyLimit?.isLimitReached() ?: false
                
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
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                
                // Remaining count
                if (!isLimitReached) {
                    Text(
                        text = "($remainingClarifications left)",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
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
                            tint = if (showTranslation) Primary else OnSurfaceVariant
                        )
                    }
                }
            }
            
            // Show Clarification if available
            if (showClarification && message.clarifications.isNotEmpty()) {
                val latestClarification = message.clarifications.last()
                ClarificationCard(clarification = latestClarification)
            }
        }
    }
}

@Composable
fun ClarificationCard(clarification: Clarification) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Primary.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Clarification",
                style = MaterialTheme.typography.labelMedium,
                color = Primary
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
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Select Intent Tag",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IntentTag.values().forEach { intent ->
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
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
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
                Text("Clear")
            }
        }
    }
}

@Composable
fun MessageInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onShowIntentSelector: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Intent Tag Button
            IconButton(
                onClick = onShowIntentSelector,
                modifier = Modifier.size(40.dp)
            ) {
                Text(
                    text = "#",
                    style = MaterialTheme.typography.titleLarge,
                    color = Primary
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Text Field
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Type a message...") },
                modifier = Modifier.weight(1f),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { onSend() }
                ),
                shape = RoundedCornerShape(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Send Button
            IconButton(
                onClick = onSend,
                enabled = value.isNotBlank(),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (value.isNotBlank()) Primary else OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun getIntentColor(intent: IntentTag): Color {
    return when (intent) {
        IntentTag.JOKE -> IntentJoke
        IntentTag.SERIOUS -> IntentSerious
        IntentTag.ADVICE -> IntentAdvice
        IntentTag.VENT -> IntentVent
        IntentTag.RANT -> IntentRant
    }
}