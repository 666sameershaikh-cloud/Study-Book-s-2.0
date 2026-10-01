package com.example.ui.screens.secret

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.local.SecretLockManager
import com.example.ui.components.GlassCard
import com.example.ui.components.WorldBooksAppBar
import com.example.ui.components.WorldBooksButton
import com.example.ui.components.WorldBooksTextField
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.RoseError
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@Composable
fun SecretLockScreen(
    onUnlockSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lockManager = remember { SecretLockManager(context) }
    val isLockSet = remember { lockManager.isLockConfigured }

    var secretInput by remember { mutableStateOf("") }
    var confirmSecretInput by remember { mutableStateOf("") }
    var lockType by remember { mutableStateOf("pin") } // "pin" or "password"
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun triggerBiometric() {
        val activity = context as? FragmentActivity
        if (activity != null && lockManager.canAuthenticateWithBiometrics()) {
            errorMessage = null
            lockManager.authenticateWithBiometric(
                activity = activity,
                title = "Secret Chats 💎",
                subtitle = "Authenticate to access private conversations",
                onSuccess = {
                    onUnlockSuccess()
                },
                onError = { err ->
                    errorMessage = err
                }
            )
        } else {
            errorMessage = "Please set up a secure screen lock on your Android device to use Secret Chats."
        }
    }

    // Auto-prompt biometric if configured on launch
    LaunchedEffect(Unit) {
        if (isLockSet && lockManager.canAuthenticateWithBiometrics()) {
            triggerBiometric()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WorldBooksAppBar(
                title = "Secret Lock 💎",
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Diamond Emblem
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            IndigoLight.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "💎", fontSize = 36.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isLockSet) "Enter Secret Lock" else "Create Secret Lock",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = if (isLockSet) "Authenticate to unlock your Secret Chats"
                    else "Choose a PIN or Password to protect your private discussions",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

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

                // First time setup: allow choosing PIN or Password
                if (!isLockSet) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilterChip(
                            selected = lockType == "pin",
                            onClick = { lockType = "pin" },
                            label = { Text("4-6 Digit PIN") },
                            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IndigoLight,
                                selectedLabelColor = BackgroundDark,
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        FilterChip(
                            selected = lockType == "password",
                            onClick = { lockType = "password" },
                            label = { Text("Alphanumeric Password") },
                            leadingIcon = { Icon(Icons.Default.Password, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IndigoLight,
                                selectedLabelColor = BackgroundDark,
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                // Input Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        WorldBooksTextField(
                            value = secretInput,
                            onValueChange = {
                                secretInput = if (lockType == "pin") it.filter { ch -> ch.isDigit() } else it
                                errorMessage = null
                            },
                            label = if (lockType == "pin") "Enter PIN" else "Enter Password",
                            placeholder = if (lockType == "pin") "••••" else "Enter secure password",
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = IndigoLight)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle visibility",
                                        tint = TextMuted
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (lockType == "pin") KeyboardType.NumberPassword else KeyboardType.Password
                            )
                        )

                        if (!isLockSet) {
                            Spacer(modifier = Modifier.height(14.dp))
                            WorldBooksTextField(
                                value = confirmSecretInput,
                                onValueChange = {
                                    confirmSecretInput = if (lockType == "pin") it.filter { ch -> ch.isDigit() } else it
                                    errorMessage = null
                                },
                                label = "Confirm ${if (lockType == "pin") "PIN" else "Password"}",
                                placeholder = "Repeat to confirm",
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (lockType == "pin") KeyboardType.NumberPassword else KeyboardType.Password
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        WorldBooksButton(
                            text = if (isLockSet) "Unlock Secret Chats 💎" else "Save & Open Secret Chats 💎",
                            onClick = {
                                if (isLockSet) {
                                    if (lockManager.verifySecretLock(secretInput)) {
                                        onUnlockSuccess()
                                    } else {
                                        errorMessage = "Incorrect Secret Lock. Please try again."
                                    }
                                } else {
                                    if (secretInput.isBlank()) {
                                        errorMessage = "Please enter a valid secret lock."
                                        return@WorldBooksButton
                                    }
                                    if (lockType == "pin" && secretInput.length < 4) {
                                        errorMessage = "PIN must be at least 4 digits."
                                        return@WorldBooksButton
                                    }
                                    if (lockType == "password" && secretInput.length < 4) {
                                        errorMessage = "Password must be at least 4 characters."
                                        return@WorldBooksButton
                                    }
                                    if (secretInput != confirmSecretInput) {
                                        errorMessage = "Secret locks do not match."
                                        return@WorldBooksButton
                                    }

                                    lockManager.setSecretLock(secretInput, lockType)
                                    onUnlockSuccess()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Device Biometric authentication option
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { triggerBiometric() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Device Authentication",
                        tint = CyanGlow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Use device authentication (Fingerprint / Face / Lock)",
                        color = CyanGlow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
