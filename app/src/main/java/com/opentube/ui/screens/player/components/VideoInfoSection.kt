package com.opentube.ui.screens.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun VideoInfoSection(
    title: String,
    uploader: String,
    uploaderAvatar: String,
    uploaderVerified: Boolean,
    views: Long,
    likes: Long,
    uploadDate: String,
    description: String,
    subscriberCount: Long,
    isFavorite: Boolean,
    isSubscribed: Boolean,
    onFavoriteClick: () -> Unit,
    onSubscribeClick: () -> Unit,
    onChannelClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Views and date
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatViews(views)} vistas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " • ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(uploadDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Channel info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onChannelClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Channel avatar
                AsyncImage(
                    model = uploaderAvatar,
                    contentDescription = "Channel avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Channel name and subscriber count
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uploader,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (uploaderVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = "${formatSubscribers(subscriberCount)} suscriptores",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Subscribe button
                FilledTonalButton(
                    onClick = onSubscribeClick,
                    colors = if (isSubscribed) {
                        ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    } else {
                        ButtonDefaults.filledTonalButtonColors()
                    }
                ) {
                    Text(if (isSubscribed) "Suscrito" else "Suscribirse")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Like button (solo mostrar si hay likes disponibles)
                if (likes > 0) {
                    AssistChip(
                        onClick = {},
                        label = { Text(formatLikes(likes)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Like"
                            )
                        }
                    )
                }

                // Share button
                AssistChip(
                    onClick = onShareClick,
                    label = { Text("Compartir") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                )

                // Favorite button
                AssistChip(
                    onClick = onFavoriteClick,
                    label = { Text(if (isFavorite) "Guardado" else "Guardar") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Icon(
                            imageVector = if (isDescriptionExpanded) 
                                Icons.Default.KeyboardArrowUp 
                            else 
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isDescriptionExpanded) "Collapse" else "Expand"
                        )
                    }

                    AnimatedVisibility(visible = isDescriptionExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val uriHandler = LocalUriHandler.current
                            val annotatedDescription = parseHtmlDescription(description)
                            
                            ClickableText(
                                text = annotatedDescription,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                onClick = { offset ->
                                    annotatedDescription.getStringAnnotations(
                                        tag = "URL",
                                        start = offset,
                                        end = offset
                                    ).firstOrNull()?.let { annotation ->
                                        try {
                                            uriHandler.openUri(annotation.item)
                                        } catch (e: Exception) {
                                            android.util.Log.e("VideoInfo", "Error opening URL", e)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatViews(views: Long): String {
    return when {
        views >= 1_000_000 -> String.format("%.1fM", views / 1_000_000.0)
        views >= 1_000 -> String.format("%.1fK", views / 1_000.0)
        else -> views.toString()
    }
}

private fun formatLikes(likes: Long): String {
    return when {
        likes >= 1_000_000 -> String.format("%.1fM", likes / 1_000_000.0)
        likes >= 1_000 -> String.format("%.1fK", likes / 1_000.0)
        else -> likes.toString()
    }
}

private fun formatSubscribers(count: Long): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

private fun formatDate(dateString: String): String {
    // Simple formatting - can be improved with actual date parsing
    return try {
        if (dateString.contains("T")) {
            val date = dateString.split("T")[0]
            date.replace("-", "/")
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

@Composable
private fun parseHtmlDescription(html: String) = buildAnnotatedString {
    if (html.isEmpty()) {
        append("Sin descripción")
        return@buildAnnotatedString
    }
    
    // Reemplazar <br> con saltos de línea
    var text = html
        .replace("<br>", "\n")
        .replace("<br/>", "\n")
        .replace("<br />", "\n")
    
    // Patrón para encontrar links <a href="...">texto</a>
    val linkPattern = """<a\s+(?:[^>]*?\s+)?href=(["'])(.*?)\1[^>]*>(.*?)</a>""".toRegex(RegexOption.IGNORE_CASE)
    
    var lastIndex = 0
    linkPattern.findAll(text).forEach { matchResult ->
        val (_, url, linkText) = matchResult.destructured
        
        // Agregar texto antes del link
        val beforeLink = text.substring(lastIndex, matchResult.range.first)
        append(beforeLink.replace(Regex("<[^>]+>"), "")) // Limpiar otros tags HTML
        
        // Agregar el link clickeable
        pushStringAnnotation(tag = "URL", annotation = url)
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(linkText)
        }
        pop()
        
        lastIndex = matchResult.range.last + 1
    }
    
    // Agregar el texto restante
    if (lastIndex < text.length) {
        val remaining = text.substring(lastIndex).replace(Regex("<[^>]+>"), "")
        append(remaining)
    }
}
