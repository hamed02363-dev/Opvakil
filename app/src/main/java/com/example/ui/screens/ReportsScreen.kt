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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.StageStatus
import com.example.data.model.UserRole
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.VakilTopBar
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.PdfActionMode
import com.example.ui.viewmodel.WorkspaceUiState
import com.example.util.CaseReportBuilder
import com.example.util.FileStorageHelper
import com.example.util.PersianDateUtils

private enum class ReportType(val titleFa: String) {
    ALL_CASES("وضعیت تمام پرونده‌ها"),
    UPCOMING_HEARINGS("پرونده‌های دارای جلسه نزدیک"),
    PENDING_ACTION("در انتظار اقدام وکیل"),
    BY_ASSOCIATE("پرونده‌های هر وکیل همکار"),
    DOCUMENTS("مدارک بارگذاری‌شده"),
    LATEST_STATUS("آخرین وضعیت هر پرونده")
}

@Composable
fun ReportsScreen(
    state: WorkspaceUiState,
    viewModel: MainViewModel,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedReport by remember { mutableStateOf(ReportType.ALL_CASES) }

    val reportSections: List<Pair<String, String>> = remember(selectedReport, state) {
        when (selectedReport) {
            ReportType.ALL_CASES -> {
                if (state.visibleCases.isEmpty()) emptyList()
                else state.visibleCases.map { c ->
                    "کلاسه ${c.officeCaseNumber} | ${c.status.titleFa}" to
                        "موضوع: ${c.subject}\nنوع: ${c.caseType.titleFa} | مرجع: ${c.authorityBranch.ifBlank { c.authorityType.titleFa }}\nخواهان: ${c.plaintiffName} | خوانده: ${c.defendantName}\nجلسه بعدی: ${PersianDateUtils.formatShamsiDateTime(c.nextHearingMillis)}"
                }
            }
            ReportType.UPCOMING_HEARINGS -> {
                state.visibleCases
                    .filter { it.nextHearingMillis != null }
                    .sortedBy { it.nextHearingMillis }
                    .map { c ->
                        "${c.officeCaseNumber} - ${PersianDateUtils.formatShamsiDateTime(c.nextHearingMillis)}" to
                            "موضوع: ${c.subject}\nمرجع: ${c.authorityBranch.ifBlank { c.authorityType.titleFa }}\nوضعیت زمانی: ${PersianDateUtils.relativeDaysLabel(c.nextHearingMillis)}"
                    }
            }
            ReportType.PENDING_ACTION -> {
                val pendingStageCaseIds = state.visibleStages
                    .filter { it.status == StageStatus.PENDING_ACTION || it.status == StageStatus.IN_PROGRESS }
                    .map { it.caseId }
                    .toSet()
                state.visibleCases
                    .filter { it.hasPendingAction || it.id in pendingStageCaseIds }
                    .map { c ->
                        val pendingTitles = state.visibleStages
                            .filter { it.caseId == c.id && (it.status == StageStatus.PENDING_ACTION || it.status == StageStatus.IN_PROGRESS) }
                            .joinToString("، ") { it.title }
                        "کلاسه ${c.officeCaseNumber} (${c.status.titleFa})" to
                            "موضوع: ${c.subject}\nمراحل در انتظار: ${pendingTitles.ifBlank { "نیازمند بررسی فوری وکیل" }}\nمهلت مهم: ${c.importantDatesNote.ifBlank { "ثبت نشده" }}"
                    }
            }
            ReportType.BY_ASSOCIATE -> {
                val associates = state.allUsers.filter { it.role == UserRole.ASSOCIATE }
                associates.map { assoc ->
                    val grants = state.allCaseAccess.filter { it.userId == assoc.id && it.isEnabled }
                    val lines = if (grants.isEmpty()) {
                        "پرونده‌ای به این همکار اختصاص داده نشده است."
                    } else {
                        grants.mapNotNull { g ->
                            val c = state.visibleCases.firstOrNull { it.id == g.caseId }
                            c?.let { "• ${it.officeCaseNumber}: ${it.subject} [${g.accessLevel.titleFa}]" }
                        }.joinToString("\n").ifBlank { "پرونده‌ای در دسترس نیست." }
                    }
                    "${assoc.fullName} (${if (assoc.isActive) "فعال" else "غیرفعال"})" to lines
                }
            }
            ReportType.DOCUMENTS -> {
                state.visibleDocuments.map { d ->
                    val parent = state.visibleCases.firstOrNull { it.id == d.caseId }
                    d.title to
                        "پرونده: ${parent?.officeCaseNumber ?: "—"} | دسته: ${d.category.titleFa} | فرمت: ${d.fileType.titleFa}\nحجم: ${FileStorageHelper.formatFileSize(d.sizeBytes)} | ثبت: ${PersianDateUtils.formatShamsiShort(d.createdAt)} توسط ${d.uploadedByName}"
                }
            }
            ReportType.LATEST_STATUS -> {
                state.visibleCases.map { c ->
                    val caseStages = state.visibleStages.filter { it.caseId == c.id }
                    val docsCount = state.visibleDocuments.count { it.caseId == c.id }
                    "پرونده ${c.officeCaseNumber}" to CaseReportBuilder.buildLatestStatusReport(c, caseStages, docsCount)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            VakilTopBar(
                title = "گزارش‌گیری و خروجی",
                subtitle = "تولید PDF، اشتراک‌گذاری و چاپ مستقیم آفلاین",
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
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ReportType.entries.forEach { rt ->
                    FilterChip(
                        selected = selectedReport == rt,
                        onClick = { selectedReport = rt },
                        label = { Text(rt.titleFa) }
                    )
                }
            }

            // Export / Print / Share Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    enabled = reportSections.isNotEmpty(),
                    onClick = {
                        viewModel.exportReportPdf(
                            context = context,
                            title = selectedReport.titleFa,
                            subtitle = "تعداد رکورد: ${PersianDateUtils.toPersianDigits(reportSections.size.toString())}",
                            sections = reportSections,
                            mode = PdfActionMode.VIEW
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("PDF")
                }
                FilledTonalButton(
                    enabled = reportSections.isNotEmpty(),
                    onClick = {
                        viewModel.exportReportPdf(
                            context = context,
                            title = selectedReport.titleFa,
                            subtitle = "گزارش دفتر وکالت وکیل‌یار",
                            sections = reportSections,
                            mode = PdfActionMode.SHARE
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("ارسال")
                }
                OutlinedButton(
                    enabled = reportSections.isNotEmpty(),
                    onClick = {
                        viewModel.exportReportPdf(
                            context = context,
                            title = selectedReport.titleFa,
                            subtitle = "چاپ مستقیم از اپلیکیشن وکیل‌یار",
                            sections = reportSections,
                            mode = PdfActionMode.PRINT
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("چاپ")
                }
            }

            Spacer(Modifier.height(8.dp))

            if (reportSections.isEmpty()) {
                EmptyStateBox(
                    icon = Icons.Default.Assessment,
                    title = "داده‌ای برای این گزارش وجود ندارد",
                    subtitle = "با ثبت پرونده یا مدرک جدید، گزارش به‌صورت خودکار به‌روزرسانی می‌شود."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reportSections) { (title, body) ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(6.dp))
                                Text(body, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
