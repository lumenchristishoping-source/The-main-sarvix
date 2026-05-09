package com.sarvix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarvix.app.data.model.*
import com.sarvix.app.data.repository.AuthRepository
import com.sarvix.app.data.repository.ChatRepository
import com.sarvix.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUserId: String
        get() = authRepository.getCurrentUserId()

    private val _chatsState = MutableStateFlow<Resource<List<ChatPreview>>>(Resource.Idle())
    val chatsState: StateFlow<Resource<List<ChatPreview>>> = _chatsState

    private val _messagesState = MutableStateFlow<Resource<List<Message>>>(Resource.Idle())
    val messagesState: StateFlow<Resource<List<Message>>> = _messagesState

    private val _clarifyLimitState = MutableStateFlow<Resource<ClarifyLimit>>(Resource.Idle())
    val clarifyLimitState: StateFlow<Resource<ClarifyLimit>> = _clarifyLimitState

    private val _selectedIntent = MutableStateFlow<IntentTag?>(null)
    val selectedIntent: StateFlow<IntentTag?> = _selectedIntent

    private val _sendState = MutableStateFlow<Resource<Message>>(Resource.Idle())
    val sendState: StateFlow<Resource<Message>> = _sendState

    fun loadChats() {
        val userId = currentUserId
        if (userId.isEmpty()) return
        viewModelScope.launch {
            chatRepository.loadChats(userId).collectLatest { _chatsState.value = it }
        }
    }

    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            chatRepository.loadMessages(chatId).collectLatest { _messagesState.value = it }
        }
    }

    fun sendMessage(content: String, receiverId: String) {
        val senderId = currentUserId
        if (senderId.isEmpty() || content.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(
                chatId = chatRepository.getOrCreateChatId(senderId, receiverId),
                senderId = senderId,
                receiverId = receiverId,
                content = content,
                intentTag = _selectedIntent.value
            ).collectLatest { _sendState.value = it }
        }
    }

    fun markMessagesAsRead(chatId: String) {
        val userId = currentUserId
        if (userId.isEmpty()) return
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(chatId, userId)
        }
    }

    fun loadClarifyLimit() {
        val userId = currentUserId
        if (userId.isEmpty()) return
        viewModelScope.launch {
            chatRepository.getClarifyLimit(userId).collectLatest { _clarifyLimitState.value = it }
        }
    }

    fun requestClarification(messageId: String) {
        val userId = currentUserId
        if (userId.isEmpty()) return
        viewModelScope.launch {
            chatRepository.requestClarification(messageId, userId).collectLatest { }
        }
    }

    fun translateMessage(messageId: String, targetLanguage: String) {
        viewModelScope.launch {
            chatRepository.translateMessage(messageId, targetLanguage).collectLatest { }
        }
    }

    fun setIntentTag(intent: IntentTag?) {
        _selectedIntent.value = intent
    }

    fun clearSendState() {
        _sendState.value = Resource.Idle()
    }
}
