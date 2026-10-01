package com.example.ui.screens.secret

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.platform.testTag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.ChatMessage
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.ui.components.AvatarImage
import com.example.ui.components.GlassCard
import com.example.ui.components.ImageViewerDialog
import com.example.ui.components.WorldBooksButton
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.ChatBubbleReceived
import com.example.ui.theme.ChatBubbleSent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.RoseError
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    otherUserId: String,
    currentUserId: String,
    chatRepository: ChatRepository,
    authRepository: AuthRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Android back handler to return cleanly to Secret Chats screen (NOT Home!)
    BackHandler {
        onBack()
    }

    var messageInput by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var otherUserProfile by remember { mutableStateOf<UserProfile?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    // Media and Dialogs state
    var showMediaSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var previewPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingPhoto by remember { mutableStateOf(false) }

    // Load other user's profile safely
    LaunchedEffect(otherUserId) {
        if (otherUserId.isNotBlank()) {
            otherUserProfile = authRepository.getUserProfile(otherUserId)
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            previewPhotoUri = uri
        }
    }

    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun createCameraTempUri(context: Context): Uri {
        val tempFile = File.createTempFile(
            "wb_cam_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        ).apply {
            createNewFile()
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraUri != null) {
            previewPhotoUri = pendingCameraUri
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val uri = createCameraTempUri(context)
                pendingCameraUri = uri
                takePictureLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to capture photos.", Toast.LENGTH_SHORT).show()
        }
    }

    // Observe real-time messages for this deterministic chatId
    val messages by chatRepository.getMessagesFlow(chatId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        val textToSend = messageInput.trim()
        if (textToSend.isBlank() || isSending) return

        isSending = true
        coroutineScope.launch {
            val result = chatRepository.sendTextMessage(
                chatId = chatId,
                senderId = currentUserId,
                receiverId = otherUserId,
                text = textToSend
            )
            isSending = false
            result.onSuccess {
                messageInput = ""
                // Stay inside the chat! Scroll to bottom
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }.onFailure { err ->
                Toast.makeText(context, err.message ?: "Message failed to send.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Chat Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark.copy(alpha = 0.95f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Secret Chats",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                AvatarImage(
                    photoUrl = otherUserProfile?.photoUrl,
                    displayName = otherUserProfile?.displayName ?: "User",
                    size = 40.dp,
                    showOnlineBadge = true,
                    isOnline = otherUserProfile?.isOnline == true
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = otherUserProfile?.displayName ?: "Student",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (otherUserProfile?.isOnline == true) EmeraldSuccess else TextMuted)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (otherUserProfile?.isOnline == true) "Online" else "Offline",
                            fontSize = 11.sp,
                            color = if (otherUserProfile?.isOnline == true) EmeraldSuccess else TextMuted
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Chat Options",
                            tint = TextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(SurfaceCard)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Hide Conversation", color = TextPrimary) },
                            onClick = {
                                showMenu = false
                                coroutineScope.launch {
                                    chatRepository.hideChat(chatId, currentUserId)
                                    Toast.makeText(context, "Chat moved to Hidden", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Block User", color = RoseError) },
                            onClick = {
                                showMenu = false
                                coroutineScope.launch {
                                    chatRepository.blockUser(currentUserId, otherUserId)
                                    Toast.makeText(context, "User blocked", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }
                            }
                        )
                    }
                }
            }

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("chat_messages_list")
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Encryption notice badge
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceDark.copy(alpha = 0.7f))
                                .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = CyanGlow,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "End-to-end encrypted private study chat",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📚", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No messages yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Say hello to start the discussion! 👋",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                items(messages, key = { it.messageId.ifBlank { it.timestamp.toString() } }) { message ->
                    val isMe = message.senderId == currentUserId
                    MessageBubble(
                        message = message,
                        isMe = isMe,
                        onImageClick = { fullScreenImageUrl = message.mediaUrl }
                    )
                }
            }

            // Message Composer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // + button for photo messaging
                IconButton(
                    onClick = { showMediaSheet = true },
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("attach_button")
                        .background(SurfaceCard, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Attach photo",
                        tint = CyanGlow
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text input with normal keyboard and emoji support
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("Message...", color = TextMuted, fontSize = 14.sp) },
                    singleLine = false,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                        focusedBorderColor = CyanGlow,
                        unfocusedBorderColor = BorderGlass,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = CyanGlow
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                IconButton(
                    onClick = { sendMessage() },
                    enabled = messageInput.isNotBlank() && !isSending,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("send_button")
                        .background(
                            if (messageInput.isNotBlank()) CyanGlow else SurfaceCard,
                            CircleShape
                        )
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            color = BackgroundDark,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            tint = if (messageInput.isNotBlank()) BackgroundDark else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Media Picker Bottom Sheet
    if (showMediaSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMediaSheet = false },
            sheetState = sheetState,
            containerColor = SurfaceCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Send Media",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(18.dp))

                // Camera Capture Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showMediaSheet = false
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                try {
                                    val uri = createCameraTempUri(context)
                                    pendingCameraUri = uri
                                    takePictureLauncher.launch(uri)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open camera: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Take photo with camera",
                        tint = CyanGlow,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Take Photo with Camera",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Capture notes, study material, or questions",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showMediaSheet = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Choose from gallery",
                        tint = CyanGlow,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Choose Photo from Gallery",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Pick screenshot, diagram, or textbook page",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }

    // Photo Preview & Send Confirmation Dialog
    if (previewPhotoUri != null) {
        AlertDialog(
            onDismissRequest = {
                if (!isUploadingPhoto) previewPhotoUri = null
            },
            containerColor = SurfaceCard,
            title = {
                Text("Send Photo?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = previewPhotoUri,
                        contentDescription = "Photo preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    if (isUploadingPhoto) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = CyanGlow, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Uploading photo...", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uri = previewPhotoUri ?: return@TextButton
                        isUploadingPhoto = true
                        coroutineScope.launch {
                            val result = chatRepository.sendImageMessage(
                                chatId = chatId,
                                senderId = currentUserId,
                                receiverId = otherUserId,
                                imageUri = uri
                            )
                            isUploadingPhoto = false
                            previewPhotoUri = null
                            result.onSuccess {
                                // Stay in chat!
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }.onFailure { e ->
                                Toast.makeText(
                                    context,
                                    e.message ?: "Photo upload failed. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    enabled = !isUploadingPhoto
                ) {
                    Text("Send", color = CyanGlow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { previewPhotoUri = null },
                    enabled = !isUploadingPhoto
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Full-screen Image Viewer
    if (fullScreenImageUrl != null) {
        ImageViewerDialog(
            imageUrl = fullScreenImageUrl!!,
            onDismiss = { fullScreenImageUrl = null }
        )
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    onImageClick: () -> Unit
) {
    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    val bubbleBg = if (isMe) ChatBubbleSent else ChatBubbleReceived
    val align = if (isMe) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(bubbleBg)
                .border(
                    width = 1.dp,
                    color = if (isMe) IndigoLight.copy(alpha = 0.4f) else BorderGlass,
                    shape = bubbleShape
                )
                .padding(if (message.type == "image") 4.dp else 12.dp)
        ) {
            Column {
                if (message.type == "image" && message.mediaUrl.isNotBlank()) {
                    AsyncImage(
                        model = message.mediaUrl,
                        contentDescription = "Received photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onImageClick() }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (message.text.isNotBlank() && (message.type != "image" || message.text != "📷 Photo")) {
                    Text(
                        text = message.text,
                        fontSize = 15.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Timestamp & Delivery status
                val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault())
                    .format(Date(message.timestamp))
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Delivered",
                            tint = CyanGlow,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}
