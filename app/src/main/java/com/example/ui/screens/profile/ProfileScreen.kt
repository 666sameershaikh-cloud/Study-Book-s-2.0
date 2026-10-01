package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Download
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.components.AvatarImage
import com.example.ui.components.GlassCard
import com.example.ui.components.WorldBooksButton
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.RoseError
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showInstallHelpDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
    ) {
        item {
            // Profile Card Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AvatarImage(
                    photoUrl = userProfile?.photoUrl,
                    displayName = userProfile?.displayName ?: "User",
                    size = 96.dp,
                    showOnlineBadge = true,
                    isOnline = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userProfile?.displayName ?: "Scholar",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                if (!userProfile?.username.isNullOrBlank()) {
                    Text(
                        text = "@${userProfile?.username}",
                        fontSize = 14.sp,
                        color = CyanGlow,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                if (!userProfile?.bio.isNullOrBlank()) {
                    Text(
                        text = userProfile?.bio ?: "",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                WorldBooksButton(
                    text = "Edit Profile",
                    leadingIcon = Icons.Default.Edit,
                    onClick = onNavigateToEditProfile,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Details Section
        item {
            Text(
                text = "Account Details",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileInfoRow(
                        icon = Icons.Default.Email,
                        label = "Email Address",
                        value = userProfile?.email?.ifBlank { "Not provided" } ?: "Not provided"
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    ProfileInfoRow(
                        icon = Icons.Default.AlternateEmail,
                        label = "Username",
                        value = if (!userProfile?.username.isNullOrBlank()) "@${userProfile?.username}" else "Not set"
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    ProfileInfoRow(
                        icon = Icons.Default.School,
                        label = "Education Stream",
                        value = userProfile?.educationLevel?.ifBlank { "Graduation" } ?: "Graduation"
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    ProfileInfoRow(
                        icon = Icons.Default.Security,
                        label = "Account Security",
                        value = "Firebase Authenticated"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Direct Install APK Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = CyanGlow.copy(alpha = 0.12f),
                borderColor = CyanGlow.copy(alpha = 0.5f),
                onClick = { showInstallHelpDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(CyanGlow.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Direct Install APK",
                            tint = CyanGlow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Direct APK Install • डायरेक्ट इंस्टॉल",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Download & install World Books directly on phone",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Option
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = RoseError.copy(alpha = 0.1f),
                borderColor = RoseError.copy(alpha = 0.3f),
                onClick = { showLogoutDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Log Out",
                        tint = RoseError,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign Out",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoseError
                    )
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }

    if (showInstallHelpDialog) {
        AlertDialog(
            onDismissRequest = { showInstallHelpDialog = false },
            containerColor = SurfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Android, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Direct APK Install", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "World Books APK direct install karne ke aasan steps:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanGlow
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "1. AI Studio ke top-right bar me Settings (⚙️) ya Download / Export icon par click karein.\n" +
                                "2. 'Download APK' (app-debug.apk) par click karein.\n" +
                                "3. Download hone ke baad file ko apne Android phone me open karein.\n" +
                                "4. 'Install from unknown sources' allow karein aur 'Install' dabayein.\n" +
                                "5. App 100% real-time chat, camera photo upload aur books ke sath chalega!",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInstallHelpDialog = false }) {
                    Text("Got It (Samajh gaya)", color = CyanGlow, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = SurfaceCard,
            title = { Text("Sign Out", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out of World Books?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Sign Out", color = RoseError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(IndigoLight.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = IndigoLight,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextMuted
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}
