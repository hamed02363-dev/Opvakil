package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CaseEntity
import com.example.data.model.LawCategory
import com.example.data.model.LawEntity
import com.example.data.model.LegalRefType
import com.example.data.model.RulingAuthority
import com.example.data.model.RulingEntity
import com.example.ui.components.DropdownSelectorField
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.VakilTopBar
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.WorkspaceUiState
import com.example.util.PersianDateUtils

@Composable
fun LegalResourcesScreen(
    state: WorkspaceUiState,
    viewModel: MainViewModel,
    onMenuClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedLawCat by remember { mutableStateOf<LawCategory?>(null) }
    var selectedRulingAuth by remember { mutableStateOf<RulingAuthority?>(null) }

    var editingLaw by remember { mutableStateOf<LawEntity?>(null) }
    var createLaw by remember { mutableStateOf(false) }
    var editingRuling by remember { mutableStateOf<RulingEntity?>(null) }
    var createRuling by remember { mutableStateOf(false) }

    var attachTarget by remember { mutableStateOf<Triple<LegalRefType, Long, Pair<String, String>>?>(null) }

    val filteredLaws = remember(state.allLaws, searchQuery, selectedLawCat) {
        state.allLaws.filter { l ->
            val qMatch = searchQuery.isBlank() ||
                l.lawName.contains(searchQuery, true) ||
                l.articleNumber.contains(searchQuery, true) ||
                l.fullText.contains(searchQuery, true) ||
                l.personalNote.contains(searchQuery, true)
            val cMatch = selectedLawCat == null || l.category == selectedLawCat
            qMatch && cMatch
        }
    }

    val filteredRulings = remember(state.allRulings, searchQuery, selectedRulingAuth) {
        state.allRulings.filter { r ->
            val qMatch = searchQuery.isBlank() ||
                r.title.contains(searchQuery, true) ||
                r.rulingNumber.contains(searchQuery, true) ||
                r.topicTag.contains(searchQuery, true) ||
                r.keywords.contains(searchQuery, true) ||
                r.persianYear.contains(searchQuery, true) ||
                r.fullText.contains(searchQuery, true)
            val aMatch = selectedRulingAuth == null || r.authority == selectedRulingAuth
            qMatch && aMatch
        }
    }

    Scaffold(
        topBar = {
            VakilTopBar(
                title = "قوانین و آرای قضایی",
                subtitle = "بانک آفلاین مقررات، آرای وحدت رویه و یادداشت‌های وکیل",
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        editingLaw = null; createLaw = true
                    } else {
                        editingRuling = null; createRuling = true
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن منبع حقوقی")
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
                    text = { Text("قوانین و مقررات (${PersianDateUtils.toPersianDigits(filteredLaws.size.toString())})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("آرای قضایی و وحدت رویه (${PersianDateUtils.toPersianDigits(filteredRulings.size.toString())})") }
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("جستجوی پیشرفته در عنوان، ماده، سال یا کلیدواژه‌ها...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (selectedTab == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(selected = selectedLawCat == null, onClick = { selectedLawCat = null }, label = { Text("همه") })
                    LawCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedLawCat == cat,
                            onClick = { selectedLawCat = if (selectedLawCat == cat) null else cat },
                            label = { Text(cat.titleFa) }
                        )
                    }
                }
                if (filteredLaws.isEmpty()) {
                    EmptyStateBox(
                        icon = Icons.Default.MenuBook,
                        title = "ماده قانونی یافت نشد",
                        subtitle = "با دکمه + می‌توانید قوانین و یادداشت‌های شخصی خود را اضافه کنید."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredLaws, key = { it.id }) { law ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${law.lawName} • ${law.articleNumber}", fontWeight = FontWeight.Bold)
                                        Text(law.category.titleFa, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    if (law.tabsera.isNotBlank() && law.tabsera != "ندارد") {
                                        Text("تبصره: ${law.tabsera}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                                    }
                                    Text(law.fullText, style = MaterialTheme.typography.bodyMedium)
                                    if (law.personalNote.isNotBlank()) {
                                        Text(
                                            "یادداشت وکیل: ${law.personalNote}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (state.visibleCases.isNotEmpty()) {
                                            TextButton(onClick = {
                                                attachTarget = Triple(
                                                    LegalRefType.LAW,
                                                    law.id,
                                                    "${law.lawName} - ${law.articleNumber}" to law.category.titleFa
                                                )
                                            }) {
                                                Icon(Icons.Default.AttachFile, contentDescription = null)
                                                Spacer(Modifier.width(4.dp))
                                                Text("پیوست به پرونده")
                                            }
                                        }
                                        IconButton(onClick = { editingLaw = law; createLaw = false }) {
                                            Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                                        }
                                        IconButton(onClick = { viewModel.deleteLaw(law.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(selected = selectedRulingAuth == null, onClick = { selectedRulingAuth = null }, label = { Text("همه مراجع") })
                    RulingAuthority.entries.forEach { auth ->
                        FilterChip(
                            selected = selectedRulingAuth == auth,
                            onClick = { selectedRulingAuth = if (selectedRulingAuth == auth) null else auth },
                            label = { Text(auth.titleFa) }
                        )
                    }
                }
                if (filteredRulings.isEmpty()) {
                    EmptyStateBox(
                        icon = Icons.Default.Gavel,
                        title = "رأی قضایی یافت نشد",
                        subtitle = "آرای وحدت رویه و آرای منتخب خود را با دکمه + ثبت کنید."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredRulings, key = { it.id }) { r ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("${r.rulingNumber} (${r.persianYear})", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                    Text(r.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${r.authority.titleFa} • موضوع: ${r.topicTag}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    Text(r.fullText, style = MaterialTheme.typography.bodyMedium)
                                    if (r.keywords.isNotBlank()) {
                                        Text("کلیدواژه‌ها: ${r.keywords}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (r.personalNote.isNotBlank()) {
                                        Text("یادداشت وکیل: ${r.personalNote}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (state.visibleCases.isNotEmpty()) {
                                            TextButton(onClick = {
                                                attachTarget = Triple(
                                                    LegalRefType.RULING,
                                                    r.id,
                                                    "${r.rulingNumber}: ${r.title}" to "${r.authority.titleFa} (${r.persianYear})"
                                                )
                                            }) {
                                                Icon(Icons.Default.AttachFile, contentDescription = null)
                                                Spacer(Modifier.width(4.dp))
                                                Text("پیوست به پرونده")
                                            }
                                        }
                                        IconButton(onClick = { editingRuling = r; createRuling = false }) {
                                            Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                                        }
                                        IconButton(onClick = { viewModel.deleteRuling(r.id) }) {
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

    if (createLaw || editingLaw != null) {
        LawFormDialog(
            initial = editingLaw,
            onDismiss = { createLaw = false; editingLaw = null },
            onSave = {
                viewModel.saveLaw(it)
                createLaw = false
                editingLaw = null
            }
        )
    }

    if (createRuling || editingRuling != null) {
        RulingFormDialog(
            initial = editingRuling,
            onDismiss = { createRuling = false; editingRuling = null },
            onSave = {
                viewModel.saveRuling(it)
                createRuling = false
                editingRuling = null
            }
        )
    }

    attachTarget?.let { (refType, refId, titlePair) ->
        var selectedCase by remember { mutableStateOf<CaseEntity>(state.visibleCases.first()) }
        Dialog(onDismissRequest = { attachTarget = null }) {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("پیوست به پرونده", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    DropdownSelectorField(
                        label = "انتخاب پرونده",
                        options = state.visibleCases,
                        selected = selectedCase,
                        optionLabel = { "${it.officeCaseNumber} - ${it.subject}" },
                        onSelected = { selectedCase = it }
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { attachTarget = null }) { Text("انصراف") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            viewModel.attachLegalRefToCase(
                                caseId = selectedCase.id,
                                refType = refType,
                                refId = refId,
                                title = titlePair.first,
                                subtitle = titlePair.second
                            )
                            attachTarget = null
                        }) {
                            Text("پیوست شود")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LawFormDialog(initial: LawEntity?, onDismiss: () -> Unit, onSave: (LawEntity) -> Unit) {
    var lawName by remember { mutableStateOf(initial?.lawName ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: LawCategory.CIVIL) }
    var articleNumber by remember { mutableStateOf(initial?.articleNumber ?: "") }
    var tabsera by remember { mutableStateOf(initial?.tabsera ?: "") }
    var fullText by remember { mutableStateOf(initial?.fullText ?: "") }
    var personalNote by remember { mutableStateOf(initial?.personalNote ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(if (initial == null) "افزودن قانون / ماده" else "ویرایش ماده قانونی", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = lawName, onValueChange = { lawName = it }, label = { Text("نام قانون") }, modifier = Modifier.fillMaxWidth())
                DropdownSelectorField(label = "دسته‌بندی", options = LawCategory.entries, selected = category, optionLabel = { it.titleFa }, onSelected = { category = it })
                OutlinedTextField(value = articleNumber, onValueChange = { articleNumber = it }, label = { Text("شماره ماده") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tabsera, onValueChange = { tabsera = it }, label = { Text("تبصره (اختیاری)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = fullText, onValueChange = { fullText = it }, label = { Text("متن کامل قانون") }, minLines = 4, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = personalNote, onValueChange = { personalNote = it }, label = { Text("یادداشت شخصی وکیل") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Button(onClick = {
                        onSave(
                            LawEntity(
                                id = initial?.id ?: 0L,
                                lawName = lawName.ifBlank { "قانون" },
                                category = category,
                                articleNumber = articleNumber.ifBlank { "ماده" },
                                tabsera = tabsera,
                                fullText = fullText,
                                personalNote = personalNote
                            )
                        )
                    }) { Text("ذخیره") }
                }
            }
        }
    }
}

@Composable
private fun RulingFormDialog(initial: RulingEntity?, onDismiss: () -> Unit, onSave: (RulingEntity) -> Unit) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var rulingNumber by remember { mutableStateOf(initial?.rulingNumber ?: "") }
    var authority by remember { mutableStateOf(initial?.authority ?: RulingAuthority.SUPREME_COURT_UNITY) }
    var persianYear by remember { mutableStateOf(initial?.persianYear ?: "۱۴۰۵") }
    var topicTag by remember { mutableStateOf(initial?.topicTag ?: "") }
    var keywords by remember { mutableStateOf(initial?.keywords ?: "") }
    var fullText by remember { mutableStateOf(initial?.fullText ?: "") }
    var personalNote by remember { mutableStateOf(initial?.personalNote ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(if (initial == null) "افزودن رأی قضایی" else "ویرایش رأی قضایی", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان رأی") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rulingNumber, onValueChange = { rulingNumber = it }, label = { Text("شماره رأی / دادنامه") }, modifier = Modifier.fillMaxWidth())
                DropdownSelectorField(label = "مرجع صادرکننده", options = RulingAuthority.entries, selected = authority, optionLabel = { it.titleFa }, onSelected = { authority = it })
                OutlinedTextField(value = persianYear, onValueChange = { persianYear = it }, label = { Text("سال صدور") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = topicTag, onValueChange = { topicTag = it }, label = { Text("برچسب موضوع") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = keywords, onValueChange = { keywords = it }, label = { Text("کلمات کلیدی") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = fullText, onValueChange = { fullText = it }, label = { Text("متن رأی") }, minLines = 4, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = personalNote, onValueChange = { personalNote = it }, label = { Text("یادداشت شخصی وکیل") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Button(onClick = {
                        onSave(
                            RulingEntity(
                                id = initial?.id ?: 0L,
                                title = title.ifBlank { "رأی قضایی" },
                                rulingNumber = rulingNumber,
                                authority = authority,
                                persianYear = persianYear,
                                topicTag = topicTag,
                                keywords = keywords,
                                fullText = fullText,
                                personalNote = personalNote
                            )
                        )
                    }) { Text("ذخیره") }
                }
            }
        }
    }
}
