package com.opentube.ui.screens.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.opentube.ui.screens.player.Comment

@Composable
fun VideoInfoSection(
    title: String,
    description: String,
    uploader: String,
    uploaderAvatar: String,
    uploaderVerified: Boolean,
    views: Long,
    likes: Long,
    uploadDate: String,
    subscriberCount: Long,
    isFavorite: Boolean,
    isSubscribed: Boolean,
    onFavoriteClick: () -> Unit,
    onSubscribeClick: () -> Unit,
    onChannelClick: () -> Unit,
    onShareClick: () -> Unit,
    onCommentsClick: () -> Unit,
    commentCount: Long = 0,
    featuredComment: Comment? = null,
    modifier: Modifier = Modifier
) {
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Title Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 26.sp
                ),
                maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 2,
                overflow = if (isDescriptionExpanded) TextOverflow.Visible else TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            
            // Views, Date and Expandable Description Area
            Card(
                onClick = { isDescriptionExpanded = !isDescriptionExpanded },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${formatViews(views)} vistas • ${formatDate(uploadDate)}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!isDescriptionExpanded) {
                            Text(
                                text = " ...más",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    AnimatedVisibility(visible = isDescriptionExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            val uriHandler = LocalUriHandler.current
                            val annotatedDescription = parseHtmlDescription(description)
                            
                            ClickableText(
                                text = annotatedDescription,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                ),
                                onClick = { offset ->
                                    annotatedDescription.getStringAnnotations("URL", offset, offset)
                                        .firstOrNull()?.let { annotation ->
                                            try { uriHandler.openUri(annotation.item) }
                                            catch (e: Exception) { }
                                        }
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Mostrar menos",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Channel Info Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onChannelClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = uploaderAvatar,
                    contentDescription = "Channel avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uploader,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (uploaderVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "${formatSubscribers(subscriberCount)} suscriptores",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Subscribe Button
            Button(
                onClick = onSubscribeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSubscribed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.onSurface,
                    contentColor = if (isSubscribed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface
                ),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = if (isSubscribed) "Suscrito" else "Suscribirse",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (isSubscribed) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Notificaciones",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Action Buttons (Scrollable Row)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                // Like / Dislike joined pill
                Row(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { /* Like Action */ }
                            .padding(horizontal = 12.dp)
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ThumbUp,
                            contentDescription = "Like",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatLikes(likes),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Divider(
                        modifier = Modifier
                            .fillMaxHeight(0.6f)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Row(
                        modifier = Modifier
                            .clickable { /* Dislike Action */ }
                            .padding(horizontal = 12.dp)
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ThumbDown,
                            contentDescription = "Dislike",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            item {
                PillButton(
                    icon = Icons.Outlined.Share,
                    text = "Compartir",
                    onClick = onShareClick
                )
            }
            
            item {
                PillButton(
                    icon = Icons.Outlined.AutoAwesome,
                    text = "Remix",
                    onClick = { /* TODO */ }
                )
            }
            
            item {
                PillButton(
                    icon = if (isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    text = "Guardar",
                    onClick = onFavoriteClick
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // 4. Featured Comment Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(onClick = onCommentsClick),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Comentarios",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatViews(commentCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.UnfoldMore,
                        contentDescription = "Expandir comentarios",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (featuredComment != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = featuredComment.authorAvatar,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val cleanText = parseHtmlDescription(featuredComment.text)
                        Text(
                            text = cleanText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun PillButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun formatViews(views: Long): String {
    return when {
        views >= 1_000_000 -> String.format("%.1f M", views / 1_000_000.0)
        views >= 1_000 -> String.format("%.1f k", views / 1_000.0)
        else -> views.toString()
    }
}

private fun formatLikes(likes: Long): String {
    if (likes == 0L) return "Me gusta"
    return when {
        likes >= 1_000_000 -> String.format("%.1f M", likes / 1_000_000.0)
        likes >= 1_000 -> String.format("%.1f k", likes / 1_000.0)
        else -> likes.toString()
    }
}

private fun formatSubscribers(count: Long): String {
    return when {
        count >= 1_000_000 -> String.format("%.1f M", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1f k", count / 1_000.0)
        else -> count.toString()
    }
}

fun formatDate(dateString: String): String {
    return try {
        if (dateString.isEmpty()) return ""
        if (dateString.contains("Hace")) return dateString
        
        if (dateString.contains("T")) {
            val datePart = dateString.substringBefore("T")
            val parts = datePart.split("-")
            if (parts.size == 3) {
                "${parts[2]}/${parts[1]}/${parts[0]}"
            } else {
                datePart.replace("-", "/")
            }
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

fun parseHtmlDescription(html: String) = buildAnnotatedString {
    if (html.isEmpty()) {
        append("Sin descripción")
        return@buildAnnotatedString
    }
    
    var text = html
        .replace("<br>", "\n")
        .replace("<br/>", "\n")
        .replace("<br />", "\n")
    
    val linkPattern = """<a\s+(?:[^>]*?\s+)?href=(["'])(.*?)\1[^>]*>(.*?)</a>""".toRegex(RegexOption.IGNORE_CASE)
    
    var lastIndex = 0
    linkPattern.findAll(text).forEach { matchResult ->
        val (_, url, linkText) = matchResult.destructured
        
        val beforeLink = text.substring(lastIndex, matchResult.range.first)
        append(beforeLink.replace(Regex("<[^>]+>"), ""))
        
        pushStringAnnotation(tag = "URL", annotation = url)
        withStyle(
            style = SpanStyle(
                color = Color(0xFFAAAAAA),
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(linkText)
        }
        pop()
        
        lastIndex = matchResult.range.last + 1
    }
    
    if (lastIndex < text.length) {
        val remaining = text.substring(lastIndex).replace(Regex("<[^>]+>"), "")
        append(remaining)
    }
}
