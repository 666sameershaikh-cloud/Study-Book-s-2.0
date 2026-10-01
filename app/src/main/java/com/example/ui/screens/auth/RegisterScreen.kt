package com.example.ui.screens.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AuthRepository
import com.example.ui.components.GlassCard
import com.example.ui.components.WorldBooksButton
import com.example.ui.components.WorldBooksTextField
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.RoseError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    authRepository: AuthRepository,
    onRegisterSuccess: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        IndigoDark.copy(alpha = 0.35f),
                        BackgroundDark,
                        Color(0xFF04060A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Brush.radialGradient(listOf(IndigoLight.copy(alpha = 0.4f), Color.Transparent)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "World Books",
                    tint = IndigoLight,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Join World Books",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Create your student account to get started",
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

            // Register Card
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    WorldBooksTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            errorMessage = null
                        },
                        label = "Full Name",
                        placeholder = "e.g. Sameer Shaikh",
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = IndigoLight)
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    WorldBooksTextField(
                        value = username,
                        onValueChange = {
                            username = it.filter { ch -> ch.isLetterOrDigit() || ch == '_' }
                            errorMessage = null
                        },
                        label = "Username",
                        placeholder = "e.g. sameer_sk",
                        leadingIcon = {
                            Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = IndigoLight)
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    WorldBooksTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = "Email Address",
                        placeholder = "student@example.com",
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = IndigoLight)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    WorldBooksTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = "Password",
                        placeholder = "At least 6 characters",
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = IndigoLight)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    WorldBooksTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = "Confirm Password",
                        placeholder = "Repeat your password",
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = IndigoLight)
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    WorldBooksButton(
                        text = "Create Account",
                        isLoading = isLoading,
                        onClick = {
                            val cleanName = fullName.trim()
                            val cleanUser = username.trim()
                            val cleanEmail = email.trim()

                            if (cleanName.isBlank() || cleanUser.isBlank() || cleanEmail.isBlank() || password.isBlank()) {
                                errorMessage = "All fields are required."
                                return@WorldBooksButton
                            }
                            if (cleanUser.length < 3) {
                                errorMessage = "Username must be at least 3 characters."
                                return@WorldBooksButton
                            }
                            if (password.length < 6) {
                                errorMessage = "Password must be at least 6 characters long."
                                return@WorldBooksButton
                            }
                            if (password != confirmPassword) {
                                errorMessage = "Passwords do not match."
                                return@WorldBooksButton
                            }

                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                val result = authRepository.registerUser(
                                    fullName = cleanName,
                                    username = cleanUser,
                                    email = cleanEmail,
                                    password = password
                                )
                                isLoading = false
                                result.onSuccess {
                                    // Requirement 8: Return to login. Do NOT automatically open Home.
                                    onRegisterSuccess("Account created successfully. Please log in to continue.")
                                }.onFailure { e ->
                                    errorMessage = e.localizedMessage ?: "Failed to create account. Please try again."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign In",
                    color = IndigoLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
