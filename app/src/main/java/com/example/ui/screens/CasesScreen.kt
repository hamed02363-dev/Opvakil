package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.model.AuthorityType
import com.example.data.model.CaseEntity
import com.example.data.model.CaseStatus
import com.example.data.model.CaseType
import com.example.data.model.UserRole
import com.example.ui.components.CaseStatusBadge
import com.example.ui.components.DropdownSelectorField
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.ShamsiDatePickerField
import com.example.ui.components.StatusPill
import com.example.ui.components.VakilTopBar
import com.example.ui.theme.StatusCrimson
import com.example.ui.theme.StatusCrimsonBg
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.WorkspaceUiState
import com.example.util.PersianDateUtils

@Composable
fun CasesScreen(
    state: WorkspaceUiState,
    viewModel: MainViewModel,
    onMenuClick: () -> Unit,
    onOpenCase: (Long) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<CaseType?>(null) }
    var selectedStatus by remember { mutableStateOf<CaseStatus?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val isLeadLawyer = state.currentUser?.role == UserRole.LEAD_LAWYER

    val filteredCases = remember(state.visibleCases, searchQuery, selectedType, selectedStatus) {
        state.visibleCases.filter { c ->
            val matchesQuery = searchQuery.isBlank() ||
                c.officeCaseNumber.contains(searchQuery, true) ||
                c.courtCaseNumber.contains(searchQuery, true) ||
                c.subject.contains(searchQuery, true) ||
                c.plaintiffName.contains(searchQuery, true) ||
                c.defendantName.contains(searchQuery, true)
            val matchesType = selectedType == null || c.caseType == selectedType
            val matchesStatus = selectedStatus == null || c.status == selectedStatus
            matchesQuery && matchesType && matchesStatus
        }
    }

    Scaffold(
        topBar = {
            VakilTopBar(
                title = "مدیریت پرونده‌ها",
                subtitle = PersianDateUtils.toPersianDigits("${filteredCases.size} پرونده در دسترس"),
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            if (isLeadLawyer) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("add_case_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "ثبت پرونده جدید")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("جستجو بر اساس کلاسه، خواهان، خوانده یا موضوع...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("cases_search_input")
            )

            // Type Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { selectedType = null },
                    label = { Text("همه انواع") }
                )
                CaseType.entries.forEach { t ->
                    FilterChip(
                        selected = selectedType == t,
                        onClick = { selectedType = if (selectedType == t) null else t },
                        label = { Text(t.titleFa) }
                    )
                }
            }

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = { selectedStatus = null },
                    label = { Text("همه وضعیت‌ها") }
                )
                CaseStatus.entries.forEach { st ->
                    FilterChip(
                        selected = selectedStatus == st,
                        onClick = { selectedStatus = if (selectedStatus == st) null else st },
                        label = { Text(st.titleFa) }
                    )
                }
            }

            if (filteredCases.isEmpty()) {
                EmptyStateBox(
                    icon = Icons.Default.FolderOff,
                    title = "پرونده‌ای یافت نشد",
                    subtitle = if (isLeadLawyer) "برای ثبت اولین پرونده روی دکمه + بزنید."
                    else "پرونده‌ای با این فیلتر برای شما تعریف نشده است.",
                    actionLabel = if (isLeadLawyer) "ثبت پرونده جدید" else null,
                    onAction = if (isLeadLawyer) { { showCreateDialog = true } } else null
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCases, key = { it.id }) { c ->
                        val access = state.accessFor(c)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenCase(c.id) }
                                .testTag("case_card_${c.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Gavel,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "کلاسه ${c.officeCaseNumber}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    CaseStatusBadge(c.status)
                                }

                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = c.subject,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "خواهان/شاکی: ${c.plaintiffName}  |  خوانده/متهم: ${c.defendantName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${c.caseType.titleFa} • ${c.authorityBranch.ifBlank { c.authorityType.titleFa }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (c.nextHearingMillis != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "جلسه: ${PersianDateUtils.formatShamsiShort(c.nextHearingMillis)}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    } else {
                                        Spacer(Modifier.width(1.dp))
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (c.hasPendingAction) {
                                            StatusPill("نیازمند اقدام", StatusCrimson, StatusCrimsonBg)
                                        }
                                        if (!isLeadLawyer && access != null) {
                                            StatusPill(
                                                access.titleFa,
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog && state.currentUser != null) {
        CaseFormDialog(
            initialCase = null,
            initialPlainNotes = "",
            ownerUserId = state.currentUser.id,
            onDismiss = { showCreateDialog = false },
            onSave = { entity, notes ->
                viewModel.saveCase(context, entity, notes) {
                    showCreateDialog = false
                }
            }
        )
    }
}

@Composable
fun CaseFormDialog(
    initialCase: CaseEntity?,
    initialPlainNotes: String,
    ownerUserId: Long,
    onDismiss: () -> Unit,
    onSave: (CaseEntity, String) -> Unit
) {
    val context = LocalContext.current
    var officeNo by remember { mutableStateOf(initialCase?.officeCaseNumber ?: "") }
    var courtNo by remember { mutableStateOf(initialCase?.courtCaseNumber ?: "") }
    var caseType by remember { mutableStateOf(initialCase?.caseType ?: CaseType.CIVIL) }
    var authorityType by remember { mutableStateOf(initialCase?.authorityType ?: AuthorityType.COURT) }
    var authorityBranch by remember { mutableStateOf(initialCase?.authorityBranch ?: "") }
    var plaintiff by remember { mutableStateOf(initialCase?.plaintiffName ?: "") }
    var defendant by remember { mutableStateOf(initialCase?.defendantName ?: "") }
    var subject by remember { mutableStateOf(initialCase?.subject ?: "") }
    var filingDate by remember { mutableStateOf<Long?>(initialCase?.filingDateMillis ?: System.currentTimeMillis()) }
    var nextHearing by remember { mutableStateOf<Long?>(initialCase?.nextHearingMillis) }
    var opposingCounsel by remember { mutableStateOf(initialCase?.opposingCounsel ?: "") }
    var status by remember { mutableStateOf(initialCase?.status ?: CaseStatus.UNDER_REVIEW) }
    var summary by remember { mutableStateOf(initialCase?.summary ?: "") }
    var lawyerNotes by remember { mutableStateOf(initialPlainNotes) }
    var importantDatesNote by remember { mutableStateOf(initialCase?.importantDatesNote ?: "") }
    var hasPendingAction by remember { mutableStateOf(initialCase?.hasPendingAction ?: false) }
    var reminderEnabled by remember { mutableStateOf(initialCase?.reminderEnabled ?: false) }
    var reminderMillis by remember { mutableStateOf(initialCase?.reminderMillis) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            reminderEnabled = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (initialCase == null) "ثبت پرونده جدید" else "ویرایش پرونده",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (validationError != null) {
                        Text(validationError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedTextField(
                        value = officeNo,
                        onValueChange = { officeNo = it },
                        label = { Text("شماره پرونده داخلی دفتر *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = courtNo,
                        onValueChange = { courtNo = it },
                        label = { Text("شماره پرونده در دادگاه یا مرجع (ثنا)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownSelectorField(
                        label = "نوع پرونده",
                        options = CaseType.entries,
                        selected = caseType,
                        optionLabel = { it.titleFa },
                        onSelected = { caseType = it }
                    )
                    DropdownSelectorField(
                        label = "مرجع رسیدگی",
                        options = AuthorityType.entries,
                        selected = authorityType,
                        optionLabel = { it.titleFa },
                        onSelected = { authorityType = it }
                    )
                    OutlinedTextField(
                        value = authorityBranch,
                        onValueChange = { authorityBranch = it },
                        label = { Text("شعبه / واحد مرجع (مثلاً شعبه ۱۲ حقوقی)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = plaintiff,
                        onValueChange = { plaintiff = it },
                        label = { Text("نام خواهان / شاکی *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = defendant,
                        onValueChange = { defendant = it },
                        label = { Text("نام خوانده / متهم / متشاکی *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("موضوع دعوا یا موضوع پرونده *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownSelectorField(
                        label = "وضعیت پرونده",
                        options = CaseStatus.entries,
                        selected = status,
                        optionLabel = { it.titleFa },
                        onSelected = { status = it }
                    )
                    ShamsiDatePickerField(
                        label = "تاریخ طرح دعوا",
                        millis = filingDate,
                        includeTime = false,
                        onDatePicked = { filingDate = it }
                    )
                    ShamsiDatePickerField(
                        label = "تاریخ جلسه بعدی (با ساعت)",
                        millis = nextHearing,
                        includeTime = true,
                        onDatePicked = {
                            nextHearing = it
                            if (it != null && reminderMillis == null) {
                                reminderMillis = it - 24L * 60 * 60 * 1000
                            }
                        }
                    )
                    OutlinedTextField(
                        value = opposingCounsel,
                        onValueChange = { opposingCounsel = it },
                        label = { Text("وکیل یا وکلای طرف مقابل") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("خلاصه پرونده") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lawyerNotes,
                        onValueChange = { lawyerNotes = it },
                        label = { Text("یادداشت‌های محرمانه وکیل (ذخیره با رمزگذاری AES)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = importantDatesNote,
                        onValueChange = { importantDatesNote = it },
                        label = { Text("تاریخ‌های مهم و مهلت‌ها") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("نیازمند اقدام فوری وکیل")
                        Switch(checked = hasPendingAction, onCheckedChange = { hasPendingAction = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(6.dp))
                            Text("فعال‌سازی یادآور / اعلان آفلاین")
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!granted) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                                reminderEnabled = enabled
                                if (enabled && reminderMillis == null) {
                                    reminderMillis = nextHearing ?: (System.currentTimeMillis() + 3600_000L)
                                }
                            }
                        )
                    }

                    if (reminderEnabled) {
                        ShamsiDatePickerField(
                            label = "زمان ارسال یادآور آفلاین",
                            millis = reminderMillis,
                            includeTime = true,
                            onDatePicked = { reminderMillis = it }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("انصراف")
                    }
                    Button(
                        onClick = {
                            if (officeNo.isBlank() || plaintiff.isBlank() || defendant.isBlank() || subject.isBlank()) {
                                validationError = "لطفاً فیلدهای ستاره‌دار را تکمیل نمایید."
                                return@Button
                            }
                            val entity = CaseEntity(
                                id = initialCase?.id ?: 0L,
                                officeCaseNumber = officeNo.trim(),
                                courtCaseNumber = courtNo.trim(),
                                caseType = caseType,
                                authorityType = authorityType,
                                authorityBranch = authorityBranch.trim(),
                                plaintiffName = plaintiff.trim(),
                                defendantName = defendant.trim(),
                                subject = subject.trim(),
                                filingDateMillis = filingDate ?: System.currentTimeMillis(),
                                nextHearingMillis = nextHearing,
                                opposingCounsel = opposingCounsel.trim(),
                                status = status,
                                summary = summary.trim(),
                                encryptedLawyerNotes = initialCase?.encryptedLawyerNotes ?: "",
                                importantDatesNote = importantDatesNote.trim(),
                                reminderEnabled = reminderEnabled,
                                reminderMillis = reminderMillis,
                                hasPendingAction = hasPendingAction,
                                ownerUserId = initialCase?.ownerUserId ?: ownerUserId,
                                createdAt = initialCase?.createdAt ?: System.currentTimeMillis()
                            )
                            onSave(entity, lawyerNotes.trim())
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_case_button")
                    ) {
                        Text("ذخیره پرونده")
                    }
                }
            }
        }
    }
}
