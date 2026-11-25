package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AccessLevel
import com.example.data.model.CaseEntity
import com.example.data.model.CaseLegalRefEntity
import com.example.data.model.CaseStageEntity
import com.example.data.model.DocumentCategory
import com.example.data.model.DocumentEntity
import com.example.data.model.LegalRefType
import com.example.data.model.StageStatus
import com.example.data.model.StageType
import com.example.data.model.UserRole
import com.example.ui.components.CaseStatusBadge
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DocumentUploadDialog
import com.example.ui.components.DocumentViewerDialog
import com.example.ui.components.DropdownSelectorField
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.ShamsiDatePickerField
import com.example.ui.components.StageStatusBadge
import com.example.ui.components.VakilTopBar
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.WorkspaceUiState
import com.example.util.CaseReportBuilder
import com.example.util.FileStorageHelper
import com.example.util.PersianDateUtils

@Composable
fun CaseDetailScreen(
    caseId: Long,
    state: WorkspaceUiState,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val caseEntity = state.visibleCases.firstOrNull { it.id == caseId }
    if (caseEntity == null) {
        Scaffold(topBar = { VakilTopBar(title = "جزئیات پرونده", onBackClick = onBack) }) { p ->
            Column(modifier = Modifier.padding(p)) {
                EmptyStateBox(
                    icon = Icons.Default.Description,
                    title = "پرونده در دسترس نیست",
                    subtitle = "ممکن است حذف شده باشد یا دسترسی شما به آن محدود شده باشد.",
                    actionLabel = "بازگشت",
                    onAction = onBack
                )
            }
        }
        return
    }

    val accessLevel = state.accessFor(caseEntity) ?: AccessLevel.VIEW_ONLY
    val isLeadLawyer = state.currentUser?.role == UserRole.LEAD_LAWYER
    val canEditCase = isLeadLawyer || accessLevel == AccessLevel.FULL_CASE_ACCESS
    val canUploadDocs = canEditCase || accessLevel == AccessLevel.VIEW_AND_UPLOAD || accessLevel == AccessLevel.REGISTER_STAGES
    val canRegisterStages = canEditCase || accessLevel == AccessLevel.REGISTER_STAGES

    val stages = remember(state.visibleStages, caseId) {
        state.visibleStages.filter { it.caseId == caseId }.sortedByDescending { it.dateMillis }
    }
    val docs = remember(state.visibleDocuments, caseId) {
        state.visibleDocuments.filter { it.caseId == caseId }
    }
    val legalRefs = remember(state.allCaseLegalRefs, caseId) {
        state.allCaseLegalRefs.filter { it.caseId == caseId }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showEditCase by remember { mutableStateOf(false) }
    var showDeleteCase by remember { mutableStateOf(false) }
    var showUploadDoc by remember { mutableStateOf(false) }
    var showStageDialog by remember { mutableStateOf<CaseStageEntity?>(null) }
    var createNewStage by remember { mutableStateOf(false) }
    var viewingDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var editingDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var showAttachLegalDialog by remember { mutableStateOf(false) }

    val tabs = listOf("اطلاعات کلی", "مراحل و رویدادها", "مدارک", "قوانین و آراء", "خلاصه و گزارش")

    Scaffold(
        topBar = {
            VakilTopBar(
                title = "پرونده ${caseEntity.officeCaseNumber}",
                subtitle = caseEntity.subject,
                onBackClick = onBack,
                actions = {
                    if (canEditCase) {
                        IconButton(onClick = { showEditCase = true }, modifier = Modifier.testTag("edit_case_button")) {
                            Icon(Icons.Default.Edit, contentDescription = "ویرایش پرونده")
                        }
                    }
                    if (isLeadLawyer) {
                        IconButton(onClick = { showDeleteCase = true }, modifier = Modifier.testTag("delete_case_button")) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف پرونده")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            when (selectedTab) {
                1 if canRegisterStages -> FloatingActionButton(
                    onClick = { createNewStage = true; showStageDialog = null },
                    modifier = Modifier.testTag("add_stage_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "ثبت مرحله جدید")
                }
                2 if canUploadDocs -> FloatingActionButton(
                    onClick = { showUploadDoc = true },
                    modifier = Modifier.testTag("add_doc_fab")
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = "بارگذاری مدرک")
                }
                3 if canEditCase -> FloatingActionButton(
                    onClick = { showAttachLegalDialog = true },
                    modifier = Modifier.testTag("attach_legal_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "پیوست قانون یا رأی")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> CaseOverviewTab(
                    caseEntity = caseEntity,
                    plainNotes = viewModel.decryptNotes(caseEntity),
                    accessLevel = accessLevel,
                    stages = stages,
                    docsCount = docs.size
                )
                1 -> CaseStagesTab(
                    stages = stages,
                    documents = docs,
                    canManage = canRegisterStages,
                    canDelete = canEditCase,
                    onEdit = { showStageDialog = it; createNewStage = false },
                    onToggleStatus = { st, next -> viewModel.updateStageStatus(st, next) },
                    onDelete = { viewModel.deleteStage(it.id) }
                )
                2 -> CaseDocumentsTab(
                    documents = docs,
                    canDelete = canEditCase,
                    onView = { viewingDoc = it },
                    onEdit = { editingDoc = it },
                    onShare = { doc ->
                        FileStorageHelper.shareFile(context, doc.localPath, doc.mimeType, doc.title)
                    },
                    onDelete = { viewModel.deleteDocument(it) }
                )
                3 -> CaseLegalRefsTab(
                    refs = legalRefs,
                    canManage = canEditCase,
                    onRemove = { viewModel.removeCaseLegalRef(it.id) }
                )
                4 -> CaseSummaryReportTab(
                    caseEntity = caseEntity,
                    plainNotes = viewModel.decryptNotes(caseEntity),
                    stages = stages,
                    documents = docs,
                    legalRefs = legalRefs,
                    onExportPdf = { share -> viewModel.exportCaseSummaryPdf(context, caseEntity, share) },
                    onToast = { viewModel.showToast(it) }
                )
            }
        }
    }

    if (showEditCase && state.currentUser != null) {
        CaseFormDialog(
            initialCase = caseEntity,
            initialPlainNotes = viewModel.decryptNotes(caseEntity),
            ownerUserId = caseEntity.ownerUserId,
            onDismiss = { showEditCase = false },
            onSave = { updated, notes ->
                viewModel.saveCase(context, updated, notes) { showEditCase = false }
            }
        )
    }

    if (showDeleteCase) {
        ConfirmDeleteDialog(
            title = "حذف پرونده",
            message = "آیا از حذف پرونده ${caseEntity.officeCaseNumber} و تمامی مدارک و مراحل آن اطمینان دارید؟",
            onConfirm = {
                showDeleteCase = false
                viewModel.deleteCase(context, caseEntity.id)
                onBack()
            },
            onDismiss = { showDeleteCase = false }
        )
    }

    if (showUploadDoc) {
        DocumentUploadDialog(
            onDismiss = { showUploadDoc = false },
            onConfirmUri = { uri, title, desc, cat, text ->
                viewModel.addDocumentFromUri(context, caseEntity.id, uri, title, desc, cat, text)
                showUploadDoc = false
            },
            onConfirmCameraFile = { file, title, desc, cat, text ->
                viewModel.addDocumentFromCameraFile(context, caseEntity.id, file, title, desc, cat, text)
                showUploadDoc = false
            }
        )
    }

    if (createNewStage || showStageDialog != null) {
        StageFormDialog(
            caseId = caseEntity.id,
            initialStage = showStageDialog,
            documents = docs,
            defaultResponsible = state.currentUser?.fullName ?: "وکیل",
            onDismiss = { createNewStage = false; showStageDialog = null },
            onSave = {
                viewModel.saveStage(it)
                createNewStage = false
                showStageDialog = null
            }
        )
    }

    viewingDoc?.let { doc ->
        DocumentViewerDialog(document = doc, onDismiss = { viewingDoc = null })
    }

    editingDoc?.let { doc ->
        EditDocumentDialog(
            document = doc,
            onDismiss = { editingDoc = null },
            onSave = { updated ->
                viewModel.updateDocument(updated)
                editingDoc = null
            }
        )
    }

    if (showAttachLegalDialog) {
        AttachLegalResourceDialog(
            state = state,
            onDismiss = { showAttachLegalDialog = false },
            onAttachLaw = { law ->
                viewModel.attachLegalRefToCase(
                    caseId = caseEntity.id,
                    refType = LegalRefType.LAW,
                    refId = law.id,
                    title = "${law.lawName} - ${law.articleNumber}",
                    subtitle = law.category.titleFa
                )
                showAttachLegalDialog = false
            },
            onAttachRuling = { ruling ->
                viewModel.attachLegalRefToCase(
                    caseId = caseEntity.id,
                    refType = LegalRefType.RULING,
                    refId = ruling.id,
                    title = "${ruling.rulingNumber}: ${ruling.title}",
                    subtitle = "${ruling.authority.titleFa} (${ruling.persianYear})"
                )
                showAttachLegalDialog = false
            }
        )
    }
}

@Composable
private fun CaseOverviewTab(
    caseEntity: CaseEntity,
    plainNotes: String,
    accessLevel: AccessLevel,
    stages: List<CaseStageEntity>,
    docsCount: Int
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("مشخصات پرونده", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        CaseStatusBadge(caseEntity.status)
                    }
                    HorizontalDivider()
                    DetailRow("کلاسه داخلی دفتر:", caseEntity.officeCaseNumber)
                    DetailRow("شماره پرونده مرجع:", caseEntity.courtCaseNumber.ifBlank { "ثبت نشده" })
                    DetailRow("نوع پرونده:", caseEntity.caseType.titleFa)
                    DetailRow("مرجع رسیدگی:", "${caseEntity.authorityType.titleFa} (${caseEntity.authorityBranch.ifBlank { "—" }})")
                    DetailRow("خواهان / شاکی:", caseEntity.plaintiffName)
                    DetailRow("خوانده / متهم:", caseEntity.defendantName)
                    DetailRow("وکیل طرف مقابل:", caseEntity.opposingCounsel.ifBlank { "ثبت نشده" })
                    DetailRow("تاریخ طرح دعوا:", PersianDateUtils.formatShamsiLong(caseEntity.filingDateMillis))
                    DetailRow("تاریخ جلسه بعدی:", PersianDateUtils.formatShamsiDateTime(caseEntity.nextHearingMillis))
                    DetailRow("سطح دسترسی شما:", accessLevel.titleFa)
                }
            }
        }

        if (caseEntity.summary.isNotBlank()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("خلاصه پرونده", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(caseEntity.summary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        if (plainNotes.isNotBlank()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("یادداشت‌های محرمانه وکیل (رمزگذاری‌شده)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(plainNotes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("نمای سریع آخرین وضعیت", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        CaseReportBuilder.buildLatestStatusReport(caseEntity, stages, docsCount),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CaseStagesTab(
    stages: List<CaseStageEntity>,
    documents: List<DocumentEntity>,
    canManage: Boolean,
    canDelete: Boolean,
    onEdit: (CaseStageEntity) -> Unit,
    onToggleStatus: (CaseStageEntity, StageStatus) -> Unit,
    onDelete: (CaseStageEntity) -> Unit
) {
    if (stages.isEmpty()) {
        EmptyStateBox(
            icon = Icons.Default.Timeline,
            title = "مرحله‌ای ثبت نشده است",
            subtitle = "مراحل رسیدگی پرونده را به صورت گام‌به‌گام ثبت کنید."
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(stages, key = { it.id }) { st ->
            val linkedDoc = documents.firstOrNull { it.id == st.relatedDocumentId }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(st.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        StageStatusBadge(st.status)
                    }
                    Text(
                        "${st.stageType.titleFa} • ${PersianDateUtils.formatShamsiLong(st.dateMillis)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (st.description.isNotBlank()) {
                        Text(st.description, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        "مسئول پیگیری: ${st.responsibleName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (linkedDoc != null) {
                        Text(
                            "مدرک مرتبط: ${linkedDoc.title}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (canManage) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (st.status != StageStatus.COMPLETED) {
                                TextButton(onClick = { onToggleStatus(st, StageStatus.COMPLETED) }) {
                                    Text("تکمیل شد")
                                }
                            }
                            TextButton(onClick = { onEdit(st) }) {
                                Text("ویرایش")
                            }
                            if (canDelete) {
                                TextButton(onClick = { onDelete(st) }) {
                                    Text("حذف", color = MaterialTheme.colorScheme.error)
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
private fun CaseDocumentsTab(
    documents: List<DocumentEntity>,
    canDelete: Boolean,
    onView: (DocumentEntity) -> Unit,
    onEdit: (DocumentEntity) -> Unit,
    onShare: (DocumentEntity) -> Unit,
    onDelete: (DocumentEntity) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf<DocumentCategory?>(null) }
    var sortNewest by remember { mutableStateOf(true) }

    val filtered = remember(documents, query, selectedCat, sortNewest) {
        documents.filter { d ->
            val qMatch = query.isBlank() ||
                d.title.contains(query, true) ||
                d.description.contains(query, true) ||
                d.extractedText.contains(query, true)
            val cMatch = selectedCat == null || d.category == selectedCat
            qMatch && cMatch
        }.let { list ->
            if (sortNewest) list.sortedByDescending { it.createdAt } else list.sortedBy { it.title }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("جستجو در نام یا متن مدارک...") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(onClick = { sortNewest = !sortNewest }) {
                Text(if (sortNewest) "جدیدترین" else "الفبا")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(selected = selectedCat == null, onClick = { selectedCat = null }, label = { Text("همه") })
            DocumentCategory.entries.forEach { cat ->
                FilterChip(
                    selected = selectedCat == cat,
                    onClick = { selectedCat = if (selectedCat == cat) null else cat },
                    label = { Text(cat.titleFa) }
                )
            }
        }

        if (filtered.isEmpty()) {
            EmptyStateBox(
                icon = Icons.Default.Description,
                title = "مدرکی یافت نشد",
                subtitle = "فایل PDF، تصویر دوربین، Word یا Excel را به پرونده اضافه کنید."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { doc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onView(doc) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(doc.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(doc.fileType.titleFa, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                "${doc.category.titleFa} • ${FileStorageHelper.formatFileSize(doc.sizeBytes)} • ${PersianDateUtils.formatShamsiShort(doc.createdAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (doc.description.isNotBlank()) {
                                Text(doc.description, style = MaterialTheme.typography.bodySmall)
                            }
                            if (doc.extractedText.isNotBlank()) {
                                Text("متن سند: ${doc.extractedText}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { onView(doc) }) {
                                    Icon(Icons.Default.Visibility, contentDescription = "مشاهده")
                                }
                                IconButton(onClick = { onEdit(doc) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                                }
                                IconButton(onClick = { onShare(doc) }) {
                                    Icon(Icons.Default.Share, contentDescription = "ارسال")
                                }
                                if (canDelete) {
                                    IconButton(onClick = { onDelete(doc) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
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

@Composable
private fun CaseLegalRefsTab(
    refs: List<CaseLegalRefEntity>,
    canManage: Boolean,
    onRemove: (CaseLegalRefEntity) -> Unit
) {
    if (refs.isEmpty()) {
        EmptyStateBox(
            icon = Icons.Default.MenuBook,
            title = "قانون یا رأی پیوست نشده است",
            subtitle = "از دکمه + برای پیوست‌کردن مواد قانونی یا آرای وحدت رویه به این پرونده استفاده کنید."
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(refs, key = { it.id }) { ref ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ref.refTitle, fontWeight = FontWeight.Bold)
                        Text(ref.refSubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (canManage) {
                        IconButton(onClick = { onRemove(ref) }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف پیوست", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CaseSummaryReportTab(
    caseEntity: CaseEntity,
    plainNotes: String,
    stages: List<CaseStageEntity>,
    documents: List<DocumentEntity>,
    legalRefs: List<CaseLegalRefEntity>,
    onExportPdf: (share: Boolean) -> Unit,
    onToast: (String) -> Unit
) {
    val context = LocalContext.current
    val comprehensiveText = remember(caseEntity, plainNotes, stages, documents, legalRefs) {
        CaseReportBuilder.buildComprehensiveCaseSummary(caseEntity, plainNotes, stages, documents, legalRefs)
    }
    val latestDigest = remember(caseEntity, stages, documents) {
        CaseReportBuilder.buildLatestStatusReport(caseEntity, stages, documents.size)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onExportPdf(false) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("خروجی PDF")
                }
                FilledTonalButton(onClick = { onExportPdf(true) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ارسال PDF")
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("گزارش کوتاه آخرین وضعیت (قابل ارسال)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row {
                            IconButton(onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("status", latestDigest))
                                onToast("گزارش در حافظه کپی شد.")
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "کپی")
                            }
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, latestDigest)
                                }
                                context.startActivity(Intent.createChooser(intent, "ارسال گزارش وضعیت..."))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "ارسال متنی")
                            }
                        }
                    }
                    Text(latestDigest, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("خلاصه جامع پرونده و مدارک", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(comprehensiveText, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun StageFormDialog(
    caseId: Long,
    initialStage: CaseStageEntity?,
    documents: List<DocumentEntity>,
    defaultResponsible: String,
    onDismiss: () -> Unit,
    onSave: (CaseStageEntity) -> Unit
) {
    var stageType by remember { mutableStateOf(initialStage?.stageType ?: StageType.FILING) }
    var title by remember { mutableStateOf(initialStage?.title ?: stageType.titleFa) }
    var dateMillis by remember { mutableStateOf<Long?>(initialStage?.dateMillis ?: System.currentTimeMillis()) }
    var description by remember { mutableStateOf(initialStage?.description ?: "") }
    var responsible by remember { mutableStateOf(initialStage?.responsibleName ?: defaultResponsible) }
    var status by remember { mutableStateOf(initialStage?.status ?: StageStatus.PENDING_ACTION) }
    var selectedDocId by remember { mutableStateOf(initialStage?.relatedDocumentId) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    if (initialStage == null) "ثبت مرحله جدید پرونده" else "ویرایش مرحله",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                DropdownSelectorField(
                    label = "نوع مرحله",
                    options = StageType.entries,
                    selected = stageType,
                    optionLabel = { it.titleFa },
                    onSelected = {
                        stageType = it
                        if (initialStage == null) title = it.titleFa
                    }
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان مرحله") },
                    modifier = Modifier.fillMaxWidth()
                )
                ShamsiDatePickerField(
                    label = "تاریخ مرحله",
                    millis = dateMillis,
                    onDatePicked = { dateMillis = it }
                )
                DropdownSelectorField(
                    label = "وضعیت",
                    options = StageStatus.entries,
                    selected = status,
                    optionLabel = { it.titleFa },
                    onSelected = { status = it }
                )
                OutlinedTextField(
                    value = responsible,
                    onValueChange = { responsible = it },
                    label = { Text("مسئول پیگیری") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (documents.isNotEmpty()) {
                    val docOptions = listOf<DocumentEntity?>(null) + documents
                    val currentDoc = documents.firstOrNull { it.id == selectedDocId }
                    DropdownSelectorField(
                        label = "مدرک مرتبط (اختیاری)",
                        options = docOptions,
                        selected = currentDoc,
                        optionLabel = { it?.title ?: "بدون مدرک پیوست" },
                        onSelected = { selectedDocId = it?.id }
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("توضیحات") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onSave(
                            CaseStageEntity(
                                id = initialStage?.id ?: 0L,
                                caseId = caseId,
                                stageType = stageType,
                                title = title.ifBlank { stageType.titleFa },
                                dateMillis = dateMillis ?: System.currentTimeMillis(),
                                description = description,
                                relatedDocumentId = selectedDocId,
                                responsibleName = responsible,
                                status = status
                            )
                        )
                    }) {
                        Text("ذخیره")
                    }
                }
            }
        }
    }
}

@Composable
private fun EditDocumentDialog(
    document: DocumentEntity,
    onDismiss: () -> Unit,
    onSave: (DocumentEntity) -> Unit
) {
    var title by remember { mutableStateOf(document.title) }
    var description by remember { mutableStateOf(document.description) }
    var category by remember { mutableStateOf(document.category) }
    var extractedText by remember { mutableStateOf(document.extractedText) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("ویرایش مشخصات مدرک", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان") }, modifier = Modifier.fillMaxWidth())
                DropdownSelectorField(
                    label = "دسته‌بندی",
                    options = DocumentCategory.entries,
                    selected = category,
                    optionLabel = { it.titleFa },
                    onSelected = { category = it }
                )
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("توضیحات") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = extractedText, onValueChange = { extractedText = it }, label = { Text("متن / رونویس مدرک") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Button(onClick = {
                        onSave(document.copy(title = title.ifBlank { document.title }, description = description, category = category, extractedText = extractedText))
                    }) {
                        Text("ذخیره")
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachLegalResourceDialog(
    state: WorkspaceUiState,
    onDismiss: () -> Unit,
    onAttachLaw: (com.example.data.model.LawEntity) -> Unit,
    onAttachRuling: (com.example.data.model.RulingEntity) -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("پیوست قانون یا رأی قضایی", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("قوانین (${PersianDateUtils.toPersianDigits(state.allLaws.size.toString())})") })
                    FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("آرای قضایی (${PersianDateUtils.toPersianDigits(state.allRulings.size.toString())})") })
                }
                LazyColumn(modifier = Modifier.height(340.dp)) {
                    if (tab == 0) {
                        items(state.allLaws) { law ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAttachLaw(law) }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text("${law.lawName} - ${law.articleNumber}", fontWeight = FontWeight.Bold)
                                Text(law.fullText, maxLines = 2, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            HorizontalDivider()
                        }
                    } else {
                        items(state.allRulings) { r ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAttachRuling(r) }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text("${r.rulingNumber}: ${r.title}", fontWeight = FontWeight.Bold)
                                Text(r.fullText, maxLines = 2, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            HorizontalDivider()
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("بستن") }
                }
            }
        }
    }
}
