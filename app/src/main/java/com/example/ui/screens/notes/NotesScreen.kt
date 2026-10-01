package com.example.ui.screens.notes

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.local.NoteEntity
import com.example.data.local.WorldBookDatabase
import com.example.ui.components.GlassCard
import com.example.ui.components.WorldBooksButton
import com.example.ui.components.WorldBooksEmptyState
import com.example.ui.components.WorldBooksTextField
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CyanGlow
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

@Composable
fun NotesScreen(
    currentUserId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val noteDao = remember { WorldBookDatabase.getInstance(context).noteDao() }
    val notes by noteDao.getNotesForUser(currentUserId).collectAsStateWithLifecycle(initialValue = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }

    val filteredNotes = notes.filter {
        val query = searchQuery.trim().lowercase()
        if (query.isEmpty()) true
        else it.title.lowercase().contains(query) || it.content.lowercase().contains(query) || it.subject.lowercase().contains(query)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Study Notes",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Personal revisions, formulas & lecture notes",
                    fontSize = 14.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search your notes...", color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = IndigoLight)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceDark,
                        focusedBorderColor = IndigoLight,
                        unfocusedBorderColor = BorderGlass,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (filteredNotes.isEmpty()) {
                WorldBooksEmptyState(
                    icon = Icons.Default.NoteAlt,
                    title = if (searchQuery.isBlank()) "No Study Notes Yet" else "No matching notes",
                    description = if (searchQuery.isBlank())
                        "Create notes while reading chapters to review formulas and concepts."
                    else "Try searching for a different topic or subject.",
                    actionButton = if (searchQuery.isBlank()) {
                        {
                            WorldBooksButton(
                                text = "Create First Note",
                                onClick = {
                                    noteToEdit = NoteEntity(userId = currentUserId)
                                    showEditDialog = true
                                }
                            )
                        }
                    } else null
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            onClick = {
                                noteToEdit = note
                                showEditDialog = true
                            }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (note.subject.isNotBlank()) {
                                        Text(
                                            text = note.subject,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanGlow
                                        )
                                    } else {
                                        Text(
                                            text = "General Study",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }

                                    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                                        .format(Date(note.updatedAt))
                                    Text(
                                        text = dateStr,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = note.title.ifBlank { "Untitled Note" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = note.content,
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            noteToEdit = note
                                            showEditDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Note",
                                            tint = IndigoLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { noteToDelete = note },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Note",
                                            tint = RoseError,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Add Note FAB
        FloatingActionButton(
            onClick = {
                noteToEdit = NoteEntity(userId = currentUserId)
                showEditDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = IndigoLight,
            contentColor = BackgroundDark
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Note")
        }
    }

    // Edit/Create Note Dialog
    if (showEditDialog && noteToEdit != null) {
        var editTitle by remember { mutableStateOf(noteToEdit?.title ?: "") }
        var editSubject by remember { mutableStateOf(noteToEdit?.subject ?: "") }
        var editContent by remember { mutableStateOf(noteToEdit?.content ?: "") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = SurfaceCard,
            title = {
                Text(
                    text = if (noteToEdit?.id == 0L) "New Study Note" else "Edit Note",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    WorldBooksTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = "Title",
                        placeholder = "e.g. Chapter 4 Key Formulas"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    WorldBooksTextField(
                        value = editSubject,
                        onValueChange = { editSubject = it },
                        label = "Subject",
                        placeholder = "e.g. Physics, History"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    WorldBooksTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        label = "Notes Content",
                        placeholder = "Write concepts, formulas, or summaries...",
                        singleLine = false,
                        modifier = Modifier.height(160.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editTitle.isBlank() && editContent.isBlank()) {
                        Toast.makeText(context, "Note cannot be empty", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    coroutineScope.launch {
                        val toSave = noteToEdit!!.copy(
                            userId = currentUserId,
                            title = editTitle.trim(),
                            subject = editSubject.trim(),
                            content = editContent.trim(),
                            updatedAt = System.currentTimeMillis()
                        )
                        noteDao.insertNote(toSave)
                        showEditDialog = false
                    }
                }) {
                    Text("Save", color = CyanGlow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            containerColor = SurfaceCard,
            title = { Text("Delete Note?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this study note?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        noteDao.deleteNote(noteToDelete!!)
                        noteToDelete = null
                        Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Delete", color = RoseError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
