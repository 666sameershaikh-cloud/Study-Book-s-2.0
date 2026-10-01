package com.example.data.model

import com.google.firebase.Timestamp

data class ChatConversation(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageType: String = "text",
    val lastMessageSenderId: String = "",
    val lastMessageTimestamp: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val hiddenFor: List<String> = emptyList(),
    val otherUser: UserProfile? = null
) {
    fun isHiddenFor(userId: String): Boolean {
        return hiddenFor.contains(userId)
    }

    companion object {
        fun fromMap(data: Map<String, Any?>?, docId: String = ""): ChatConversation {
            if (data == null) return ChatConversation(chatId = docId)

            fun parseLong(value: Any?): Long {
                return when (value) {
                    is Number -> value.toLong()
                    is Timestamp -> value.toDate().time
                    is String -> value.toLongOrNull() ?: 0L
                    else -> 0L
                }
            }

            @Suppress("UNCHECKED_CAST")
            val participantsList = (data["participants"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

            @Suppress("UNCHECKED_CAST")
            val hiddenForList = (data["hiddenFor"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

            return ChatConversation(
                chatId = docId,
                participants = participantsList,
                lastMessage = data["lastMessage"]?.toString() ?: "",
                lastMessageType = data["lastMessageType"]?.toString() ?: "text",
                lastMessageSenderId = data["lastMessageSenderId"]?.toString() ?: "",
                lastMessageTimestamp = parseLong(data["lastMessageTimestamp"]),
                createdAt = parseLong(data["createdAt"]),
                updatedAt = parseLong(data["updatedAt"]),
                hiddenFor = hiddenForList
            )
        }
    }
}
