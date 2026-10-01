package com.example.data.model

import com.google.firebase.Timestamp

data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val type: String = "text", // "text" | "image"
    val text: String = "",
    val mediaUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "messageId" to messageId,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "type" to type,
            "text" to text,
            "mediaUrl" to mediaUrl,
            "timestamp" to timestamp,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(data: Map<String, Any?>?, docId: String = ""): ChatMessage {
            if (data == null) return ChatMessage(messageId = docId)

            fun parseLong(value: Any?): Long {
                return when (value) {
                    is Number -> value.toLong()
                    is Timestamp -> value.toDate().time
                    is String -> value.toLongOrNull() ?: 0L
                    else -> 0L
                }
            }

            val timestamp = parseLong(data["timestamp"]).let {
                if (it == 0L) parseLong(data["createdAt"]) else it
            }.let { if (it == 0L) System.currentTimeMillis() else it }

            return ChatMessage(
                messageId = data["messageId"]?.toString()?.ifBlank { docId } ?: docId,
                senderId = data["senderId"]?.toString() ?: "",
                receiverId = data["receiverId"]?.toString() ?: "",
                type = data["type"]?.toString() ?: "text",
                text = data["text"]?.toString() ?: "",
                mediaUrl = data["mediaUrl"]?.toString() ?: "",
                timestamp = timestamp,
                createdAt = parseLong(data["createdAt"]).let { if (it == 0L) timestamp else it }
            )
        }
    }
}
