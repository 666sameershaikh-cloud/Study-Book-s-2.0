package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object BookReader : Screen("book_reader/{chapterId}") {
        fun createRoute(chapterId: String): String = "book_reader/$chapterId"
    }
    object EditProfile : Screen("edit_profile")
    object SecretLock : Screen("secret_lock")
    object SecretChats : Screen("secret_chats")
    object Chat : Screen("chat/{chatId}/{otherUserId}") {
        fun createRoute(chatId: String, otherUserId: String): String = "chat/$chatId/$otherUserId"
    }
}

enum class BottomNavItem(
    val title: String,
    val selectedIcon: String,
    val unselectedIcon: String
) {
    HOME("Home", "home", "home_outlined"),
    LIBRARY("Library", "local_library", "local_library_outlined"),
    CHAPTERS("Chapters", "menu_book", "menu_book_outlined"),
    NOTES("Notes", "edit_note", "edit_note_outlined"),
    PROFILE("Profile", "person", "person_outlined")
}
