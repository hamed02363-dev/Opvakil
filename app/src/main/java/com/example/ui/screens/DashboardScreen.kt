package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CaseStatus
import com.example.data.model.StageStatus
import com.example.data.model.UserRole
import com.example.ui.components.CaseStatusBadge
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.StatKpiCard
import com.example.ui.components.StatusPill
import com.example.ui.components.VakilTopBar
import com.example.ui.theme.BrassSecondary
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusCrimson
import com.example.ui.theme.StatusEmerald
import com.example.ui.theme.StatusIndigo
import com.example.ui.viewmodel.WorkspaceUiState
import com.example.util.PersianDateUtils

@Composable
fun DashboardScreen(
    state: WorkspaceUiState,
    onMenuClick: () -> Unit,
    onOpenCase: (Long) -> Unit,
    onNavigateToCases: () -> Unit,
    onNavigateToBriefs: () -> Unit,
    onNavigateToLegal: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToDocuments: () -> Unit
) {
    var quickQuery by remember { mutableStateOf("") }
    val user = state.currentUser

    val activeCases = remember(state.visibleCases) {
        state.visibleCases.filter { it.status != CaseStatus.CLOSED && it.status != CaseStatus.DISMISSED }
    }
    val upcomingHearings = remember(state.visibleCases) {
        val now = System.currentTimeMillis()
        val twoWeeks = 14L * 24 * 60 * 60 * 1000
        state.visibleCases
            .filter { it.nextHearingMillis != null && it.nextHearingMillis in (now - 86400000L)..(now + twoWeeks) }
            .sortedBy { it.nextHearingMillis }
    }
    val pendingStages = remember(state.visibleStages) {
        state.visibleStages.filter { it.status == StageStatus.PENDING_ACTION || it.status == StageStatus.IN_PROGRESS }
    }
    val pendingCasesCount = remember(state.visibleCases, pendingStages) {
        val caseIds = state.visibleCases.filter { it.hasPendingAction }.map { it.id }.toSet() +
            pendingStages.map { it.caseId }.toSet()
        caseIds.size
    }

    val searchCaseResults = remember(quickQuery, state.visibleCases) {
        if (quickQuery.isBlank()) emptyList()
        else state.visibleCases.filter {
            it.officeCaseNumber.contains(quickQuery, true) ||
                it.courtCaseNumber.contains(quickQuery, true) ||
                it.subject.contains(quickQuery, true) ||
                it.plaintiffName.contains(quickQuery, true) ||
                it.defendantName.contains(quickQuery, true)
        }
    }
    val searchDocResults = remember(quickQuery, state.visibleDocuments) {
        if (quickQuery.isBlank()) emptyList()
        else state.visibleDocuments.filter {
            it.title.contains(quickQuery, true) ||
                it.description.contains(quickQuery, true) ||
                it.extractedText.contains(quickQuery, true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        VakilTopBar(
            title = "داشبورد دفتر وکالت",
            subtitle = user?.let { "${it.fullName} • ${it.role.titleFa}" },
            onMenuClick = onMenuClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Executive Hero Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_legal_banner_1795617216388),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        NavyPrimary.copy(alpha = 0.94f),
                                        NavyPrimary.copy(alpha = 0.72f)
                                    )
                                )
                            )
                    )
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "خوش آمدید، ${user?.fullName ?: "وکیل گرامی"}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "امروز ${PersianDateUtils.formatShamsiLong(System.currentTimeMillis())}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFEBD7A7)
                        )
                        if (user?.lastLoginAt != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "آخرین ورود: ${PersianDateUtils.formatShamsiDateTime(user.lastLoginAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (user?.role == UserRole.LEAD_LAWYER) {
                                FilledTonalButton(
                                    onClick = onNavigateToCases,
                                    modifier = Modifier.testTag("dashboard_cases_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("مدیریت پرونده‌ها")
                                }
                            }
                            Surface(
                                color = BrassSecondary.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "۱۰۰٪ آفلاین",
                                    color = Color(0xFFFFE4A3),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Search across Cases and Documents
            item {
                OutlinedTextField(
                    value = quickQuery,
                    onValueChange = { quickQuery = it },
                    placeholder = { Text("جستجوی سریع در کلاسه، طرفین، موضوع یا مدارک...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "جستجو") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_quick_search")
                )
            }

            if (quickQuery.isNotBlank()) {
                item {
                    Text(
                        text = "نتایج جستجو (${PersianDateUtils.toPersianDigits((searchCaseResults.size + searchDocResults.size).toString())})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (searchCaseResults.isEmpty() && searchDocResults.isEmpty()) {
                    item {
                        EmptyStateBox(
                            icon = Icons.Default.Search,
                            title = "موردی یافت نشد",
                            subtitle = "عبارت دیگری را برای جستجو در پرونده‌ها و مدارک امتحان کنید."
                        )
                    }
                } else {
                    items(searchCaseResults) { c ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenCase(c.id) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${c.officeCaseNumber} - ${c.subject}", fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${c.plaintiffName} علیه ${c.defendantName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                CaseStatusBadge(c.status)
                            }
                        }
                    }
                    items(searchDocResults) { d ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenCase(d.caseId) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = BrassSecondary)
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(d.title, fontWeight = FontWeight.SemiBold)
                                    Text("مدرک (${d.category.titleFa} - ${d.fileType.titleFa})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            } else {
                // 2x2 KPIs
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatKpiCard(
                                title = "پرونده‌های فعال",
                                value = activeCases.size,
                                icon = Icons.Default.Folder,
                                accentColor = StatusIndigo,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToCases
                            )
                            StatKpiCard(
                                title = "جلسات نزدیک",
                                value = upcomingHearings.size,
                                icon = Icons.Default.Event,
                                accentColor = StatusAmber,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToCases
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatKpiCard(
                                title = "مدارک بایگانی‌شده",
                                value = state.visibleDocuments.size,
                                icon = Icons.Default.Description,
                                accentColor = StatusEmerald,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToDocuments
                            )
                            StatKpiCard(
                                title = "در انتظار اقدام",
                                value = pendingCasesCount,
                                icon = Icons.Default.PendingActions,
                                accentColor = StatusCrimson,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToReports
                            )
                        }
                    }
                }

                // Quick Navigation Shortcuts
                item {
                    Text("دسترسی سریع بخش‌ها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickShortcutTile("لوایح و الگوها", Icons.Default.EditNote, Modifier.weight(1f), onNavigateToBriefs)
                        QuickShortcutTile("قوانین و آراء", Icons.Default.MenuBook, Modifier.weight(1f), onNavigateToLegal)
                        QuickShortcutTile("گزارش PDF", Icons.Default.Assessment, Modifier.weight(1f), onNavigateToReports)
                    }
                }

                // Upcoming Hearings
                item {
                    Text("جلسات رسیدگی نزدیک (۱۴ روز آینده)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                if (upcomingHearings.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "جلسه نزدیکی در ۱۴ روز آینده ثبت نشده است.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(upcomingHearings) { c ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenCase(c.id) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${c.officeCaseNumber} • ${c.subject}", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Spacer(Modifier.height(2.dp))
                                    Text(c.authorityBranch.ifBlank { c.authorityType.titleFa }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        PersianDateUtils.formatShamsiDateTime(c.nextHearingMillis),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                StatusPill(
                                    text = PersianDateUtils.relativeDaysLabel(c.nextHearingMillis),
                                    fgColor = StatusAmber,
                                    bgColor = Color(0xFFFFF1D6)
                                )
                            }
                        }
                    }
                }

                // Pending Stages
                item {
                    Text("مراحل و کارهای در انتظار پیگیری", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                if (pendingStages.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "همه مراحل ثبت‌شده تکمیل شده‌اند.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(pendingStages.take(5)) { st ->
                        val parentCase = state.visibleCases.firstOrNull { it.id == st.caseId }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenCase(st.caseId) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(st.title, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${parentCase?.officeCaseNumber ?: ""} • ${PersianDateUtils.formatShamsiShort(st.dateMillis)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusPill(
                                    text = st.status.titleFa,
                                    fgColor = StatusCrimson,
                                    bgColor = Color(0xFFFDE8E8)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickShortcutTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
