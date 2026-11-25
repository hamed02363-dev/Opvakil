package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AccessLevel
import com.example.data.model.CaseEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.components.DropdownSelectorField
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.StatusPill
import com.example.ui.components.VakilTopBar
import com.example.ui.theme.StatusCrimson
import com.example.ui.theme.StatusCrimsonBg
import com.example.ui.theme.StatusEmerald
import com.example.ui.theme.StatusEmeraldBg
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.WorkspaceUiState
import com.example.util.PersianDateUtils

@Composable
fun TeamAccessScreen(
    state: WorkspaceUiState,
    viewModel: MainViewModel,
    onMenuClick: () -> Unit
) {
    val isLead = state.currentUser?.role == UserRole.LEAD_LAWYER
    var showAddAssociate by remember { mutableStateOf(false) }
    var assignForUser by remember { mutableStateOf<UserEntity?>(null) }

    val associates = remember(state.allUsers) {
        state.allUsers.filter { it.role == UserRole.ASSOCIATE }
    }

    Scaffold(
        topBar = {
            VakilTopBar(
                title = "مدیریت دسترسی همکاران",
                subtitle = "کنترل سطح دسترسی وکلای همکار و کارمندان دفتر",
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            if (isLead) {
                FloatingActionButton(onClick = { showAddAssociate = true }) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "افزودن همکار")
                }
            }
        }
    ) { innerPadding ->
        if (!isLead) {
            Column(modifier = Modifier.padding(innerPadding)) {
                EmptyStateBox(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "محدود به وکیل اصلی",
                    subtitle = "تنها وکیل اصلی (مدیر دفتر) مجاز به تنظیم دسترسی همکاران است."
                )
            }
            return@Scaffold
        }

        if (associates.isEmpty()) {
            Column(modifier = Modifier.padding(innerPadding)) {
                EmptyStateBox(
                    icon = Icons.Default.PersonAdd,
                    title = "همکاری ثبت نشده است",
                    subtitle = "با دکمه + می‌توانید حساب وکیل همکار یا کارمند دفتر ایجاد کرده و پرونده‌ها را اختصاص دهید.",
                    actionLabel = "افزودن همکار جدید",
                    onAction = { showAddAssociate = true }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(associates, key = { it.id }) { assoc ->
                    val grants = state.allCaseAccess.filter { it.userId == assoc.id }
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(assoc.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("شناسه ورود: ${assoc.identifier}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "آخرین ورود: ${PersianDateUtils.formatShamsiDateTime(assoc.lastLoginAt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusPill(
                                    text = if (assoc.isActive) "فعال" else "غیرفعال",
                                    fgColor = if (assoc.isActive) StatusEmerald else StatusCrimson,
                                    bgColor = if (assoc.isActive) StatusEmeraldBg else StatusCrimsonBg
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("وضعیت حساب کاربری", style = MaterialTheme.typography.bodyMedium)
                                Switch(
                                    checked = assoc.isActive,
                                    onCheckedChange = { viewModel.toggleUserActive(assoc.id, it) }
                                )
                            }

                            HorizontalDivider()

                            Text("پرونده‌های اختصاص‌یافته:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            if (grants.isEmpty()) {
                                Text("هیچ پرونده‌ای برای این کاربر تعریف نشده است.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                grants.forEach { g ->
                                    val c = state.visibleCases.firstOrNull { it.id == g.caseId }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(c?.let { "${it.officeCaseNumber} - ${it.subject}" } ?: "پرونده #${g.caseId}", fontWeight = FontWeight.Medium)
                                            Text(
                                                "${g.accessLevel.titleFa} • ${if (g.isEnabled) "دسترسی برقرار" else "معلق"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Switch(
                                            checked = g.isEnabled,
                                            onCheckedChange = { enabled ->
                                                viewModel.assignCaseAccess(g.caseId, assoc.id, g.accessLevel, enabled)
                                            }
                                        )
                                        IconButton(onClick = { viewModel.revokeCaseAccess(g.caseId, assoc.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "لغو دسترسی", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }

                            if (state.visibleCases.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { assignForUser = assoc },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("تخصیص پرونده / تغییر سطح دسترسی")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddAssociate) {
        AddAssociateDialog(
            onDismiss = { showAddAssociate = false },
            onAdd = { name, id, pwd, lic ->
                viewModel.addAssociateUser(name, id, pwd, lic)
                showAddAssociate = false
            }
        )
    }

    assignForUser?.let { assoc ->
        AssignCaseAccessDialog(
            user = assoc,
            cases = state.visibleCases,
            onDismiss = { assignForUser = null },
            onAssign = { caseEntity, level ->
                viewModel.assignCaseAccess(caseEntity.id, assoc.id, level, true)
                assignForUser = null
            }
        )
    }
}

@Composable
private fun AddAssociateDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("افزودن وکیل همکار / کارمند", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام و نام خانوادگی *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = identifier, onValueChange = { identifier = it }, label = { Text("شماره موبایل یا ایمیل *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("رمز عبور اولیه (حداقل ۴ کاراکتر) *") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(value = license, onValueChange = { license = it }, label = { Text("شماره پروانه / سمت") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Button(
                        enabled = name.isNotBlank() && identifier.isNotBlank() && password.length >= 4,
                        onClick = { onAdd(name, identifier, password, license) }
                    ) { Text("ایجاد حساب") }
                }
            }
        }
    }
}

@Composable
private fun AssignCaseAccessDialog(
    user: UserEntity,
    cases: List<CaseEntity>,
    onDismiss: () -> Unit,
    onAssign: (CaseEntity, AccessLevel) -> Unit
) {
    var selectedCase by remember { mutableStateOf(cases.first()) }
    var selectedLevel by remember { mutableStateOf(AccessLevel.VIEW_ONLY) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("تعیین دسترسی برای ${user.fullName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                DropdownSelectorField(
                    label = "انتخاب پرونده",
                    options = cases,
                    selected = selectedCase,
                    optionLabel = { "${it.officeCaseNumber} - ${it.subject}" },
                    onSelected = { selectedCase = it }
                )
                DropdownSelectorField(
                    label = "سطح دسترسی",
                    options = AccessLevel.entries,
                    selected = selectedLevel,
                    optionLabel = { it.titleFa },
                    onSelected = { selectedLevel = it }
                )
                Text(
                    selectedLevel.descriptionFa,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onAssign(selectedCase, selectedLevel) }) {
                        Text("ذخیره دسترسی")
                    }
                }
            }
        }
    }
}
