package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.WorldBooksTheme
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val authRepository by lazy { AuthRepository() }
    private val chatRepository by lazy { ChatRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WorldBooksTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    AppNavigation(
                        authRepository = authRepository,
                        chatRepository = chatRepository
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            authRepository.updateOnlineStatus(true)
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            authRepository.updateOnlineStatus(false)
        }
    }
}
