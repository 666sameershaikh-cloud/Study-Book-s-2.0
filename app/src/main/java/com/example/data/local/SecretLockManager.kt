package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.MessageDigest
import java.security.SecureRandom

class SecretLockManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "wb_secret_lock_prefs"
        private const val KEY_HASH = "secret_lock_hash"
        private const val KEY_SALT = "secret_lock_salt"
        private const val KEY_TYPE = "secret_lock_type" // "pin" or "password"
        private const val KEY_ENABLED = "secret_lock_enabled"
    }

    val isLockConfigured: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false) && prefs.getString(KEY_HASH, null) != null

    val lockType: String
        get() = prefs.getString(KEY_TYPE, "pin") ?: "pin"

    fun setSecretLock(secret: String, type: String): Boolean {
        if (secret.isBlank()) return false
        val salt = generateSalt()
        val hash = hashSecret(secret, salt)
        prefs.edit()
            .putString(KEY_HASH, hash)
            .putString(KEY_SALT, salt)
            .putString(KEY_TYPE, type)
            .putBoolean(KEY_ENABLED, true)
            .apply()
        return true
    }

    fun verifySecretLock(secret: String): Boolean {
        val storedHash = prefs.getString(KEY_HASH, null) ?: return false
        val storedSalt = prefs.getString(KEY_SALT, null) ?: return false
        val computedHash = hashSecret(secret, storedSalt)
        return storedHash == computedHash
    }

    fun changeSecretLock(oldSecret: String, newSecret: String, newType: String): Boolean {
        if (!verifySecretLock(oldSecret)) return false
        return setSecretLock(newSecret, newType)
    }

    fun forceResetSecretLockWithBiometric(newSecret: String, newType: String): Boolean {
        return setSecretLock(newSecret, newType)
    }

    fun canAuthenticateWithBiometrics(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val canAuth = biometricManager.canAuthenticate(authenticators)
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticateWithBiometric(
        activity: FragmentActivity,
        title: String = "Secret Chats 💎 Authentication",
        subtitle: String = "Use fingerprint, face, or device screen lock",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Authentication failed. Please try again.")
                }
            }
        )

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onError("Biometric authentication error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashSecret(secret: String, salt: String): String {
        val input = secret + salt
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
