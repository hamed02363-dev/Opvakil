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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import com.example.data.model.AccessLevel
import com.example.data.model.StageStatus
import com.example.data.model.UserRole
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.StageStatusBadge
import com.example.ui.components.VakilTopBar
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.WorkspaceUiState
import com.example.util.PersianDateUtils

@Composable
fun StagesTrackerScreen(
    state: WorkspaceUiState,
    viewModel: MainViewModel,
    onMenuClick: () -> Unit,
    onOpenCase: (Long) -> Unit
) {
    var selectedStatus by remember { mutableStateOf<StageStatus?>(null) }

    val filteredStages = remember(state.visibleStages, selectedStatus) {
        state.visibleStages.filter { selectedStatus == null || it.status == selectedStatus }
    }

    Scaffold(
        topBar = {
            VakilTopBar(
                title = "پیگیری مراحل پرونده‌ها",
                subtitle = PersianDateUtils.toPersianDigits("${filteredStages.size} مرحله ثبت‌شده"),
                onMenuClick = onMenuClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = { selectedStatus = null },
                    label = { Text("همه مراحل") }
                )
                StageStatus.entries.forEach { st ->
                    FilterChip(
                        selected = selectedStatus == st,
                        onClick = { selectedStatus = if (selectedStatus == st) null else st },
                        label = { Text(st.titleFa) }
                    )
                }
            }

            if (filteredStages.isEmpty()) {
                EmptyStateBox(
                    icon = Icons.Default.Timeline,
                    title = "مرحله‌ای یافت نشد",
                    subtitle = "برای ثبت مراحل جدید وارد صفحه پرونده مورد نظر شوید."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredStages, key = { it.id }) { st ->
                        val parentCase = state.visibleCases.firstOrNull { it.id == st.caseId }
                        val canUpdate = parentCase != null && (
                            state.currentUser?.role == UserRole.LEAD_LAWYER ||
                                state.accessFor(parentCase) == AccessLevel.FULL_CASE_ACCESS ||
                                state.accessFor(parentCase) == AccessLevel.REGISTER_STAGES
                            )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenCase(st.caseId) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(st.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    StageStatusBadge(st.status)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "پرونده: ${parentCase?.officeCaseNumber ?: "—"} • ${st.stageType.titleFa}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "تاریخ: ${PersianDateUtils.formatShamsiLong(st.dateMillis)} • مسئول: ${st.responsibleName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (st.description.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(st.description, style = MaterialTheme.typography.bodySmall)
                                }
                                if (canUpdate && st.status != StageStatus.COMPLETED) {
                                    TextButton(onClick = { viewModel.updateStageStatus(st, StageStatus.COMPLETED) }) {
                                        Text("علامت‌گذاری به‌عنوان انجام‌شده")
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
