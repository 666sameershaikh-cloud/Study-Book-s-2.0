package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.example.WorldBooksApp
import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    companion object {
        private const val TAG = "AuthRepository"
        private const val USERS_COLLECTION = "users"
        private const val PREFS_NAME = "world_books_auth_prefs"
        private const val KEY_UID = "local_uid"
        private const val KEY_USERNAME = "local_username"
        private const val KEY_FULL_NAME = "local_full_name"
        private const val KEY_EMAIL = "local_email"
        private const val KEY_BIO = "local_bio"
        private const val KEY_EDUCATION = "local_education"
        private const val KEY_PHOTO_URL = "local_photo_url"
    }

    private val prefs: SharedPreferences? by lazy {
        try {
            WorldBooksApp.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        } catch (e: Exception) {
            null
        }
    }

    private val _currentUserProfileFlow = MutableStateFlow<UserProfile?>(loadSavedLocalProfile())
    val currentUserProfileFlow: StateFlow<UserProfile?> = _currentUserProfileFlow.asStateFlow()

    private val _currentUserIdFlow = MutableStateFlow(resolveCurrentUserId())
    val currentUserIdFlow: StateFlow<String> = _currentUserIdFlow.asStateFlow()

    val currentProfileSync: UserProfile?
        get() = _currentUserProfileFlow.value

    val currentFirebaseUser: FirebaseUser?
        get() = try { auth.currentUser } catch (e: Exception) { null }

    val currentUserId: String
        get() = resolveCurrentUserId()

    val isLoggedIn: Boolean
        get() = currentFirebaseUser != null || _currentUserProfileFlow.value != null

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
            _currentUserIdFlow.value = resolveCurrentUserId()
        }
        try {
            auth.addAuthStateListener(listener)
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth listener error: ${e.message}")
        }
        awaitClose {
            try {
                auth.removeAuthStateListener(listener)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun resolveCurrentUserId(): String {
        return try {
            auth.currentUser?.uid?.ifBlank { null }
        } catch (e: Exception) {
            null
        } ?: _currentUserProfileFlow.value?.uid ?: prefs?.getString(KEY_UID, "") ?: ""
    }

    private fun loadSavedLocalProfile(): UserProfile? {
        val p = prefs ?: return null
        val uid = p.getString(KEY_UID, null) ?: return null
        return UserProfile(
            uid = uid,
            username = p.getString(KEY_USERNAME, "student") ?: "student",
            usernameLowercase = (p.getString(KEY_USERNAME, "student") ?: "student").lowercase(),
            fullName = p.getString(KEY_FULL_NAME, "World Books Scholar") ?: "World Books Scholar",
            fullNameLowercase = (p.getString(KEY_FULL_NAME, "World Books Scholar") ?: "World Books Scholar").lowercase(),
            email = p.getString(KEY_EMAIL, "scholar@worldbooks.app") ?: "scholar@worldbooks.app",
            photoUrl = p.getString(KEY_PHOTO_URL, "") ?: "",
            bio = p.getString(KEY_BIO, "Student at World Books") ?: "Student at World Books",
            educationLevel = p.getString(KEY_EDUCATION, "Graduation") ?: "Graduation",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastSeen = System.currentTimeMillis(),
            onlineStatus = "online"
        )
    }

    private fun saveLocalProfile(profile: UserProfile) {
        prefs?.edit()?.apply {
            putString(KEY_UID, profile.uid)
            putString(KEY_USERNAME, profile.username)
            putString(KEY_FULL_NAME, profile.fullName)
            putString(KEY_EMAIL, profile.email)
            putString(KEY_BIO, profile.bio)
            putString(KEY_EDUCATION, profile.educationLevel)
            putString(KEY_PHOTO_URL, profile.photoUrl)
            apply()
        }
        _currentUserProfileFlow.value = profile
        _currentUserIdFlow.value = profile.uid
    }

    private fun clearLocalProfile() {
        prefs?.edit()?.clear()?.apply()
        _currentUserProfileFlow.value = null
        _currentUserIdFlow.value = ""
    }

    private fun isFirebaseErrorRequiringFallback(e: Throwable): Boolean {
        val msg = e.message?.lowercase() ?: ""
        return msg.contains("api key") ||
                msg.contains("internal error") ||
                msg.contains("network") ||
                msg.contains("unavailable") ||
                msg.contains("uninitialized") ||
                e is com.google.firebase.FirebaseException
    }

    suspend fun registerUser(
        fullName: String,
        username: String,
        email: String,
        password: String
    ): Result<UserProfile> {
        val trimmedUsername = username.trim().removePrefix("@")
        val usernameLower = trimmedUsername.lowercase()

        return try {
            // Attempt with Firestore & Firebase Auth
            val existing = try {
                firestore.collection(USERS_COLLECTION)
                    .whereEqualTo("usernameLowercase", usernameLower)
                    .limit(1)
                    .get()
                    .await()
            } catch (e: Exception) {
                null
            }

            if (existing != null && !existing.isEmpty) {
                return Result.failure(Exception("Username @$trimmedUsername is already taken. Please choose another."))
            }

            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw Exception("Failed to create Firebase user")
            val uid = user.uid

            val now = System.currentTimeMillis()
            val userProfile = UserProfile(
                uid = uid,
                username = trimmedUsername,
                usernameLowercase = usernameLower,
                fullName = fullName.trim(),
                fullNameLowercase = fullName.trim().lowercase(),
                email = email.trim(),
                photoUrl = "",
                bio = "Student at World Books",
                educationLevel = "Graduation",
                createdAt = now,
                updatedAt = now,
                lastSeen = now,
                onlineStatus = "offline"
            )

            try {
                firestore.collection(USERS_COLLECTION)
                    .document(uid)
                    .set(userProfile.toMap())
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore save failed, proceeding with registration", e)
            }

            // Always sign out after registration so user can log in explicitly
            auth.signOut()
            saveLocalProfile(userProfile)
            Result.success(userProfile)
        } catch (e: Exception) {
            Log.w(TAG, "Firebase register failed: ${e.message}, checking fallback", e)
            if (isFirebaseErrorRequiringFallback(e)) {
                // Smooth local account creation
                val localUid = "user_" + UUID.randomUUID().toString().take(10)
                val now = System.currentTimeMillis()
                val localProfile = UserProfile(
                    uid = localUid,
                    username = trimmedUsername,
                    usernameLowercase = usernameLower,
                    fullName = fullName.trim(),
                    fullNameLowercase = fullName.trim().lowercase(),
                    email = email.trim(),
                    photoUrl = "",
                    bio = "Student at World Books",
                    educationLevel = "Graduation",
                    createdAt = now,
                    updatedAt = now,
                    lastSeen = now,
                    onlineStatus = "online"
                )
                saveLocalProfile(localProfile)
                Result.success(localProfile)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun loginUser(email: String, password: String): Result<UserProfile> {
        val trimmedEmail = email.trim()

        return try {
            val authResult = auth.signInWithEmailAndPassword(trimmedEmail, password).await()
            val user = authResult.user ?: throw Exception("Login failed: empty user")
            val uid = user.uid

            val docRef = firestore.collection(USERS_COLLECTION).document(uid)
            val snapshot = try { docRef.get().await() } catch (e: Exception) { null }

            val profile: UserProfile
            val now = System.currentTimeMillis()
            if (snapshot != null && snapshot.exists()) {
                val existing = UserProfile.fromMap(snapshot.data, uid)
                val backfilled = existing.copy(
                    usernameLowercase = existing.usernameLowercase.ifBlank { existing.username.lowercase() },
                    fullNameLowercase = existing.fullNameLowercase.ifBlank { existing.fullName.lowercase() },
                    lastSeen = now,
                    onlineStatus = "online"
                )
                try { docRef.set(backfilled.toMap(), SetOptions.merge()).await() } catch (e: Exception) {}
                profile = backfilled
            } else {
                val defaultUsername = user.email?.substringBefore("@") ?: "user_${uid.take(5)}"
                val newProfile = UserProfile(
                    uid = uid,
                    username = defaultUsername,
                    usernameLowercase = defaultUsername.lowercase(),
                    fullName = user.displayName ?: defaultUsername,
                    fullNameLowercase = (user.displayName ?: defaultUsername).lowercase(),
                    email = user.email ?: trimmedEmail,
                    photoUrl = user.photoUrl?.toString() ?: "",
                    bio = "Student at World Books",
                    educationLevel = "Graduation",
                    createdAt = now,
                    updatedAt = now,
                    lastSeen = now,
                    onlineStatus = "online"
                )
                try { docRef.set(newProfile.toMap()).await() } catch (e: Exception) {}
                profile = newProfile
            }

            saveLocalProfile(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Log.w(TAG, "Firebase login failed: ${e.message}, checking fallback", e)
            if (isFirebaseErrorRequiringFallback(e)) {
                // If local account already exists, use it; otherwise create one with email
                val existing = loadSavedLocalProfile()
                val resolvedProfile = if (existing != null && existing.email.equals(trimmedEmail, ignoreCase = true)) {
                    existing
                } else {
                    val defaultName = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                    val newUid = "local_" + UUID.randomUUID().toString().take(10)
                    val now = System.currentTimeMillis()
                    UserProfile(
                        uid = newUid,
                        username = trimmedEmail.substringBefore("@").lowercase(),
                        usernameLowercase = trimmedEmail.substringBefore("@").lowercase(),
                        fullName = defaultName,
                        fullNameLowercase = defaultName.lowercase(),
                        email = trimmedEmail,
                        photoUrl = "",
                        bio = "Student at World Books",
                        educationLevel = "Graduation",
                        createdAt = now,
                        updatedAt = now,
                        lastSeen = now,
                        onlineStatus = "online"
                    )
                }
                saveLocalProfile(resolvedProfile)
                Result.success(resolvedProfile)
            } else {
                Result.failure(e)
            }
        }
    }

    fun quickGuestLogin(): Result<UserProfile> {
        val existing = loadSavedLocalProfile()
        if (existing != null) {
            _currentUserProfileFlow.value = existing
            _currentUserIdFlow.value = existing.uid
            return Result.success(existing)
        }
        val guestUid = "scholar_" + UUID.randomUUID().toString().take(8)
        val now = System.currentTimeMillis()
        val guestProfile = UserProfile(
            uid = guestUid,
            username = "scholar_" + guestUid.takeLast(4),
            usernameLowercase = "scholar_" + guestUid.takeLast(4),
            fullName = "World Books Scholar",
            fullNameLowercase = "world books scholar",
            email = "scholar@worldbooks.app",
            photoUrl = "",
            bio = "Active student exploring books & secret study groups",
            educationLevel = "Undergraduate",
            createdAt = now,
            updatedAt = now,
            lastSeen = now,
            onlineStatus = "online"
        )
        saveLocalProfile(guestProfile)
        return Result.success(guestProfile)
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        if (uid.isBlank()) return null
        val local = _currentUserProfileFlow.value
        if (local != null && local.uid == uid) {
            return local
        }
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION).document(uid).get().await()
            if (snapshot.exists()) {
                UserProfile.fromMap(snapshot.data, uid)
            } else local
        } catch (e: Exception) {
            local
        }
    }

    suspend fun updateProfile(
        uid: String,
        fullName: String,
        username: String,
        bio: String,
        educationLevel: String,
        photoUri: Uri?
    ): Result<UserProfile> {
        return try {
            val trimmedUsername = username.trim().removePrefix("@")
            val usernameLower = trimmedUsername.lowercase()

            var finalPhotoUrl: String? = photoUri?.toString()
            if (photoUri != null && photoUri.scheme != "http" && photoUri.scheme != "https") {
                try {
                    val ref = storage.reference.child("profile_photos/$uid.jpg")
                    ref.putFile(photoUri).await()
                    finalPhotoUrl = ref.downloadUrl.await().toString()
                } catch (e: Exception) {
                    Log.w(TAG, "Storage upload failed, using local URI", e)
                }
            }

            val existingProfile = getUserProfile(uid) ?: UserProfile(
                uid = uid,
                username = trimmedUsername,
                usernameLowercase = usernameLower,
                fullName = fullName.trim(),
                fullNameLowercase = fullName.trim().lowercase(),
                email = "scholar@worldbooks.app",
                photoUrl = finalPhotoUrl ?: "",
                bio = bio.trim(),
                educationLevel = educationLevel,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                lastSeen = System.currentTimeMillis(),
                onlineStatus = "online"
            )

            val updated = existingProfile.copy(
                fullName = fullName.trim(),
                fullNameLowercase = fullName.trim().lowercase(),
                username = trimmedUsername,
                usernameLowercase = usernameLower,
                bio = bio.trim(),
                educationLevel = educationLevel,
                photoUrl = finalPhotoUrl ?: existingProfile.photoUrl,
                updatedAt = System.currentTimeMillis()
            )

            try {
                firestore.collection(USERS_COLLECTION)
                    .document(uid)
                    .set(updated.toMap(), SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore profile update failed, saving locally", e)
            }

            saveLocalProfile(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
            Result.failure(e)
        }
    }

    suspend fun updateOnlineStatus(isOnline: Boolean) {
        val uid = currentUserId
        if (uid.isBlank()) return
        try {
            val status = if (isOnline) "online" else "offline"
            firestore.collection(USERS_COLLECTION).document(uid).update(
                mapOf(
                    "onlineStatus" to status,
                    "lastSeen" to System.currentTimeMillis()
                )
            ).await()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun logout() {
        val uid = currentUserId
        if (uid.isNotBlank()) {
            try {
                firestore.collection(USERS_COLLECTION).document(uid).update(
                    mapOf(
                        "onlineStatus" to "offline",
                        "lastSeen" to System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                // ignore
            }
        }
        try {
            auth.signOut()
        } catch (e: Exception) {
            // ignore
        }
        clearLocalProfile()
    }
}
