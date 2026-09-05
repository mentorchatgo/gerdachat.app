package com.example.gerdachat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gerdachat.data.model.CallRecord
import com.example.gerdachat.data.model.ChatMessage
import com.example.gerdachat.data.model.Contact
import com.example.gerdachat.data.model.Memory
import com.example.gerdachat.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActiveCallState(
    val contact: Contact,
    val isVideo: Boolean,
    val durationSeconds: Int = 0,
    val isConnected: Boolean = false
)

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    val contacts: StateFlow<List<Contact>> = repository.getContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calls: StateFlow<List<CallRecord>> = repository.getCalls()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<Memory>> = repository.getMemories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedContact = MutableStateFlow<Contact?>(null)
    val selectedContact: StateFlow<Contact?> = _selectedContact.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _realisticDelay = MutableStateFlow(true)
    val realisticDelay: StateFlow<Boolean> = _realisticDelay.asStateFlow()

    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall: StateFlow<ActiveCallState?> = _activeCall.asStateFlow()

    private var messageCollectionJob: Job? = null
    private var callTimerJob: Job? = null

    fun selectContact(contact: Contact) {
        _selectedContact.value = contact
        messageCollectionJob?.cancel()
        messageCollectionJob = viewModelScope.launch {
            repository.getMessages(contact.id).collect {
                _messages.value = it
            }
        }
        viewModelScope.launch {
            repository.contactDao.markAsRead(contact.id)
        }
    }

    fun toggleRealisticDelay() {
        _realisticDelay.value = !_realisticDelay.value
    }

    fun sendMessage(text: String, imageUrl: String? = null) {
        val contact = _selectedContact.value ?: return
        if (text.isBlank() && imageUrl == null) return

        viewModelScope.launch {
            repository.sendMessage(contact.id, text, imageUrl)

            // Simulate typing indicator
            _isTyping.value = true

            val delayDuration = if (_realisticDelay.value) {
                (1200L + (text.length * 40L)).coerceIn(1200L, 3500L)
            } else {
                400L
            }
            delay(delayDuration)

            repository.getAiResponse(contact.id, _messages.value)
            _isTyping.value = false
        }
    }

    fun startCall(contact: Contact, isVideo: Boolean) {
        _activeCall.value = ActiveCallState(contact = contact, isVideo = isVideo, durationSeconds = 0, isConnected = false)
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            // Ringing for 2 seconds
            delay(2000)
            _activeCall.value = _activeCall.value?.copy(isConnected = true)

            while (true) {
                delay(1000)
                val current = _activeCall.value ?: break
                if (current.isConnected) {
                    _activeCall.value = current.copy(durationSeconds = current.durationSeconds + 1)
                }
            }
        }
    }

    fun endCall() {
        val current = _activeCall.value
        callTimerJob?.cancel()
        _activeCall.value = null

        if (current != null) {
            viewModelScope.launch {
                repository.recordCall(
                    contactId = current.contact.id,
                    durationSeconds = current.durationSeconds,
                    isVideo = current.isVideo,
                    isIncoming = false
                )
            }
        }
    }

    fun clearChat(contactId: String) {
        viewModelScope.launch {
            repository.messageDao.deleteMessagesForContact(contactId)
            repository.contactDao.updateLastMessage(contactId, "", "")
        }
    }

    fun clearMemories() {
        viewModelScope.launch {
            repository.memoryDao.clearMemories()
        }
    }

    fun saveContact(contact: Contact) {
        viewModelScope.launch {
            repository.contactDao.insertContact(contact)
            if (_selectedContact.value?.id == contact.id) {
                _selectedContact.value = contact
            }
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            repository.contactDao.deleteContact(contact)
            repository.messageDao.deleteMessagesForContact(contact.id)
            if (_selectedContact.value?.id == contact.id) {
                _selectedContact.value = null
            }
        }
    }
}

class ChatViewModelFactory(private val repository: ChatRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
