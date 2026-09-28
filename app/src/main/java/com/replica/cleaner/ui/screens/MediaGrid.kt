package com.replica.cleaner.ui.screens

import com.replica.cleaner.l10n.tr

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyColumnItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.replica.cleaner.core.formatBytes
import com.replica.cleaner.data.model.MediaFile
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.components.CleanerCheckbox
import com.replica.cleaner.ui.components.CleanerTopBar
import com.replica.cleaner.ui.components.PrimaryButton
import com.replica.cleaner.ui.theme.LocalCleanerColors

private enum class MediaSort { Date, Size, Name }

/**
 * One screen serving every media list in the app. `kind` picks the source:
 * a raw MediaStore collection, or one of the photo-analysis buckets.
 *
 * Photos layout matches AVG: Date filter chip, TOTAL row, 3-column grid with
 * size + checkbox under each thumbnail.
 */
@Composable
fun MediaGridScreen(
    vm: CleanerViewModel,
    kind: String,
    onBack: () -> Unit
) {
    val photos by vm.photos.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current

    var files by remember { mutableStateOf<List<MediaFile>>(emptyList()) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var loading by remember { mutableStateOf(true) }
    var freedMessage by remember { mutableStateOf<String?>(null) }
    var sort by remember {
        mutableStateOf(if (kind == "photos" || kind == "video") MediaSort.Date else MediaSort.Size)
    }
    var gridMode by remember { mutableStateOf(true) }

    LaunchedEffect(kind, photos) {
        loading = true
        files = when (kind) {
            "photos" -> vm.images(limit = 5000)
            "video" -> vm.videos()
            "audio" -> vm.audio()
            "others" -> vm.otherFiles()
            "similar" -> photos?.similarFlat.orEmpty()
            "bad" -> photos?.badQuality.orEmpty()
            "screenshots" -> photos?.screenshots.orEmpty()
            "old" -> photos?.old.orEmpty()
            "optimizable" -> photos?.optimizable.orEmpty()
            else -> emptyList()
        }
        loading = false
    }

    val sorted = remember(files, sort) {
        when (sort) {
            MediaSort.Date -> files.sortedByDescending { it.dateAdded }
            MediaSort.Size -> files.sortedByDescending { it.sizeBytes }
            MediaSort.Name -> files.sortedBy { it.name.lowercase() }
        }
    }

    val title = when (kind) {
        "photos" -> "Photos"
        "video" -> "Video"
        "audio" -> "Audio"
        "others" -> tr("Other files")
        "similar" -> tr("Similar photos")
        "bad" -> tr("Bad quality")
        "screenshots" -> tr("Sensitive photos")
        "old" -> tr("Old photos")
        "optimizable" -> tr("Optimizable images")
        else -> "Media"
    }

    val sortLabel = when (sort) {
        MediaSort.Date -> tr("Date")
        MediaSort.Size -> tr("Size")
        MediaSort.Name -> tr("Name")
    }

    val useGrid = gridMode && kind !in setOf("audio", "others")
    val selectedFiles = sorted.filter { it.uri.toString() in selected }
    val showAvgChrome = kind in setOf("photos", "video", "audio", "others")

    fun cycleSort() {
        sort = when (sort) {
            MediaSort.Date -> MediaSort.Size
            MediaSort.Size -> MediaSort.Name
            MediaSort.Name -> MediaSort.Date
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(title, onBack = onBack, centered = true) {
            if (showAvgChrome && kind !in setOf("audio", "others")) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { gridMode = !gridMode },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (gridMode) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                        contentDescription = tr("Toggle view"),
                        tint = colors.textSecondary
                    )
                }
            }
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable { cycleSort() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SwapVert, tr("Change sorting"), tint = colors.textSecondary)
            }
        }

        if (showAvgChrome) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { cycleSort() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = null,
                        tint = Color(0xFF07281A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = sortLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF07281A)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${sorted.size} TOTAL (${formatBytes(sorted.sumOf { it.sizeBytes })})",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (selected.size == sorted.size && sorted.isNotEmpty()) tr("DESELECT ALL") else tr("SELECT ALL"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable {
                    selected = if (selected.size == sorted.size) emptySet()
                    else sorted.map { it.uri.toString() }.toSet()
                }
            )
        }

        when {
            loading -> Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(tr("Loading…"), color = colors.textSecondary)
                }
            }

            sorted.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                Text(
                    text = freedMessage ?: tr("Nothing found here."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }

            useGrid -> LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(sorted, key = { it.uri.toString() }) { file ->
                    MediaGridCell(
                        file = file,
                        checked = file.uri.toString() in selected,
                        sizeBelow = showAvgChrome,
                        isVideo = kind == "video",
                        onToggle = {
                            val id = file.uri.toString()
                            selected = if (id in selected) selected - id else selected + id
                        }
                    )
                }
            }

            else -> LazyColumn(Modifier.weight(1f)) {
                lazyColumnItems(sorted, key = { it.uri.toString() }) { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val id = file.uri.toString()
                                selected = if (id in selected) selected - id else selected + id
                            }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OtherFileThumb(
                            file = file,
                            kind = kind,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatBytes(file.sizeBytes),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        CleanerCheckbox(
                            checked = file.uri.toString() in selected,
                            onCheckedChange = null
                        )
                    }
                }
            }
        }

        if (selectedFiles.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.card)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Text(
                    text = "${selectedFiles.size} SELECTED (${formatBytes(selectedFiles.sumOf { it.sizeBytes })})",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
                Spacer(Modifier.height(8.dp))
                PrimaryButton(
                    text = tr("DELETE SELECTED"),
                    onClick = {
                        vm.deleteMedia(selectedFiles) { freed ->
                            freedMessage = "Freed ${formatBytes(freed)}"
                        }
                        files = files - selectedFiles.toSet()
                        selected = emptySet()
                    }
                )
            }
        }
    }
}

@Composable
private fun MediaGridCell(
    file: MediaFile,
    checked: Boolean,
    sizeBelow: Boolean,
    isVideo: Boolean = false,
    onToggle: () -> Unit
) {
    val colors = LocalCleanerColors.current
    val context = LocalContext.current
    val imageModel = remember(file.uri, isVideo) {
        ImageRequest.Builder(context)
            .data(file.uri)
            .crossfade(true)
            .apply {
                if (isVideo) {
                    // Coil needs VideoFrameDecoder to render a still from video URIs.
                    decoderFactory(VideoFrameDecoder.Factory())
                    videoFrameMillis(1_000)
                }
            }
            .build()
    }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onToggle)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(colors.cardHigh)
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = file.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (isVideo) {
                // AVG places a translucent play affordance over every video thumb.
                Icon(
                    Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                )
            }
            if (!sizeBelow) {
                Text(
                    text = formatBytes(file.sizeBytes),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
                CleanerCheckbox(
                    checked = checked,
                    onCheckedChange = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                )
            }
        }
        if (sizeBelow) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatBytes(file.sizeBytes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                CleanerCheckbox(
                    checked = checked,
                    onCheckedChange = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private val IMAGE_EXTS = setOf("jpg", "jpeg", "png", "webp", "gif", "heic", "heif", "bmp")
private val VIDEO_EXTS = setOf("mp4", "mkv", "avi", "mov", "webm", "3gp", "m4v")
private val AUDIO_EXTS = setOf("mp3", "m4a", "aac", "flac", "wav", "ogg", "wma", "opus")
private val ARCHIVE_EXTS = setOf("zip", "rar", "7z", "tar", "gz")
private val DOC_EXTS = setOf("txt", "doc", "docx", "rtf", "md", "csv", "xls", "xlsx", "ppt", "pptx")

private fun fileExtension(name: String): String =
    name.substringAfterLast('.', missingDelimiterValue = "").lowercase()

/**
 * Preview for list rows (Others / Audio / list-mode media):
 * real thumbnail for images & videos, typed Material icon otherwise.
 */
@Composable
private fun OtherFileThumb(
    file: MediaFile,
    kind: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalCleanerColors.current
    val context = LocalContext.current
    val ext = remember(file.name) { fileExtension(file.name) }
    val showImage = ext in IMAGE_EXTS
    val showVideo = ext in VIDEO_EXTS || kind == "video"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.cardHigh),
        contentAlignment = Alignment.Center
    ) {
        when {
            showImage || showVideo -> {
                AsyncImage(
                    model = remember(file.uri, showVideo) {
                        ImageRequest.Builder(context)
                            .data(file.uri)
                            .crossfade(true)
                            .apply {
                                if (showVideo) {
                                    decoderFactory(VideoFrameDecoder.Factory())
                                    videoFrameMillis(1_000)
                                }
                            }
                            .build()
                    },
                    contentDescription = file.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (showVideo) {
                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            else -> {
                val (icon, tint) = fileTypeIcon(ext)
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun fileTypeIcon(ext: String): Pair<ImageVector, Color> {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = LocalCleanerColors.current.textSecondary
    return when (ext) {
        "apk" -> Icons.Default.Android to primary
        "pdf" -> Icons.Default.PictureAsPdf to Color(0xFFE57373)
        "url", "html", "htm" -> Icons.Default.Link to Color(0xFF64B5F6)
        in ARCHIVE_EXTS -> Icons.Default.FolderZip to Color(0xFFFFB74D)
        in AUDIO_EXTS -> Icons.Default.AudioFile to Color(0xFFBA68C8)
        in VIDEO_EXTS -> Icons.Default.VideoFile to Color(0xFF4FC3F7)
        in IMAGE_EXTS -> Icons.Default.Image to primary
        in DOC_EXTS -> Icons.Default.Description to Color(0xFF81C784)
        else -> Icons.AutoMirrored.Filled.InsertDriveFile to secondary
    }
}
