package com.example.ui.screens.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.ui.components.AvatarImage
import com.example.ui.components.GlassCard
import com.example.ui.components.WorldBooksAppBar
import com.example.ui.components.WorldBooksButton
import com.example.ui.components.WorldBooksTextField
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.RoseError
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    currentProfile: UserProfile?,
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onProfileUpdated: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf(currentProfile?.fullName ?: "") }
    var username by remember { mutableStateOf(currentProfile?.username ?: "") }
    var bio by remember { mutableStateOf(currentProfile?.bio ?: "") }
    var educationLevel by remember { mutableStateOf(currentProfile?.educationLevel ?: "Graduation") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WorldBooksAppBar(
                title = "Edit Profile",
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Photo with Picker Trigger
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AvatarImage(
                        photoUrl = selectedImageUri?.toString() ?: currentProfile?.photoUrl,
                        displayName = fullName.ifBlank { "User" },
                        size = 100.dp
                    )

                    // Overlay icon
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(BackgroundDark.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change profile picture",
                            tint = TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap to choose photo",
                    fontSize = 12.sp,
                    color = CyanGlow
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (errorMessage != null) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        backgroundColor = RoseError.copy(alpha = 0.2f),
                        borderColor = RoseError.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = RoseError,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        WorldBooksTextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                errorMessage = null
                            },
                            label = "Full Name",
                            placeholder = "Enter your full name"
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        WorldBooksTextField(
                            value = username,
                            onValueChange = {
                                username = it.filter { ch -> ch.isLetterOrDigit() || ch == '_' }
                                errorMessage = null
                            },
                            label = "Username",
                            placeholder = "e.g. sameer_sk"
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        WorldBooksTextField(
                            value = educationLevel,
                            onValueChange = { educationLevel = it },
                            label = "Education Stream",
                            placeholder = "e.g. Class 12, BCA, UPSC, etc."
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        WorldBooksTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = "Bio / Study Status",
                            placeholder = "Tell fellow students about your goals...",
                            singleLine = false,
                            modifier = Modifier.height(110.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                WorldBooksButton(
                    text = "Save Profile",
                    isLoading = isLoading,
                    onClick = {
                        val uid = currentProfile?.uid ?: authRepository.currentUserId
                        if (uid.isBlank()) {
                            errorMessage = "Not logged in"
                            return@WorldBooksButton
                        }
                        if (username.trim().length < 3) {
                            errorMessage = "Username must be at least 3 characters."
                            return@WorldBooksButton
                        }

                        isLoading = true
                        errorMessage = null

                        coroutineScope.launch {
                            val result = authRepository.updateProfile(
                                uid = uid,
                                fullName = fullName.trim(),
                                username = username.trim(),
                                bio = bio.trim(),
                                educationLevel = educationLevel.trim(),
                                photoUri = selectedImageUri
                            )

                            isLoading = false
                            result.onSuccess {
                                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                onProfileUpdated()
                                onBack()
                            }.onFailure { e ->
                                errorMessage = e.localizedMessage ?: "Failed to update profile."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
