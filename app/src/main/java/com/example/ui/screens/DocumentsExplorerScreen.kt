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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AccessLevel
import com.example.data.model.CaseEntity
import com.example.data.model.DocumentCategory
import com.example.data.model.DocumentEntity
import com.example.data.model.UserRole
import com.example.ui.components.DocumentUploadDialog
import com.example.ui.components.DocumentViewerDialog
import com.example.ui.components.DropdownSelectorField
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.VakilTopBar
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.WorkspaceUiState
import com.example.util.FileStorageHelper
import com.example.util.PersianDateUtils

@Composable
fun DocumentsExplorerScreen(
    state: WorkspaceUiState,
    viewModel: MainViewModel,
    onMenuClick: () -> Unit,
    onOpenCase: (Long) -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<DocumentCategory?>(null) }
    var viewingDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var showSelectCaseForUpload by remember { mutableStateOf(false) }
    var targetUploadCase by remember { mutableStateOf<CaseEntity?>(null) }

    val uploadableCases = remember(state.visibleCases, state.currentUser, state.allCaseAccess) {
        state.visibleCases.filter { c ->
            val access = state.accessFor(c)
            state.currentUser?.role == UserRole.LEAD_LAWYER ||
                access == AccessLevel.FULL_CASE_ACCESS ||
                access == AccessLevel.REGISTER_STAGES ||
                access == AccessLevel.VIEW_AND_UPLOAD
        }
    }

    val filteredDocs = remember(state.visibleDocuments, query, selectedCategory) {
        state.visibleDocuments.filter { d ->
            val qMatch = query.isBlank() ||
                d.title.contains(query, true) ||
                d.description.contains(query, true) ||
                d.extractedText.contains(query, true)
            val cMatch = selectedCategory == null || d.category == selectedCategory
            qMatch && cMatch
        }
    }

    Scaffold(
        topBar = {
            VakilTopBar(
                title = "آرشیو مدارک پرونده‌ها",
                subtitle = PersianDateUtils.toPersianDigits("${filteredDocs.size} مدرک"),
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            if (uploadableCases.isNotEmpty()) {
                FloatingActionButton(onClick = {
                    targetUploadCase = uploadableCases.first()
                    showSelectCaseForUpload = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "بارگذاری مدرک")
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
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("جستجو در عنوان، توضیح یا متن مدارک...") },
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
                FilterChip(selected = selectedCategory == null, onClick = { selectedCategory = null }, label = { Text("همه") })
                DocumentCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text(cat.titleFa) }
                    )
                }
            }

            if (filteredDocs.isEmpty()) {
                EmptyStateBox(
                    icon = Icons.Default.Description,
                    title = "مدرکی در آرشیو نیست",
                    subtitle = "مدارک پرونده‌ها از جمله PDF، تصویر دوربین، Word و Excel در اینجا نمایش داده می‌شوند."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredDocs, key = { it.id }) { doc ->
                        val parentCase = state.visibleCases.firstOrNull { it.id == doc.caseId }
                        val canDelete = parentCase != null && (
                            state.currentUser?.role == UserRole.LEAD_LAWYER ||
                                state.accessFor(parentCase) == AccessLevel.FULL_CASE_ACCESS
                            )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewingDoc = doc },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(doc.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text(doc.fileType.titleFa, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                }
                                Text(
                                    "پرونده: ${parentCase?.officeCaseNumber ?: "—"} (${parentCase?.subject ?: ""})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { parentCase?.let { onOpenCase(it.id) } }
                                )
                                Text(
                                    "${doc.category.titleFa} • ${FileStorageHelper.formatFileSize(doc.sizeBytes)} • ${PersianDateUtils.formatShamsiShort(doc.createdAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewingDoc = doc }) {
                                        Icon(Icons.Default.Visibility, contentDescription = "مشاهده")
                                    }
                                    IconButton(onClick = {
                                        FileStorageHelper.shareFile(context, doc.localPath, doc.mimeType, doc.title)
                                    }) {
                                        Icon(Icons.Default.Share, contentDescription = "ارسال")
                                    }
                                    if (canDelete) {
                                        IconButton(onClick = { viewModel.deleteDocument(doc) }) {
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

    viewingDoc?.let { doc ->
        DocumentViewerDialog(document = doc, onDismiss = { viewingDoc = null })
    }

    if (showSelectCaseForUpload && targetUploadCase != null) {
        var selectedCase by remember { mutableStateOf(targetUploadCase!!) }
        var stepUpload by remember { mutableStateOf(false) }

        if (!stepUpload) {
            Dialog(onDismissRequest = { showSelectCaseForUpload = false }) {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("انتخاب پرونده مقصد", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        DropdownSelectorField(
                            label = "پرونده",
                            options = uploadableCases,
                            selected = selectedCase,
                            optionLabel = { "${it.officeCaseNumber} - ${it.subject}" },
                            onSelected = { selectedCase = it }
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showSelectCaseForUpload = false }) { Text("انصراف") }
                            Spacer(Modifier.width(8.dp))
                            androidx.compose.material3.Button(onClick = { stepUpload = true }) {
                                Text("ادامه")
                            }
                        }
                    }
                }
            }
        } else {
            DocumentUploadDialog(
                onDismiss = { showSelectCaseForUpload = false },
                onConfirmUri = { uri, title, desc, cat, text ->
                    viewModel.addDocumentFromUri(context, selectedCase.id, uri, title, desc, cat, text)
                    showSelectCaseForUpload = false
                },
                onConfirmCameraFile = { file, title, desc, cat, text ->
                    viewModel.addDocumentFromCameraFile(context, selectedCase.id, file, title, desc, cat, text)
                    showSelectCaseForUpload = false
                }
            )
        }
    }
}
