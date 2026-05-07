package com.sarvix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarvix.app.data.model.ChatPreview
import com.sarvix.app.data.model.ClarifyLimit
import com.sarvix.app.data.model.IntentTag
import com.sarvix.app.data.model.Message
import com.sarvix.app.data.repository.AuthRepository
import com.sarvix.app.data.repository.ChatRepository
import com.sarvix.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _chatsState = MutableStateFlow<Resource<List<ChatPreview>>?>(null)
    val chatsState: StateFlow<Resource<List<ChatPreview>>?> = _chatsState

    private val _messagesState = MutableStateFlow<Resource<List<Message>>?>(null)
    val messagesState: StateFlow<Resource<List<Message>>?> = _messagesState

    private val _sendMessageState = MutableStateFlow<Resource<Message>?>(null)
    val sendMessageState: StateFlow<Resource<Message>?> = _sendMessageState

    private val _createChatState = MutableStateFlow<Resource<String>?>(null)
    val createChatState: StateFlow<Resource<String>?> = _createChatState

    private val _clarifyState = MutableStateFlow<Resource<Boolean>?>(null)
    val clarifyState: StateFlow<Resource<Boolean>?> = _clarifyState

    private val _clarifyLimitState = MutableStateFlow<Resource<ClarifyLimit>?>(null)
    val clarifyLimitState: StateFlow<Resource<ClarifyLimit>?> = _clarifyLimitState

    private val _translateState = MutableStateFlow<Resource<String>?>(null)
    val translateState: StateFlow<Resource<String>?> = _translateState

    private val _selectedIntent = MutableStateFlow<IntentTag?>(null)
    val selectedIntent: StateFlow<IntentTag?> = _selectedIntent

    private val _currentChatId = MutableStateFlow<String?>(null)
    val currentChatId: StateFlow<String?> = _currentChatId

    val currentUserId: String
        get() = authRepository.getCurrentUserId()

    fun loadChats() {
        chatRepository.getChats(authRepository.getCurrentUserId())
            .onEach { _chatsState.value = it }
            .launchIn(viewModelScope)
    }

    fun loadMessages(chatId: String) {
        _currentChatId.value = chatId
        chatRepository.getMessages(chatId)
            .onEach { _messagesState.value = it }
            .launchIn(viewModelScope)
    }

    fun sendMessage(content: String, receiverId: String) {
        val chatId = _currentChatId.value ?: return
        chatRepository.sendMessage(chatId, receiverId, content, _selectedIntent.value)
            .onEach { 
                _sendMessageState.value = it
                if (it is Resource.Success) {
                    _selectedIntent.value = null // Clear intent after sending
                }
            }
            .launchIn(viewModelScope)
    }

    fun createChat(otherUserId: String) {
        chatRepository.createChat(otherUserId)
            .onEach { 
                _createChatState.value = it
                if (it is Resource.Success) {
                    _currentChatId.value = it.data
                }
            }
            .launchIn(viewModelScope)
    }

    fun setIntentTag(intentTag: IntentTag?) {
        _selectedIntent.value = intentTag
    }

    fun requestClarification(messageId: String) {
        chatRepository.requestClarification(messageId)
            .onEach { _clarifyState.value = it }
            .launchIn(viewModelScope)
    }

    fun loadClarifyLimit() {
        chatRepository.getClarifyLimit(authRepository.getCurrentUserId())
            .onEach { _clarifyLimitState.value = it }
            .launchIn(viewModelScope)
    }

    fun translateMessage(messageId: String, targetLanguage: String) {
        chatRepository.translateMessage(messageId, targetLanguage)
            .onEach { _translateState.value = it }
            .launchIn(viewModelScope)
    }

    fun markMessagesAsRead(chatId: String) {
        chatRepository.markMessagesAsRead(chatId)
            .launchIn(viewModelScope)
    }

    fun clearChatState() {
        _currentChatId.value = null
        _messagesState.value = null
        _selectedIntent.value = null
    }

    fun clearStates() {
        _sendMessageState.value = null
        _clarifyState.value = null
        _translateState.value = null
        _createChatState.value = null
    }
}