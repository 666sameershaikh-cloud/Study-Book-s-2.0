package com.example.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.ui.navigation.BottomNavItem
import com.example.ui.screens.chapters.ChaptersScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.notes.NotesScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun MainContainerScreen(
    currentUserId: String,
    currentUserProfile: UserProfile?,
    authRepository: AuthRepository,
    onNavigateToChapter: (String) -> Unit,
    onOpenSecretLock: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val navItems = listOf(
        BottomNavItem.HOME,
        BottomNavItem.LIBRARY,
        BottomNavItem.CHAPTERS,
        BottomNavItem.NOTES,
        BottomNavItem.PROFILE
    )

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 8.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    val icon = when (item) {
                        BottomNavItem.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                        BottomNavItem.LIBRARY -> if (isSelected) Icons.Filled.LocalLibrary else Icons.Outlined.LocalLibrary
                        BottomNavItem.CHAPTERS -> if (isSelected) Icons.Filled.Book else Icons.Outlined.Book
                        BottomNavItem.NOTES -> if (isSelected) Icons.Filled.EditNote else Icons.Outlined.EditNote
                        BottomNavItem.PROFILE -> if (isSelected) Icons.Filled.Person else Icons.Outlined.Person
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanGlow,
                            selectedTextColor = CyanGlow,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = IndigoDark.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    currentUser = currentUserProfile,
                    onNavigateToLibrary = { selectedTab = 1 },
                    onNavigateToChapter = onNavigateToChapter
                )
                1 -> LibraryScreen(
                    onNavigateToChapter = onNavigateToChapter
                )
                2 -> ChaptersScreen(
                    onNavigateToChapter = onNavigateToChapter,
                    onOpenSecretLock = onOpenSecretLock
                )
                3 -> NotesScreen(
                    currentUserId = currentUserId
                )
                4 -> ProfileScreen(
                    userProfile = currentUserProfile,
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    onLogout = onLogout
                )
            }
        }
    }
}
