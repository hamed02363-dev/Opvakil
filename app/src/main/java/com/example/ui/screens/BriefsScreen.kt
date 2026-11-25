package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BriefEntity
import com.example.data.model.BriefType
import com.example.data.model.CaseEntity
import com.example.ui.components.DropdownSelectorField
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.VakilTopBar
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.WorkspaceUiState
import com.example.util.PersianDateUtils

@Composable
fun BriefsScreen(
    state: WorkspaceUiState,
    viewModel: MainViewModel,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<BriefType?>(null) }
    var editingBrief by remember { mutableStateOf<BriefEntity?>(null) }
    var templateCasePickerFor by remember { mutableStateOf<BriefEntity?>(null) }

    val savedBriefs = remember(state.allBriefs, searchQuery, selectedType) {
        state.allBriefs.filter { !it.isTemplate }.filter { b ->
            val qMatch = searchQuery.isBlank() ||
                b.title.contains(searchQuery, true) ||
                b.content.contains(searchQuery, true) ||
                b.caseTitleSnapshot.contains(searchQuery, true)
            val tMatch = selectedType == null || b.briefType == selectedType
            qMatch && tMatch
        }
    }

    val templates = remember(state.allBriefs, searchQuery, selectedType) {
        state.allBriefs.filter { it.isTemplate }.filter { b ->
            val qMatch = searchQuery.isBlank() ||
                b.title.contains(searchQuery, true) ||
                b.content.contains(searchQuery, true)
            val tMatch = selectedType == null || b.briefType == selectedType
            qMatch && tMatch
        }
    }

    Scaffold(
        topBar = {
            VakilTopBar(
                title = "لوایح، دادخواست و مکاتبات",
                subtitle = "تنظیم سند، ذخیره در پرونده و خروجی PDF آفلاین",
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingBrief = BriefEntity(
                        title = "",
                        briefType = BriefType.DEFENSE_BRIEF,
                        content = "",
                        isTemplate = false,
                        authorName = state.currentUser?.fullName ?: "وکیل"
                    )
                },
                modifier = Modifier.testTag("new_brief_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "سند جدید")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("اسناد ذخیره‌شده (${PersianDateUtils.toPersianDigits(savedBriefs.size.toString())})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("الگوهای آماده (${PersianDateUtils.toPersianDigits(templates.size.toString())})") }
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("جستجو در عنوان و متن لوایح و مکاتبات...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(selected = selectedType == null, onClick = { selectedType = null }, label = { Text("همه") })
                BriefType.entries.forEach { t ->
                    FilterChip(
                        selected = selectedType == t,
                        onClick = { selectedType = if (selectedType == t) null else t },
                        label = { Text(t.titleFa) }
                    )
                }
            }

            if (selectedTab == 0) {
                if (savedBriefs.isEmpty()) {
                    EmptyStateBox(
                        icon = Icons.Default.EditNote,
                        title = "سندی ذخیره نشده است",
                        subtitle = "از تب «الگوهای آماده» یک الگو انتخاب کنید یا با دکمه + سند جدید بنویسید.",
                        actionLabel = "مشاهده الگوهای آماده",
                        onAction = { selectedTab = 1 }
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(savedBriefs, key = { it.id }) { brief ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { editingBrief = brief },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(brief.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        Text(brief.briefType.titleFa, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                    }
                                    if (brief.caseTitleSnapshot.isNotBlank()) {
                                        Text(
                                            "پرونده مرتبط: ${brief.caseTitleSnapshot}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        brief.content,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = { editingBrief = brief }) {
                                            Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                                        }
                                        IconButton(onClick = { viewModel.exportBriefPdf(context, brief, false) }) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = "ساخت PDF")
                                        }
                                        IconButton(onClick = { viewModel.exportBriefPdf(context, brief, true) }) {
                                            Icon(Icons.Default.Share, contentDescription = "ارسال PDF")
                                        }
                                        IconButton(onClick = { viewModel.deleteBrief(brief.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(templates, key = { it.id }) { tpl ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { templateCasePickerFor = tpl },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tpl.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text(tpl.briefType.titleFa, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    tpl.content,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(onClick = { templateCasePickerFor = tpl }) {
                                    Text("ایجاد سند از این الگو")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    templateCasePickerFor?.let { tpl ->
        var selectedCase by remember { mutableStateOf<CaseEntity?>(state.visibleCases.firstOrNull()) }
        val options = listOf<CaseEntity?>(null) + state.visibleCases
        Dialog(onDismissRequest = { templateCasePickerFor = null }) {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("تکمیل خودکار با اطلاعات پرونده", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    DropdownSelectorField(
                        label = "اتصال به پرونده (اختیاری)",
                        options = options,
                        selected = selectedCase,
                        optionLabel = { it?.let { "${it.officeCaseNumber} - ${it.subject}" } ?: "بدون اتصال به پرونده" },
                        onSelected = { selectedCase = it }
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { templateCasePickerFor = null }) { Text("انصراف") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            editingBrief = viewModel.createBriefFromTemplate(tpl, selectedCase)
                            templateCasePickerFor = null
                        }) {
                            Text("ورود به ویرایشگر")
                        }
                    }
                }
            }
        }
    }

    editingBrief?.let { initial ->
        BriefEditorDialog(
            initialBrief = initial,
            availableCases = state.visibleCases,
            onDismiss = { editingBrief = null },
            onSave = { saved ->
                viewModel.saveBrief(saved) { editingBrief = null }
            },
            onExportPdf = { brief, share ->
                viewModel.saveBrief(brief) { newId ->
                    viewModel.exportBriefPdf(context, brief.copy(id = newId), share)
                }
            }
        )
    }
}

@Composable
private fun BriefEditorDialog(
    initialBrief: BriefEntity,
    availableCases: List<CaseEntity>,
    onDismiss: () -> Unit,
    onSave: (BriefEntity) -> Unit,
    onExportPdf: (BriefEntity, share: Boolean) -> Unit
) {
    var title by remember { mutableStateOf(initialBrief.title) }
    var briefType by remember { mutableStateOf(initialBrief.briefType) }
    var content by remember { mutableStateOf(initialBrief.content) }
    var selectedCase by remember {
        mutableStateOf(availableCases.firstOrNull { it.id == initialBrief.caseId })
    }
    val caseOptions = listOf<CaseEntity?>(null) + availableCases

    fun buildEntity(): BriefEntity = initialBrief.copy(
        title = title.ifBlank { briefType.titleFa },
        briefType = briefType,
        caseId = selectedCase?.id,
        caseTitleSnapshot = selectedCase?.let { "${it.officeCaseNumber} - ${it.subject}" } ?: "",
        content = content,
        isTemplate = false
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ویرایشگر لایحه و مکاتبات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان سند") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownSelectorField(
                        label = "نوع سند",
                        options = BriefType.entries,
                        selected = briefType,
                        optionLabel = { it.titleFa },
                        onSelected = { briefType = it }
                    )
                    DropdownSelectorField(
                        label = "ذخیره در پرونده",
                        options = caseOptions,
                        selected = selectedCase,
                        optionLabel = { it?.let { "${it.officeCaseNumber} - ${it.subject}" } ?: "سند مستقل (بدون پرونده)" },
                        onSelected = { selectedCase = it }
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("متن کامل سند حقوقی") },
                        minLines = 12,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onExportPdf(buildEntity(), false) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("نمایش PDF")
                        }
                        OutlinedButton(
                            onClick = { onExportPdf(buildEntity(), true) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("ارسال PDF")
                        }
                    }
                    Button(
                        onClick = { onSave(buildEntity()) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ذخیره سند")
                    }
                }
            }
        }
    }
}
