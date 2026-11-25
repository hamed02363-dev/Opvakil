package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CaseStatus
import com.example.data.model.DocumentCategory
import com.example.data.model.StageStatus
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusAmberBg
import com.example.ui.theme.StatusCrimson
import com.example.ui.theme.StatusCrimsonBg
import com.example.ui.theme.StatusEmerald
import com.example.ui.theme.StatusEmeraldBg
import com.example.ui.theme.StatusIndigo
import com.example.ui.theme.StatusIndigoBg
import com.example.util.FileStorageHelper
import com.example.util.PersianDateUtils
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VakilTopBar(
    title: String,
    subtitle: String? = null,
    onMenuClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("topbar_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                }
            } else if (onMenuClick != null) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.testTag("topbar_menu_button")
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "منو")
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun StatusPill(text: String, fgColor: Color, bgColor: Color, modifier: Modifier = Modifier) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = fgColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CaseStatusBadge(status: CaseStatus) {
    val (fg, bg) = when (status) {
        CaseStatus.CLOSED -> StatusEmerald to StatusEmeraldBg
        CaseStatus.DISMISSED -> Color(0xFF546E7A) to Color(0xFFECEFF1)
        CaseStatus.IN_ENFORCEMENT -> StatusAmber to StatusAmberBg
        CaseStatus.APPEAL -> StatusCrimson to StatusCrimsonBg
        CaseStatus.IN_COURT, CaseStatus.IN_PROSECUTOR -> StatusIndigo to StatusIndigoBg
        else -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    }
    StatusPill(text = status.titleFa, fgColor = fg, bgColor = bg)
}

@Composable
fun StageStatusBadge(status: StageStatus) {
    val (fg, bg) = when (status) {
        StageStatus.COMPLETED -> StatusEmerald to StatusEmeraldBg
        StageStatus.IN_PROGRESS -> StatusIndigo to StatusIndigoBg
        StageStatus.PENDING_ACTION -> StatusAmber to StatusAmberBg
        StageStatus.CANCELLED -> StatusCrimson to StatusCrimsonBg
    }
    StatusPill(text = status.titleFa, fgColor = fg, bgColor = bg)
}

@Composable
fun StatKpiCard(
    title: String,
    value: Int,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = PersianDateUtils.toPersianDigits(value.toString()),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyStateBox(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownSelectorField(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = optionLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(optionLabel(item)) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("حذف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

@Composable
fun ShamsiDatePickerField(
    label: String,
    millis: Long?,
    includeTime: Boolean = false,
    onDatePicked: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val displayValue = if (millis == null) "انتخاب نشده" else {
        if (includeTime) PersianDateUtils.formatShamsiDateTime(millis)
        else PersianDateUtils.formatShamsiLong(millis)
    }

    OutlinedTextField(
        value = displayValue,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = label)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    )

    if (showDialog) {
        ShamsiDatePickerDialog(
            initialMillis = millis ?: System.currentTimeMillis(),
            includeTime = includeTime,
            onConfirm = {
                onDatePicked(it)
                showDialog = false
            },
            onClear = {
                onDatePicked(null)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun ShamsiDatePickerDialog(
    initialMillis: Long,
    includeTime: Boolean,
    onConfirm: (Long) -> Unit,
    onClear: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val initialJalali = remember(initialMillis) { PersianDateUtils.millisToJalali(initialMillis) }
    val initialCal = remember(initialMillis) {
        Calendar.getInstance().apply { timeInMillis = initialMillis }
    }

    var year by remember { mutableIntStateOf(initialJalali.year) }
    var month by remember { mutableIntStateOf(initialJalali.month) }
    var day by remember { mutableIntStateOf(initialJalali.day) }
    var hour by remember { mutableIntStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(initialCal.get(Calendar.MINUTE)) }

    val maxDays = PersianDateUtils.daysInJalaliMonth(year, month)
    if (day > maxDays) day = maxDays

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "انتخاب تاریخ شمسی",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))

                // Year & Month Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { year-- }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "سال قبل")
                        }
                        Text(
                            text = PersianDateUtils.toPersianDigits(year.toString()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { year++ }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "سال بعد")
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            if (month > 1) month-- else {
                                month = 12
                                year--
                            }
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "ماه قبل")
                        }
                        Text(
                            text = PersianDateUtils.monthNames[month - 1],
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = {
                            if (month < 12) month++ else {
                                month = 1
                                year++
                            }
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "ماه بعد")
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(210.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(maxDays) { idx ->
                        val d = idx + 1
                        val selected = d == day
                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                )
                                .clickable { day = d },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = PersianDateUtils.toPersianDigits(d.toString()),
                                color = if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                if (includeTime) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ساعت:", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = { hour = (hour + 1) % 24 }) {
                            Text(PersianDateUtils.toPersianDigits(String.format("%02d", hour)))
                        }
                        Text(" : ", fontWeight = FontWeight.Bold)
                        OutlinedButton(onClick = { minute = (minute + 15) % 60 }) {
                            Text(PersianDateUtils.toPersianDigits(String.format("%02d", minute)))
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (onClear != null) {
                        TextButton(onClick = onClear) { Text("پاک کردن") }
                    }
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Spacer(Modifier.width(6.dp))
                    Button(onClick = {
                        val ms = PersianDateUtils.jalaliToMillis(year, month, day, hour, minute)
                        onConfirm(ms)
                    }) {
                        Text("تأیید")
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentUploadDialog(
    onDismiss: () -> Unit,
    onConfirmUri: (uri: Uri, title: String, description: String, category: DocumentCategory, extractedText: String) -> Unit,
    onConfirmCameraFile: (file: File, title: String, description: String, category: DocumentCategory, extractedText: String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var extractedText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(DocumentCategory.CONTRACT) }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var capturedFile by remember { mutableStateOf<File?>(null) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    var sourceLabel by remember { mutableStateOf("هنوز فایلی انتخاب نشده است") }

    val docPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pickedUri = uri
            capturedFile = null
            sourceLabel = "فایل از حافظه دستگاه انتخاب شد"
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedUri = uri
            capturedFile = null
            sourceLabel = "تصویر از گالری انتخاب شد"
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraFile != null) {
            capturedFile = tempCameraFile
            pickedUri = null
            sourceLabel = "تصویر با دوربین ثبت شد"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "بارگذاری مدرک جدید",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            docPicker.launch(
                                arrayOf(
                                    "application/pdf",
                                    "application/msword",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "application/vnd.ms-excel",
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    "image/*"
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("PDF/فایل", style = MaterialTheme.typography.labelMedium)
                    }
                    FilledTonalButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("گالری", style = MaterialTheme.typography.labelMedium)
                    }
                    FilledTonalButton(
                        onClick = {
                            val (uri, file) = FileStorageHelper.createTempCameraImageUri(context)
                            tempCameraFile = file
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("دوربین", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Text(
                    text = sourceLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (pickedUri != null || capturedFile != null) StatusEmerald
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان مدرک (اختیاری - نام فایل جایگزین می‌شود)") },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownSelectorField(
                    label = "دسته‌بندی مدرک",
                    options = DocumentCategory.entries,
                    selected = category,
                    optionLabel = { it.titleFa },
                    onSelected = { category = it }
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("توضیحات") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = extractedText,
                    onValueChange = { extractedText = it },
                    label = { Text("متن / رونویس کلیدی سند (برای جستجوی سریع آفلاین)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        enabled = pickedUri != null || capturedFile != null,
                        onClick = {
                            pickedUri?.let {
                                onConfirmUri(it, title, description, category, extractedText)
                            } ?: capturedFile?.let {
                                onConfirmCameraFile(it, title, description, category, extractedText)
                            }
                        }
                    ) {
                        Text("ثبت در پرونده")
                    }
                }
            }
        }
    }
}
