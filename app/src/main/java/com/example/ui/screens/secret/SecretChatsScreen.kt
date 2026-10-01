package com.example.ui.screens.secret

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChatConversation
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.ui.components.AvatarImage
import com.example.ui.components.GlassCard
import com.example.ui.components.WorldBooksAppBar
import com.example.ui.components.WorldBooksEmptyState
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SecretChatsScreen(
    currentUserId: String,
    chatRepository: ChatRepository,
    authRepository: AuthRepository,
    onOpenChat: (String, String) -> Unit, // chatId, otherUserId
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("CHATS", "REQUESTS", "HIDDEN")

    var showSearchDialog by remember { mutableStateOf(false) }

    val rawConversations by chatRepository.getConversationsFlow(currentUserId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // Cache of user profiles
    val userProfileCache = remember { mutableStateMapOf<String, UserProfile>() }

    // Fetch other user profiles for conversations
    LaunchedEffect(rawConversations) {
        rawConversations.forEach { conv ->
            val otherUid = conv.participants.firstOrNull { it != currentUserId } ?: ""
            if (otherUid.isNotBlank() && !userProfileCache.containsKey(otherUid)) {
                val profile = authRepository.getUserProfile(otherUid)
                if (profile != null) {
                    userProfileCache[otherUid] = profile
                }
            }
        }
    }

    val activeChats = rawConversations.filter { !it.isHiddenFor(currentUserId) }
    val hiddenChats = rawConversations.filter { it.isHiddenFor(currentUserId) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WorldBooksAppBar(
                title = "Secret Chats 💎",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search users",
                            tint = CyanGlow
                        )
                    }
                }
            )

            // Tabs Row (CHATS | REQUESTS | HIDDEN)
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = SurfaceDark,
                contentColor = CyanGlow,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = CyanGlow
                    )
                }
            ) {
                tabs.forEachIndexed { index, tabName ->
                    val isSelected = selectedTabIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = tabName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CyanGlow else TextSecondary
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    // CHATS Tab
                    if (activeChats.isEmpty()) {
                        WorldBooksEmptyState(
                            icon = Icons.Default.Chat,
                            title = "No Secret Chats Yet",
                            description = "Search for a student using the + button to start an encrypted private discussion.",
                            actionButton = {
                                FloatingActionButton(
                                    onClick = { showSearchDialog = true },
                                    containerColor = IndigoLight,
                                    contentColor = BackgroundDark,
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Find Students", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        )
                    } else {
                        ConversationListView(
                            conversations = activeChats,
                            currentUserId = currentUserId,
                            userProfileCache = userProfileCache,
                            isHiddenTab = false,
                            onOpenChat = onOpenChat,
                            onHideChat = { chatId ->
                                coroutineScope.launch {
                                    chatRepository.hideChat(chatId, currentUserId)
                                    Toast.makeText(context, "Chat moved to Hidden", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onUnhideChat = {}
                        )
                    }
                }
                1 -> {
                    // REQUESTS Tab
                    WorldBooksEmptyState(
                        icon = Icons.Default.Chat,
                        title = "No Pending Requests",
                        description = "Direct chat requests and group invites from study peers will appear here."
                    )
                }
                2 -> {
                    // HIDDEN Tab
                    if (hiddenChats.isEmpty()) {
                        WorldBooksEmptyState(
                            icon = Icons.Default.VisibilityOff,
                            title = "No Hidden Chats",
                            description = "Chats you hide from the main screen will stay safely preserved here."
                        )
                    } else {
                        ConversationListView(
                            conversations = hiddenChats,
                            currentUserId = currentUserId,
                            userProfileCache = userProfileCache,
                            isHiddenTab = true,
                            onOpenChat = onOpenChat,
                            onHideChat = {},
                            onUnhideChat = { chatId ->
                                coroutineScope.launch {
                                    chatRepository.unhideChat(chatId, currentUserId)
                                    Toast.makeText(context, "Chat restored to Chats", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }

        // FAB to start new chat
        FloatingActionButton(
            onClick = { showSearchDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = CyanGlow,
            contentColor = BackgroundDark
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Secret Chat")
        }
    }

    if (showSearchDialog) {
        UserSearchDialog(
            currentUserId = currentUserId,
            chatRepository = chatRepository,
            onDismiss = { showSearchDialog = false },
            onStartChat = { chatId, user ->
                showSearchDialog = false
                userProfileCache[user.uid] = user
                onOpenChat(chatId, user.uid)
            }
        )
    }
}

@Composable
private fun ConversationListView(
    conversations: List<ChatConversation>,
    currentUserId: String,
    userProfileCache: Map<String, UserProfile>,
    isHiddenTab: Boolean,
    onOpenChat: (String, String) -> Unit,
    onHideChat: (String) -> Unit,
    onUnhideChat: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(conversations, key = { it.chatId }) { convo ->
            val otherUid = convo.participants.firstOrNull { it != currentUserId } ?: ""
            val otherProfile = userProfileCache[otherUid]
            val displayName = otherProfile?.displayName ?: if (otherUid.isNotBlank()) "Student" else "Chat"
            var menuExpanded by remember { mutableStateOf(false) }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (otherUid.isNotBlank()) {
                        onOpenChat(convo.chatId, otherUid)
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarImage(
                        photoUrl = otherProfile?.photoUrl,
                        displayName = displayName,
                        size = 50.dp,
                        showOnlineBadge = true,
                        isOnline = otherProfile?.isOnline == true
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = displayName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (convo.lastMessageTimestamp > 0L) {
                                val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault())
                                    .format(Date(convo.lastMessageTimestamp))
                                Text(
                                    text = timeStr,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = convo.lastMessage.ifBlank { "Tap to send a message" },
                            fontSize = 13.sp,
                            color = if (convo.lastMessage.isNotBlank()) TextSecondary else TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Options Menu (Hide / Unhide)
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Chat Options",
                                tint = TextMuted
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(SurfaceCard)
                        ) {
                            if (!isHiddenTab) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.VisibilityOff,
                                                contentDescription = null,
                                                tint = CyanGlow,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Hide Chat", color = TextPrimary)
                                        }
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onHideChat(convo.chatId)
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = CyanGlow,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Unhide Chat", color = TextPrimary)
                                        }
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onUnhideChat(convo.chatId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
