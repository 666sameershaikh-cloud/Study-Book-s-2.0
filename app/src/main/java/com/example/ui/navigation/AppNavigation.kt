package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.library.BookReaderScreen
import com.example.ui.screens.main.MainContainerScreen
import com.example.ui.screens.profile.EditProfileScreen
import com.example.ui.screens.secret.ChatScreen
import com.example.ui.screens.secret.SecretChatsScreen
import com.example.ui.screens.secret.SecretLockScreen
import com.example.ui.screens.splash.SplashScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    chatRepository: ChatRepository
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val currentUserId by authRepository.currentUserIdFlow.collectAsStateWithLifecycle(initialValue = authRepository.currentUserId)
    val currentUserProfile by authRepository.currentUserProfileFlow.collectAsStateWithLifecycle(initialValue = authRepository.currentProfileSync)
    var registrationSuccessMessage by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    if (authRepository.isLoggedIn) {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                authRepository = authRepository,
                infoBannerMessage = registrationSuccessMessage,
                onLoginSuccess = {
                    registrationSuccessMessage = null
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    registrationSuccessMessage = null
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // Register Screen
        composable(Screen.Register.route) {
            RegisterScreen(
                authRepository = authRepository,
                onRegisterSuccess = { message ->
                    registrationSuccessMessage = message
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Main Container (5 bottom navigation tabs)
        composable(Screen.Main.route) {
            MainContainerScreen(
                currentUserId = currentUserId,
                currentUserProfile = currentUserProfile,
                authRepository = authRepository,
                onNavigateToChapter = { chapterId ->
                    navController.navigate(Screen.BookReader.createRoute(chapterId))
                },
                onOpenSecretLock = {
                    navController.navigate(Screen.SecretLock.route)
                },
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onLogout = {
                    authRepository.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Book Reader Screen
        composable(
            route = Screen.BookReader.route,
            arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
            BookReaderScreen(
                chapterId = chapterId,
                currentUserId = currentUserId,
                onBack = { navController.popBackStack() },
                onNavigateToNotes = { navController.popBackStack() }
            )
        }

        // Edit Profile Screen
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                currentProfile = currentUserProfile,
                authRepository = authRepository,
                onBack = { navController.popBackStack() },
                onProfileUpdated = {
                    coroutineScope.launch {
                        authRepository.getUserProfile(currentUserId)
                    }
                }
            )
        }

        // Secret Lock Screen
        composable(Screen.SecretLock.route) {
            SecretLockScreen(
                onUnlockSuccess = {
                    navController.navigate(Screen.SecretChats.route) {
                        popUpTo(Screen.SecretLock.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Secret Chats Screen
        composable(Screen.SecretChats.route) {
            SecretChatsScreen(
                currentUserId = currentUserId,
                chatRepository = chatRepository,
                authRepository = authRepository,
                onOpenChat = { chatId, otherUserId ->
                    navController.navigate(Screen.Chat.createRoute(chatId, otherUserId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Private Chat Screen
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("otherUserId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""

            ChatScreen(
                chatId = chatId,
                otherUserId = otherUserId,
                currentUserId = currentUserId,
                chatRepository = chatRepository,
                authRepository = authRepository,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
