package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.DocumentEntity
import com.example.data.model.DocumentFileType
import com.example.util.FileStorageHelper
import com.example.util.PersianDateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DocumentViewerDialog(
    document: DocumentEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = document.title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${document.category.titleFa} • ${FileStorageHelper.formatFileSize(document.sizeBytes)}",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    IconButton(onClick = {
                        FileStorageHelper.shareFile(context, document.localPath, document.mimeType, document.title)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "اشتراک‌گذاری", tint = Color.White)
                    }
                    IconButton(onClick = {
                        FileStorageHelper.openExternalFile(context, document.localPath, document.mimeType)
                    }) {
                        Icon(Icons.Default.OpenInNew, contentDescription = "باز کردن خارجی", tint = Color.White)
                    }
                }

                // Content
                when (document.fileType) {
                    DocumentFileType.IMAGE -> ZoomableImageContent(document.localPath)
                    DocumentFileType.PDF -> OfflinePdfViewerContent(document.localPath)
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = "فایل ${document.fileType.titleFa}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "برای مشاهده فایل‌های Word و Excel از برنامه نصب‌شده روی گوشی استفاده کنید.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                                Button(onClick = {
                                    FileStorageHelper.openExternalFile(context, document.localPath, document.mimeType)
                                }) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("باز کردن با برنامه مرتبط")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomableImageContent(localPath: String) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 4f)
                    if (scale > 1f) {
                        offsetX += pan.x
                        offsetY += pan.y
                    } else {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = File(localPath),
            contentDescription = "پیش‌نمایش تصویر مدرک",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
        )
    }
}

@Composable
private fun OfflinePdfViewerContent(localPath: String) {
    var pageIndex by remember { mutableIntStateOf(0) }
    var pageCount by remember { mutableIntStateOf(0) }
    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }

    val rendererHolder = remember(localPath) {
        runCatching {
            val file = File(localPath)
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            pageCount = renderer.pageCount
            pfd to renderer
        }.getOrElse {
            errorText = "امکان باز کردن فایل PDF وجود ندارد."
            null
        }
    }

    DisposableEffect(localPath) {
        onDispose {
            runCatching {
                rendererHolder?.second?.close()
                rendererHolder?.first?.close()
            }
        }
    }

    LaunchedEffect(pageIndex, rendererHolder) {
        val renderer = rendererHolder?.second ?: return@LaunchedEffect
        if (renderer.pageCount <= 0) return@LaunchedEffect
        val safeIdx = pageIndex.coerceIn(0, renderer.pageCount - 1)
        val bmp = withContext(Dispatchers.IO) {
            renderer.openPage(safeIdx).use { page ->
                val targetWidth = (page.width * 2).coerceAtLeast(800)
                val targetHeight = (page.height * 2).coerceAtLeast(1100)
                val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            }
        }
        renderedBitmap = bmp
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF2B2F36))
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 3.5f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            when {
                errorText != null -> Text(errorText ?: "", color = Color.White)
                renderedBitmap == null -> CircularProgressIndicator(color = Color.White)
                else -> {
                    Image(
                        bitmap = renderedBitmap!!.asImageBitmap(),
                        contentDescription = "صفحه PDF",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                    )
                }
            }
        }

        if (pageCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    enabled = pageIndex > 0,
                    onClick = { pageIndex--; scale = 1f }
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "صفحه قبل")
                }
                Text(
                    text = PersianDateUtils.toPersianDigits("صفحه ${pageIndex + 1} از $pageCount"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    enabled = pageIndex < pageCount - 1,
                    onClick = { pageIndex++; scale = 1f }
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "صفحه بعد")
                }
            }
        }
    }
}
