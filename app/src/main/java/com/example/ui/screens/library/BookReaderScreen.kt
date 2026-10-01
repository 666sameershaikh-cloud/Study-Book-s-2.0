package com.example.ui.screens.library

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.local.NoteEntity
import com.example.data.local.WorldBookDatabase
import com.example.data.repository.LibraryRepository
import com.example.ui.components.GlassCard
import com.example.ui.components.WorldBooksAppBar
import com.example.ui.components.WorldBooksButton
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun BookReaderScreen(
    chapterId: String,
    currentUserId: String,
    onBack: () -> Unit,
    onNavigateToNotes: () -> Unit
) {
    val chapter = LibraryRepository.findChapterById(chapterId)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val noteDao = remember { WorldBookDatabase.getInstance(context).noteDao() }

    var fontSizeSp by remember { mutableFloatStateOf(16f) }
    var isCompleted by remember { mutableStateOf(false) }

    if (chapter == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Chapter not found", color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            WorldBooksButton(text = "Go Back", onClick = onBack)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WorldBooksAppBar(
                title = chapter.subjectName,
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        fontSizeSp = if (fontSizeSp >= 22f) 14f else fontSizeSp + 2f
                    }) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Adjust font size",
                            tint = CyanGlow
                        )
                    }
                    IconButton(onClick = {
                        isCompleted = !isCompleted
                        Toast.makeText(
                            context,
                            if (isCompleted) "Marked as completed! 🎉" else "Marked uncompleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.BookmarkBorder,
                            contentDescription = "Toggle completion",
                            tint = if (isCompleted) EmeraldSuccess else TextMuted
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(bottom = 80.dp)
            ) {
                // Chapter Header Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(IndigoLight.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "CHAPTER ${chapter.number}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanGlow
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "10 min read",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = chapter.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = chapter.summary,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Chapter Reading Content
                Text(
                    text = chapter.content,
                    fontSize = fontSizeSp.sp,
                    color = TextPrimary,
                    lineHeight = (fontSizeSp * 1.6f).sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Mark Completed Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isCompleted = !isCompleted
                        Toast.makeText(
                            context,
                            if (isCompleted) "Chapter completed!" else "Progress updated",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = if (isCompleted) EmeraldSuccess else IndigoLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isCompleted) "Completed" else "Mark Chapter as Complete",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Floating Action Button to Take a Note
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    val note = NoteEntity(
                        userId = currentUserId,
                        title = "Notes: ${chapter.title}",
                        content = "Key takeaways from ${chapter.subjectName} Chapter ${chapter.number}:\n\n- ",
                        subject = chapter.subjectName,
                        chapterTitle = chapter.title
                    )
                    noteDao.insertNote(note)
                    Toast.makeText(context, "Note created for this chapter!", Toast.LENGTH_SHORT).show()
                    onNavigateToNotes()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = IndigoLight,
            contentColor = BackgroundDark
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.EditNote, contentDescription = "Take Note")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Take Note", fontWeight = FontWeight.Bold)
            }
        }
    }
}
