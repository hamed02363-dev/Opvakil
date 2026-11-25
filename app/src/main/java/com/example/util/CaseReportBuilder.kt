package com.example.util

import com.example.data.model.CaseEntity
import com.example.data.model.CaseLegalRefEntity
import com.example.data.model.CaseStageEntity
import com.example.data.model.DocumentEntity
import com.example.data.model.StageStatus

object CaseReportBuilder {

    fun buildLatestStatusReport(
        caseEntity: CaseEntity,
        stages: List<CaseStageEntity>,
        documentsCount: Int
    ): String {
        val sortedStages = stages.sortedByDescending { it.dateMillis }
        val latestStage = sortedStages.firstOrNull()
        val pendingStages = sortedStages.filter {
            it.status == StageStatus.PENDING_ACTION || it.status == StageStatus.IN_PROGRESS
        }

        val sb = StringBuilder()
        sb.appendLine("گزارش آخرین وضعیت پرونده")
        sb.appendLine("کلاسه دفتر: ${caseEntity.officeCaseNumber}")
        if (caseEntity.courtCaseNumber.isNotBlank()) {
            sb.appendLine("شماره پرونده مرجع: ${caseEntity.courtCaseNumber}")
        }
        sb.appendLine("موضوع: ${caseEntity.subject}")
        sb.appendLine("خواهان / شاکی: ${caseEntity.plaintiffName}")
        sb.appendLine("خوانده / متهم: ${caseEntity.defendantName}")
        sb.appendLine("مرجع رسیدگی: ${caseEntity.authorityType.titleFa} (${caseEntity.authorityBranch.ifBlank { "ثبت نشده" }})")
        sb.appendLine("وضعیت فعلی: ${caseEntity.status.titleFa}")
        if (caseEntity.nextHearingMillis != null) {
            sb.appendLine("جلسه بعدی: ${PersianDateUtils.formatShamsiDateTime(caseEntity.nextHearingMillis)}")
        }
        if (latestStage != null) {
            sb.appendLine("آخرین اقدام ثبت‌شده: ${latestStage.title} (${PersianDateUtils.formatShamsiShort(latestStage.dateMillis)} - ${latestStage.status.titleFa})")
        }
        if (pendingStages.isNotEmpty()) {
            sb.appendLine("موارد در انتظار پیگیری: ${pendingStages.joinToString("، ") { it.title }}")
        }
        sb.appendLine("تعداد مدارک بایگانی‌شده: ${PersianDateUtils.toPersianDigits(documentsCount.toString())} فقره")
        return sb.toString().trim()
    }

    fun buildComprehensiveCaseSummary(
        caseEntity: CaseEntity,
        plainLawyerNotes: String,
        stages: List<CaseStageEntity>,
        documents: List<DocumentEntity>,
        legalRefs: List<CaseLegalRefEntity>
    ): String {
        val sb = StringBuilder()
        sb.appendLine("خلاصه جامع و ساختاریافته پرونده")
        sb.appendLine("════════════════════")
        sb.appendLine("• کلاسه داخلی: ${caseEntity.officeCaseNumber} | شماره دادگاه/مرجع: ${caseEntity.courtCaseNumber.ifBlank { "—" }}")
        sb.appendLine("• نوع دعوا: ${caseEntity.caseType.titleFa} | وضعیت: ${caseEntity.status.titleFa}")
        sb.appendLine("• مرجع: ${caseEntity.authorityType.titleFa} - ${caseEntity.authorityBranch}")
        sb.appendLine("• خواهان/شاکی: ${caseEntity.plaintiffName}")
        sb.appendLine("• خوانده/متهم: ${caseEntity.defendantName}")
        if (caseEntity.opposingCounsel.isNotBlank()) {
            sb.appendLine("• وکیل طرف مقابل: ${caseEntity.opposingCounsel}")
        }
        sb.appendLine("• تاریخ طرح دعوا: ${PersianDateUtils.formatShamsiShort(caseEntity.filingDateMillis)}")
        sb.appendLine("• تاریخ جلسه بعدی: ${PersianDateUtils.formatShamsiDateTime(caseEntity.nextHearingMillis)}")
        sb.appendLine()

        if (caseEntity.summary.isNotBlank()) {
            sb.appendLine("شرح خلاصه پرونده:")
            sb.appendLine(caseEntity.summary)
            sb.appendLine()
        }

        if (plainLawyerNotes.isNotBlank()) {
            sb.appendLine("یادداشت‌های محرمانه وکیل:")
            sb.appendLine(plainLawyerNotes)
            sb.appendLine()
        }

        sb.appendLine("روند مراحل دادرسی (${PersianDateUtils.toPersianDigits(stages.size.toString())} مرحله):")
        if (stages.isEmpty()) {
            sb.appendLine("هنوز مرحله‌ای ثبت نشده است.")
        } else {
            stages.sortedBy { it.dateMillis }.forEachIndexed { idx, st ->
                sb.appendLine("${PersianDateUtils.toPersianDigits((idx + 1).toString())}. ${st.title} [${st.status.titleFa}] - ${PersianDateUtils.formatShamsiShort(st.dateMillis)}")
                if (st.description.isNotBlank()) sb.appendLine("   توضیح: ${st.description}")
            }
        }
        sb.appendLine()

        sb.appendLine("فهرست مدارک پرونده (${PersianDateUtils.toPersianDigits(documents.size.toString())} مدرک):")
        if (documents.isEmpty()) {
            sb.appendLine("مدرکی بارگذاری نشده است.")
        } else {
            documents.forEachIndexed { idx, doc ->
                sb.appendLine("${PersianDateUtils.toPersianDigits((idx + 1).toString())}. ${doc.title} (${doc.category.titleFa} - ${doc.fileType.titleFa})")
                if (doc.extractedText.isNotBlank()) {
                    sb.appendLine("   متن/خلاصه سند: ${doc.extractedText.take(180)}")
                }
            }
        }

        if (legalRefs.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("مستندات قانونی و آرای پیوست‌شده:")
            legalRefs.forEach { ref ->
                sb.appendLine("• ${ref.refTitle} (${ref.refSubtitle})")
            }
        }
        return sb.toString().trim()
    }
}
