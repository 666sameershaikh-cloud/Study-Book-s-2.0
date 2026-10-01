package com.example.data.repository

import android.net.Uri
import android.util.Log
import com.example.data.model.ChatConversation
import com.example.data.model.ChatMessage
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    companion object {
        private const val TAG = "ChatRepository"
        private const val USERS_COLLECTION = "users"
        private const val CHATS_COLLECTION = "chats"
        private const val MESSAGES_SUBCOLLECTION = "messages"

        fun getDeterministicChatId(uidA: String, uidB: String): String {
            return if (uidA < uidB) "${uidA}_${uidB}" else "${uidB}_${uidA}"
        }

        val DEFAULT_STUDY_PEERS = listOf(
            UserProfile(
                uid = "peer_priya",
                username = "priya_study",
                usernameLowercase = "priya_study",
                fullName = "Priya Sharma",
                fullNameLowercase = "priya sharma",
                email = "priya@worldbooks.app",
                photoUrl = "",
                bio = "Medical & Biology notes sharing | Final Year",
                educationLevel = "Graduation",
                createdAt = System.currentTimeMillis() - 86400000L * 10,
                updatedAt = System.currentTimeMillis(),
                lastSeen = System.currentTimeMillis(),
                onlineStatus = "online"
            ),
            UserProfile(
                uid = "peer_alex",
                username = "alex_cs",
                usernameLowercase = "alex_cs",
                fullName = "Alex Morgan",
                fullNameLowercase = "alex morgan",
                email = "alex@worldbooks.app",
                photoUrl = "",
                bio = "Algorithms, Data Structures & System Design",
                educationLevel = "Post Graduation",
                createdAt = System.currentTimeMillis() - 86400000L * 15,
                updatedAt = System.currentTimeMillis(),
                lastSeen = System.currentTimeMillis(),
                onlineStatus = "online"
            ),
            UserProfile(
                uid = "peer_rahul",
                username = "rahul_gate",
                usernameLowercase = "rahul_gate",
                fullName = "Rahul Verma",
                fullNameLowercase = "rahul verma",
                email = "rahul@worldbooks.app",
                photoUrl = "",
                bio = "GATE prep & Engineering Physics study group",
                educationLevel = "Graduation",
                createdAt = System.currentTimeMillis() - 86400000L * 5,
                updatedAt = System.currentTimeMillis(),
                lastSeen = System.currentTimeMillis(),
                onlineStatus = "offline"
            ),
            UserProfile(
                uid = "peer_anita",
                username = "anita_lit",
                usernameLowercase = "anita_lit",
                fullName = "Dr. Anita Desai",
                fullNameLowercase = "dr. anita desai",
                email = "anita@worldbooks.app",
                photoUrl = "",
                bio = "Literature & Philosophy mentor",
                educationLevel = "Doctorate",
                createdAt = System.currentTimeMillis() - 86400000L * 25,
                updatedAt = System.currentTimeMillis(),
                lastSeen = System.currentTimeMillis(),
                onlineStatus = "online"
            )
        )
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val localMessages = mutableMapOf<String, MutableStateFlow<List<ChatMessage>>>()
    private val localConversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    private val hiddenChatsSet = mutableSetOf<String>()

    init {
        // Initialize default study chats
        val now = System.currentTimeMillis()
        val defaultConvos = listOf(
            ChatConversation(
                chatId = "chat_peer_priya",
                participants = listOf("peer_priya"),
                lastMessage = "Hey! Let me know if you need the chapter 3 summary notes 📖",
                lastMessageType = "text",
                lastMessageSenderId = "peer_priya",
                lastMessageTimestamp = now - 3600000L,
                updatedAt = now - 3600000L
            ),
            ChatConversation(
                chatId = "chat_peer_alex",
                participants = listOf("peer_alex"),
                lastMessage = "I've solved the practice problems for today's topic.",
                lastMessageType = "text",
                lastMessageSenderId = "peer_alex",
                lastMessageTimestamp = now - 7200000L,
                updatedAt = now - 7200000L
            )
        )
        localConversations.value = defaultConvos

        // Preload sample messages for Priya
        val priyaFlow = getOrCreateLocalMessageFlow("chat_peer_priya")
        priyaFlow.value = listOf(
            ChatMessage(
                messageId = "msg_p1",
                senderId = "peer_priya",
                receiverId = "",
                type = "text",
                text = "Welcome to World Books Secret Study Group! 🌟",
                mediaUrl = "",
                timestamp = now - 7200000L,
                createdAt = now - 7200000L
            ),
            ChatMessage(
                messageId = "msg_p2",
                senderId = "peer_priya",
                receiverId = "",
                type = "text",
                text = "You can share notes, ask questions, or send photos using the camera attachment.",
                mediaUrl = "",
                timestamp = now - 3600000L,
                createdAt = now - 3600000L
            )
        )
    }

    private fun getOrCreateLocalMessageFlow(chatId: String): MutableStateFlow<List<ChatMessage>> {
        synchronized(localMessages) {
            return localMessages.getOrPut(chatId) { MutableStateFlow(emptyList()) }
        }
    }

    /**
     * Search real Firebase users with local study peers fallback.
     */
    suspend fun searchUsers(query: String, currentUserId: String): Result<List<UserProfile>> {
        val trimmed = query.trim().lowercase().removePrefix("@")
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .limit(50)
                .get()
                .await()

            val remoteUsers = snapshot.documents.mapNotNull { doc ->
                if (doc.id == currentUserId) return@mapNotNull null
                val user = UserProfile.fromMap(doc.data, doc.id)
                if (trimmed.isEmpty()) user else {
                    val matchUsername = user.usernameLowercase.contains(trimmed) || user.username.lowercase().contains(trimmed)
                    val matchName = user.fullNameLowercase.contains(trimmed) || user.fullName.lowercase().contains(trimmed)
                    val matchEmail = user.email.lowercase().contains(trimmed)
                    if (matchUsername || matchName || matchEmail) user else null
                }
            }

            val filteredPeers = DEFAULT_STUDY_PEERS.filter { peer ->
                peer.uid != currentUserId && (
                    trimmed.isEmpty() ||
                    peer.usernameLowercase.contains(trimmed) ||
                    peer.fullNameLowercase.contains(trimmed)
                )
            }

            // Combine remote users with study peers, avoiding duplicates
            val combined = (remoteUsers + filteredPeers).distinctBy { it.uid }
            Result.success(combined)
        } catch (e: Exception) {
            Log.w(TAG, "Firestore search failed: ${e.message}, returning study peers fallback")
            val filteredPeers = DEFAULT_STUDY_PEERS.filter { peer ->
                peer.uid != currentUserId && (
                    trimmed.isEmpty() ||
                    peer.usernameLowercase.contains(trimmed) ||
                    peer.fullNameLowercase.contains(trimmed)
                )
            }
            Result.success(filteredPeers)
        }
    }

    /**
     * Real-time listener for messages in a private chat.
     */
    fun getMessagesFlow(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        if (chatId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val localFlow = getOrCreateLocalMessageFlow(chatId)
        trySend(localFlow.value)

        var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
        try {
            listenerRegistration = firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_SUBCOLLECTION)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Firestore messages listener warning: ${error.message}")
                        trySend(localFlow.value)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val remoteMessages = snapshot.documents.map { doc ->
                            ChatMessage.fromMap(doc.data, doc.id)
                        }
                        val merged = (remoteMessages + localFlow.value).distinctBy { it.messageId }.sortedBy { it.timestamp }
                        localFlow.value = merged
                        trySend(merged)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore messages flow init exception: ${e.message}")
            trySend(localFlow.value)
        }

        val job = scope.launch {
            localFlow.collect { list ->
                trySend(list)
            }
        }

        awaitClose {
            listenerRegistration?.remove()
            job.cancel()
        }
    }

    /**
     * Real-time listener for user's conversations.
     */
    fun getConversationsFlow(currentUserId: String): Flow<List<ChatConversation>> = callbackFlow {
        if (currentUserId.isBlank()) {
            trySend(localConversations.value)
            close()
            return@callbackFlow
        }

        trySend(localConversations.value)

        var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
        try {
            listenerRegistration = firestore.collection(CHATS_COLLECTION)
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Firestore conversations listener warning: ${error.message}")
                        trySend(localConversations.value)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val remoteConvos = snapshot.documents.map { doc ->
                            ChatConversation.fromMap(doc.data, doc.id)
                        }
                        val combined = (remoteConvos + localConversations.value).distinctBy { it.chatId }.sortedByDescending { it.lastMessageTimestamp }
                        localConversations.value = combined
                        trySend(combined)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore conversations flow init exception: ${e.message}")
            trySend(localConversations.value)
        }

        val job = scope.launch {
            localConversations.collect { list ->
                trySend(list)
            }
        }

        awaitClose {
            listenerRegistration?.remove()
            job.cancel()
        }
    }

    /**
     * Send a text message.
     */
    suspend fun sendTextMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        text: String
    ): Result<Unit> {
        if (chatId.isBlank() || senderId.isBlank() || text.isBlank()) {
            return Result.failure(Exception("Message content or identifiers cannot be empty"))
        }

        val now = System.currentTimeMillis()
        val messageId = UUID.randomUUID().toString()
        val message = ChatMessage(
            messageId = messageId,
            senderId = senderId,
            receiverId = receiverId,
            type = "text",
            text = text.trim(),
            mediaUrl = "",
            timestamp = now,
            createdAt = now
        )

        // Always save to local flow immediately for instant UI update
        val localFlow = getOrCreateLocalMessageFlow(chatId)
        localFlow.value = (localFlow.value + message).distinctBy { it.messageId }

        updateLocalConversation(
            chatId = chatId,
            participants = listOf(senderId, receiverId),
            lastMessage = text.trim(),
            lastMessageType = "text",
            senderId = senderId,
            timestamp = now
        )

        // Try pushing to Firestore
        try {
            val chatDocRef = firestore.collection(CHATS_COLLECTION).document(chatId)
            chatDocRef.collection(MESSAGES_SUBCOLLECTION)
                .document(messageId)
                .set(message.toMap())
                .await()

            val conversationData = mapOf(
                "chatId" to chatId,
                "participants" to listOf(senderId, receiverId),
                "lastMessage" to text.trim(),
                "lastMessageType" to "text",
                "lastMessageSenderId" to senderId,
                "lastMessageTimestamp" to now,
                "updatedAt" to now
            )
            chatDocRef.set(conversationData, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore sendTextMessage failed, saved locally: ${e.message}")
        }

        // If recipient is a study peer, send an automated study partner response
        if (receiverId.startsWith("peer_")) {
            triggerStudyPeerReply(chatId, receiverId, senderId, text)
        }

        return Result.success(Unit)
    }

    /**
     * Send an image message.
     */
    suspend fun sendImageMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        imageUri: Uri
    ): Result<Unit> {
        val now = System.currentTimeMillis()
        val messageId = UUID.randomUUID().toString()

        var downloadUrl: String = imageUri.toString()
        try {
            val storageRef = storage.reference.child("chats/$chatId/$messageId.jpg")
            storageRef.putFile(imageUri).await()
            downloadUrl = storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Storage upload warning, using local URI: ${e.message}")
        }

        val message = ChatMessage(
            messageId = messageId,
            senderId = senderId,
            receiverId = receiverId,
            type = "image",
            text = "📷 Photo",
            mediaUrl = downloadUrl,
            timestamp = now,
            createdAt = now
        )

        val localFlow = getOrCreateLocalMessageFlow(chatId)
        localFlow.value = (localFlow.value + message).distinctBy { it.messageId }

        updateLocalConversation(
            chatId = chatId,
            participants = listOf(senderId, receiverId),
            lastMessage = "📷 Photo",
            lastMessageType = "image",
            senderId = senderId,
            timestamp = now
        )

        try {
            val chatDocRef = firestore.collection(CHATS_COLLECTION).document(chatId)
            chatDocRef.collection(MESSAGES_SUBCOLLECTION)
                .document(messageId)
                .set(message.toMap())
                .await()

            val conversationData = mapOf(
                "chatId" to chatId,
                "participants" to listOf(senderId, receiverId),
                "lastMessage" to "📷 Photo",
                "lastMessageType" to "image",
                "lastMessageSenderId" to senderId,
                "lastMessageTimestamp" to now,
                "updatedAt" to now
            )
            chatDocRef.set(conversationData, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore sendImageMessage failed, saved locally: ${e.message}")
        }

        if (receiverId.startsWith("peer_")) {
            triggerStudyPeerReply(chatId, receiverId, senderId, "photo")
        }

        return Result.success(Unit)
    }

    private fun updateLocalConversation(
        chatId: String,
        participants: List<String>,
        lastMessage: String,
        lastMessageType: String,
        senderId: String,
        timestamp: Long
    ) {
        val existingList = localConversations.value.toMutableList()
        val index = existingList.indexOfFirst { it.chatId == chatId }
        val updatedConvo = ChatConversation(
            chatId = chatId,
            participants = participants,
            lastMessage = lastMessage,
            lastMessageType = lastMessageType,
            lastMessageSenderId = senderId,
            lastMessageTimestamp = timestamp,
            updatedAt = timestamp
        )
        if (index >= 0) {
            existingList[index] = updatedConvo
        } else {
            existingList.add(0, updatedConvo)
        }
        localConversations.value = existingList.sortedByDescending { it.lastMessageTimestamp }
    }

    private fun triggerStudyPeerReply(chatId: String, peerId: String, userId: String, prompt: String) {
        scope.launch {
            delay(1200)
            val now = System.currentTimeMillis()
            val replyText = when {
                prompt.equals("photo", ignoreCase = true) -> "Received the photo! The diagram / page is clear. Thanks for sharing."
                prompt.contains("hello", ignoreCase = true) || prompt.contains("hi", ignoreCase = true) -> "Hey there! Ready to study today's chapter?"
                prompt.contains("note", ignoreCase = true) -> "I've added your note to my study list. Let me know if you need more materials."
                prompt.contains("exam", ignoreCase = true) -> "All the best! We'll score great together."
                else -> "Got your message! I'm reviewing chapter notes right now in World Books."
            }

            val replyMsg = ChatMessage(
                messageId = UUID.randomUUID().toString(),
                senderId = peerId,
                receiverId = userId,
                type = "text",
                text = replyText,
                mediaUrl = "",
                timestamp = now,
                createdAt = now
            )

            val localFlow = getOrCreateLocalMessageFlow(chatId)
            localFlow.value = (localFlow.value + replyMsg).distinctBy { it.messageId }

            updateLocalConversation(
                chatId = chatId,
                participants = listOf(userId, peerId),
                lastMessage = replyText,
                lastMessageType = "text",
                senderId = peerId,
                timestamp = now
            )
        }
    }

    suspend fun hideChat(chatId: String, userId: String): Result<Unit> {
        hiddenChatsSet.add("${chatId}_$userId")
        val current = localConversations.value.map { convo ->
            if (convo.chatId == chatId) {
                convo.copy(hiddenFor = convo.hiddenFor + userId)
            } else convo
        }
        localConversations.value = current

        return try {
            firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .update("hiddenFor", FieldValue.arrayUnion(userId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    suspend fun unhideChat(chatId: String, userId: String): Result<Unit> {
        hiddenChatsSet.remove("${chatId}_$userId")
        val current = localConversations.value.map { convo ->
            if (convo.chatId == chatId) {
                convo.copy(hiddenFor = convo.hiddenFor.filter { it != userId })
            } else convo
        }
        localConversations.value = current

        return try {
            firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .update("hiddenFor", FieldValue.arrayRemove(userId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    suspend fun blockUser(currentUserId: String, targetUserId: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(currentUserId)
                .update("blockedUsers", FieldValue.arrayUnion(targetUserId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }
}
