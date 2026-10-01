package com.example.data.model

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val usernameLowercase: String = "",
    val fullName: String = "",
    val fullNameLowercase: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val educationLevel: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val lastSeen: Long = 0L,
    val onlineStatus: String = "offline",
    val blockedUsers: List<String> = emptyList()
) {
    val isOnline: Boolean
        get() = onlineStatus.equals("online", ignoreCase = true)

    val displayName: String
        get() = when {
            fullName.isNotBlank() -> fullName
            username.isNotBlank() -> username
            email.isNotBlank() -> email.substringBefore("@")
            else -> "User"
        }

    val displayHandle: String
        get() = if (username.isNotBlank()) "@$username" else ""

    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "username" to username,
            "usernameLowercase" to usernameLowercase.ifBlank { username.lowercase() },
            "fullName" to fullName,
            "fullNameLowercase" to fullNameLowercase.ifBlank { fullName.lowercase() },
            "email" to email,
            "photoUrl" to photoUrl,
            "bio" to bio,
            "educationLevel" to educationLevel,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "lastSeen" to lastSeen,
            "onlineStatus" to onlineStatus,
            "blockedUsers" to blockedUsers
        )
    }

    companion object {
        fun fromMap(data: Map<String, Any?>?, defaultUid: String = ""): UserProfile {
            if (data == null) return UserProfile(uid = defaultUid)

            fun parseLong(value: Any?): Long {
                return when (value) {
                    is Number -> value.toLong()
                    is Timestamp -> value.toDate().time
                    is String -> value.toLongOrNull() ?: 0L
                    else -> 0L
                }
            }

            @Suppress("UNCHECKED_CAST")
            val blockedList = (data["blockedUsers"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

            val username = data["username"]?.toString() ?: ""
            val fullName = data["fullName"]?.toString() ?: ""

            return UserProfile(
                uid = data["uid"]?.toString()?.ifBlank { defaultUid } ?: defaultUid,
                username = username,
                usernameLowercase = data["usernameLowercase"]?.toString() ?: username.lowercase(),
                fullName = fullName,
                fullNameLowercase = data["fullNameLowercase"]?.toString() ?: fullName.lowercase(),
                email = data["email"]?.toString() ?: "",
                photoUrl = data["photoUrl"]?.toString() ?: "",
                bio = data["bio"]?.toString() ?: "",
                educationLevel = data["educationLevel"]?.toString() ?: "",
                createdAt = parseLong(data["createdAt"]),
                updatedAt = parseLong(data["updatedAt"]),
                lastSeen = parseLong(data["lastSeen"]),
                onlineStatus = data["onlineStatus"]?.toString() ?: "offline",
                blockedUsers = blockedList
            )
        }
    }
}
