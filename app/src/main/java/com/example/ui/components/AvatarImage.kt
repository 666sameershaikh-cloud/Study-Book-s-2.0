package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VioletAccent

@Composable
fun AvatarImage(
    photoUrl: String?,
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showOnlineBadge: Boolean = false,
    isOnline: Boolean = false
) {
    val cleanUrl = photoUrl?.trim().orEmpty()
    val initial = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (cleanUrl.isNotBlank()) {
            AsyncImage(
                model = cleanUrl,
                contentDescription = "$displayName profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(IndigoLight, VioletAccent)),
                        CircleShape
                    )
            )
        } else {
            // Default avatar with initials and gradient
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                SurfaceCard,
                                IndigoLight.copy(alpha = 0.35f)
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(IndigoLight.copy(alpha = 0.6f), VioletAccent.copy(alpha = 0.6f))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (displayName.isNotBlank()) {
                    Text(
                        text = initial,
                        color = TextPrimary,
                        fontSize = (size.value * 0.42).sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default avatar",
                        tint = TextMuted,
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
            }
        }

        if (showOnlineBadge) {
            val statusColor = if (isOnline) EmeraldSuccess else TextMuted
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .clip(CircleShape)
                    .background(statusColor)
                    .border(1.5.dp, BackgroundDark, CircleShape)
            )
        }
    }
}
