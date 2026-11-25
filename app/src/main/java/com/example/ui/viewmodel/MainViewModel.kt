package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AccessLevel
import com.example.data.model.BriefEntity
import com.example.data.model.CaseAccessEntity
import com.example.data.model.CaseEntity
import com.example.data.model.CaseLegalRefEntity
import com.example.data.model.CaseStageEntity
import com.example.data.model.DocumentCategory
import com.example.data.model.DocumentEntity
import com.example.data.model.LawEntity
import com.example.data.model.LegalRefType
import com.example.data.model.RulingEntity
import com.example.data.model.StageStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.VakilRepository
import com.example.reminder.ReminderScheduler
import com.example.util.CaseReportBuilder
import com.example.util.FileStorageHelper
import com.example.util.PdfExportHelper
import com.example.util.PersianDateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class WorkspaceUiState(
    val currentUser: UserEntity? = null,
    val allUsers: List<UserEntity> = emptyList(),
    val visibleCases: List<CaseEntity> = emptyList(),
    val allCaseAccess: List<CaseAccessEntity> = emptyList(),
    val visibleDocuments: List<DocumentEntity> = emptyList(),
    val visibleStages: List<CaseStageEntity> = emptyList(),
    val allBriefs: List<BriefEntity> = emptyList(),
    val allLaws: List<LawEntity> = emptyList(),
    val allRulings: List<RulingEntity> = emptyList(),
    val allCaseLegalRefs: List<CaseLegalRefEntity> = emptyList(),
    val isDarkTheme: Boolean = false
) {
    fun accessFor(caseEntity: CaseEntity): AccessLevel? {
        val user = currentUser ?: return null
        if (user.role == UserRole.LEAD_LAWYER) return AccessLevel.FULL_CASE_ACCESS
        if (caseEntity.ownerUserId == user.id) return AccessLevel.FULL_CASE_ACCESS
        val grant = allCaseAccess.firstOrNull { it.caseId == caseEntity.id && it.userId == user.id }
        return if (grant != null && grant.isEnabled) grant.accessLevel else null
    }
}

class MainViewModel(
    private val repository: VakilRepository,
    private val prefs: PreferencesRepository
) : ViewModel() {

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    val loggedInUserId: StateFlow<Long?> = prefs.loggedInUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isDarkTheme: StateFlow<Boolean> = prefs.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val baseFlow = combine(
        loggedInUserId,
        repository.allUsers,
        repository.allCases,
        repository.allCaseAccess,
        isDarkTheme
    ) { uid, users, cases, accessList, dark ->
        val currentUser = users.firstOrNull { it.id == uid && it.isActive }
        val visibleCases = if (currentUser == null) {
            emptyList()
        } else if (currentUser.role == UserRole.LEAD_LAWYER) {
            cases
        } else {
            cases.filter { c ->
                c.ownerUserId == currentUser.id ||
                    accessList.any { it.caseId == c.id && it.userId == currentUser.id && it.isEnabled }
            }
        }
        TempBaseState(currentUser, users, visibleCases, accessList, dark)
    }

    private val secondaryFlow = combine(
        repository.allDocuments,
        repository.allStages,
        repository.allBriefs,
        repository.allLaws,
        repository.allRulings
    ) { docs, stages, briefs, laws, rulings ->
        TempSecondaryState(docs, stages, briefs, laws, rulings)
    }

    val workspaceState: StateFlow<WorkspaceUiState> = combine(
        baseFlow,
        secondaryFlow,
        repository.allCaseLegalRefs
    ) { base, sec, refs ->
        val visibleCaseIds = base.visibleCases.map { it.id }.toSet()
        WorkspaceUiState(
            currentUser = base.currentUser,
            allUsers = base.users,
            visibleCases = base.visibleCases,
            allCaseAccess = base.accessList,
            visibleDocuments = sec.docs.filter { it.caseId in visibleCaseIds },
            visibleStages = sec.stages.filter { it.caseId in visibleCaseIds },
            allBriefs = sec.briefs,
            allLaws = sec.laws,
            allRulings = sec.rulings,
            allCaseLegalRefs = refs.filter { it.caseId in visibleCaseIds },
            isDarkTheme = base.dark
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkspaceUiState())

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun consumeToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch { prefs.setDarkTheme(enabled) }
    }

    fun logout() {
        viewModelScope.launch { repository.logout() }
    }

    // --- Case Operations ---

    fun decryptNotes(caseEntity: CaseEntity): String = repository.decryptLawyerNotes(caseEntity)

    fun saveCase(context: Context, caseEntity: CaseEntity, plainNotes: String, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val savedId = repository.saveCase(caseEntity, plainNotes)
            if (caseEntity.reminderEnabled && caseEntity.reminderMillis != null) {
                ReminderScheduler.scheduleReminder(
                    context = context,
                    requestCode = savedId.toInt(),
                    triggerAtMillis = caseEntity.reminderMillis,
                    title = "یادآور پرونده ${caseEntity.officeCaseNumber}",
                    message = "${caseEntity.subject} - جلسه/مهلت در تاریخ ${PersianDateUtils.formatShamsiDateTime(caseEntity.nextHearingMillis)}"
                )
            } else {
                ReminderScheduler.cancelReminder(context, savedId.toInt())
            }
            _toastMessage.value = "پرونده با موفقیت ذخیره شد."
            onSaved(savedId)
        }
    }

    fun deleteCase(context: Context, caseId: Long) {
        viewModelScope.launch {
            ReminderScheduler.cancelReminder(context, caseId.toInt())
            repository.deleteCase(caseId)
            _toastMessage.value = "پرونده حذف گردید."
        }
    }

    // --- Documents ---

    fun addDocumentFromUri(
        context: Context,
        caseId: Long,
        uri: Uri,
        title: String,
        description: String,
        category: DocumentCategory,
        extractedText: String
    ) {
        val uploader = workspaceState.value.currentUser?.fullName ?: "وکیل"
        viewModelScope.launch {
            val copied = withContext(Dispatchers.IO) {
                FileStorageHelper.copyUriToInternalStorage(context, uri, caseId)
            }
            if (copied == null) {
                _toastMessage.value = "امکان خواندن فایل انتخاب‌شده وجود نداشت."
                return@launch
            }
            repository.insertDocument(
                DocumentEntity(
                    caseId = caseId,
                    title = title.ifBlank { copied.fileName },
                    description = description,
                    category = category,
                    fileType = copied.fileType,
                    localPath = copied.localPath,
                    mimeType = copied.mimeType,
                    sizeBytes = copied.sizeBytes,
                    extractedText = extractedText,
                    uploadedByName = uploader
                )
            )
            _toastMessage.value = "مدرک با موفقیت در پرونده بایگانی شد."
        }
    }

    fun addDocumentFromCameraFile(
        context: Context,
        caseId: Long,
        photoFile: File,
        title: String,
        description: String,
        category: DocumentCategory,
        extractedText: String
    ) {
        val uploader = workspaceState.value.currentUser?.fullName ?: "وکیل"
        viewModelScope.launch {
            val copied = withContext(Dispatchers.IO) {
                FileStorageHelper.copyFileToInternalStorage(context, photoFile, caseId)
            }
            if (copied == null) {
                _toastMessage.value = "خطا در ذخیره تصویر دوربین."
                return@launch
            }
            repository.insertDocument(
                DocumentEntity(
                    caseId = caseId,
                    title = title.ifBlank { copied.fileName },
                    description = description,
                    category = category,
                    fileType = copied.fileType,
                    localPath = copied.localPath,
                    mimeType = copied.mimeType,
                    sizeBytes = copied.sizeBytes,
                    extractedText = extractedText,
                    uploadedByName = uploader
                )
            )
            _toastMessage.value = "تصویر دوربین در پرونده ثبت شد."
        }
    }

    fun updateDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.updateDocument(doc)
            _toastMessage.value = "اطلاعات مدرک به‌روزرسانی شد."
        }
    }

    fun deleteDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.deleteDocument(doc)
            _toastMessage.value = "مدرک حذف شد."
        }
    }

    // --- Stages ---

    fun saveStage(stage: CaseStageEntity) {
        viewModelScope.launch {
            repository.saveStage(stage)
            _toastMessage.value = "مرحله پرونده ثبت شد."
        }
    }

    fun updateStageStatus(stage: CaseStageEntity, status: StageStatus) {
        viewModelScope.launch {
            repository.saveStage(stage.copy(status = status))
            _toastMessage.value = "وضعیت مرحله تغییر یافت."
        }
    }

    fun deleteStage(stageId: Long) {
        viewModelScope.launch {
            repository.deleteStage(stageId)
            _toastMessage.value = "مرحله حذف شد."
        }
    }

    // --- Briefs ---

    fun saveBrief(brief: BriefEntity, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.saveBrief(brief)
            _toastMessage.value = "سند حقوقی ذخیره شد."
            onSaved(id)
        }
    }

    fun deleteBrief(briefId: Long) {
        viewModelScope.launch {
            repository.deleteBrief(briefId)
            _toastMessage.value = "سند حذف شد."
        }
    }

    fun createBriefFromTemplate(template: BriefEntity, linkedCase: CaseEntity?): BriefEntity {
        val author = workspaceState.value.currentUser?.fullName ?: "وکیل دادگستری"
        var body = template.content
        if (linkedCase != null) {
            body = body
                .replace("[نام خواهان]", linkedCase.plaintiffName)
                .replace("[نام خوانده]", linkedCase.defendantName)
                .replace("[شماره پرونده]", linkedCase.courtCaseNumber.ifBlank { linkedCase.officeCaseNumber })
                .replace("[مرجع رسیدگی]", linkedCase.authorityBranch.ifBlank { linkedCase.authorityType.titleFa })
                .replace("[موضوع]", linkedCase.subject)
        }
        return BriefEntity(
            id = 0,
            title = template.title.removePrefix("الگوی ") + if (linkedCase != null) " (${linkedCase.officeCaseNumber})" else "",
            briefType = template.briefType,
            caseId = linkedCase?.id,
            caseTitleSnapshot = linkedCase?.let { "${it.officeCaseNumber} - ${it.subject}" } ?: "",
            content = body,
            isTemplate = false,
            authorName = author
        )
    }

    // --- Laws & Rulings ---

    fun saveLaw(law: LawEntity) {
        viewModelScope.launch {
            repository.saveLaw(law)
            _toastMessage.value = "قانون / ماده قانونی ذخیره شد."
        }
    }

    fun deleteLaw(lawId: Long) {
        viewModelScope.launch {
            repository.deleteLaw(lawId)
            _toastMessage.value = "قانون حذف شد."
        }
    }

    fun saveRuling(ruling: RulingEntity) {
        viewModelScope.launch {
            repository.saveRuling(ruling)
            _toastMessage.value = "رأی قضایی ذخیره شد."
        }
    }

    fun deleteRuling(rulingId: Long) {
        viewModelScope.launch {
            repository.deleteRuling(rulingId)
            _toastMessage.value = "رأی حذف شد."
        }
    }

    fun attachLegalRefToCase(
        caseId: Long,
        refType: LegalRefType,
        refId: Long,
        title: String,
        subtitle: String
    ) {
        viewModelScope.launch {
            repository.attachLegalRefToCase(caseId, refType, refId, title, subtitle)
            _toastMessage.value = "منبع حقوقی به پرونده پیوست شد."
        }
    }

    fun removeCaseLegalRef(id: Long) {
        viewModelScope.launch {
            repository.removeCaseLegalRef(id)
            _toastMessage.value = "پیوست حذف شد."
        }
    }

    // --- Team & RBAC Management ---

    fun addAssociateUser(
        fullName: String,
        identifier: String,
        password: String,
        barLicense: String
    ) {
        viewModelScope.launch {
            val res = repository.registerUser(
                fullName = fullName,
                identifier = identifier,
                password = password,
                role = UserRole.ASSOCIATE,
                barLicense = barLicense,
                securityQuestion = "نام شهر محل تولد شما چیست؟",
                securityAnswer = "تهران"
            )
            res.onSuccess {
                _toastMessage.value = "وکیل همکار / کارمند جدید اضافه شد."
            }.onFailure {
                _toastMessage.value = it.message ?: "خطا در افزودن کاربر"
            }
        }
    }

    fun toggleUserActive(userId: Long, active: Boolean) {
        viewModelScope.launch {
            repository.setUserActive(userId, active)
            _toastMessage.value = if (active) "دسترسی کاربر فعال شد." else "حساب کاربر غیرفعال شد."
        }
    }

    fun assignCaseAccess(caseId: Long, userId: Long, level: AccessLevel, enabled: Boolean) {
        viewModelScope.launch {
            repository.assignCaseAccess(caseId, userId, level, enabled)
            _toastMessage.value = "سطح دسترسی پرونده به‌روزرسانی شد."
        }
    }

    fun revokeCaseAccess(caseId: Long, userId: Long) {
        viewModelScope.launch {
            repository.revokeCaseAccess(caseId, userId)
            _toastMessage.value = "دسترسی کاربر به پرونده لغو شد."
        }
    }

    // --- Security Settings ---

    fun changePassword(currentPwd: String, newPwd: String) {
        val userId = workspaceState.value.currentUser?.id ?: return
        viewModelScope.launch {
            val res = repository.changePassword(userId, currentPwd, newPwd)
            res.onSuccess {
                _toastMessage.value = "رمز عبور با موفقیت تغییر کرد."
            }.onFailure {
                _toastMessage.value = it.message ?: "خطا در تغییر رمز عبور"
            }
        }
    }

    fun updateSecuritySettings(twoFactorEnabled: Boolean, question: String, newAnswer: String?) {
        val userId = workspaceState.value.currentUser?.id ?: return
        viewModelScope.launch {
            repository.updateUserSecuritySettings(userId, twoFactorEnabled, question, newAnswer)
            _toastMessage.value = "تنظیمات امنیتی ذخیره شد."
        }
    }

    // --- Offline PDF Export Helpers ---

    fun exportCaseSummaryPdf(context: Context, caseEntity: CaseEntity, shareImmediately: Boolean) {
        val state = workspaceState.value
        val stages = state.visibleStages.filter { it.caseId == caseEntity.id }
        val docs = state.visibleDocuments.filter { it.caseId == caseEntity.id }
        val refs = state.allCaseLegalRefs.filter { it.caseId == caseEntity.id }
        val notes = decryptNotes(caseEntity)
        val summaryText = CaseReportBuilder.buildComprehensiveCaseSummary(caseEntity, notes, stages, docs, refs)

        viewModelScope.launch {
            val pdfFile = withContext(Dispatchers.IO) {
                PdfExportHelper.generatePersianPdf(
                    context = context,
                    fileNamePrefix = "case_${caseEntity.id}",
                    documentTitle = "گزارش جامع پرونده ${caseEntity.officeCaseNumber}",
                    subtitle = "${caseEntity.caseType.titleFa} | ${caseEntity.status.titleFa}",
                    sections = listOf("مشخصات، مراحل و مدارک پرونده" to summaryText)
                )
            }
            if (pdfFile == null) {
                _toastMessage.value = "خطا در تولید فایل PDF."
            } else if (shareImmediately) {
                FileStorageHelper.shareFile(context, pdfFile.absolutePath, "application/pdf", caseEntity.subject)
            } else {
                FileStorageHelper.openExternalFile(context, pdfFile.absolutePath, "application/pdf")
            }
        }
    }

    fun exportBriefPdf(context: Context, brief: BriefEntity, shareImmediately: Boolean) {
        viewModelScope.launch {
            val pdfFile = withContext(Dispatchers.IO) {
                PdfExportHelper.generatePersianPdf(
                    context = context,
                    fileNamePrefix = "brief_${brief.id}",
                    documentTitle = brief.title,
                    subtitle = "${brief.briefType.titleFa} ${if (brief.caseTitleSnapshot.isNotBlank()) "| ${brief.caseTitleSnapshot}" else ""}",
                    sections = listOf("" to brief.content)
                )
            }
            if (pdfFile == null) {
                _toastMessage.value = "خطا در تولید PDF لایحه."
            } else if (shareImmediately) {
                FileStorageHelper.shareFile(context, pdfFile.absolutePath, "application/pdf", brief.title)
            } else {
                FileStorageHelper.openExternalFile(context, pdfFile.absolutePath, "application/pdf")
            }
        }
    }

    fun exportReportPdf(
        context: Context,
        title: String,
        subtitle: String,
        sections: List<Pair<String, String>>,
        mode: PdfActionMode
    ) {
        viewModelScope.launch {
            val pdfFile = withContext(Dispatchers.IO) {
                PdfExportHelper.generatePersianPdf(
                    context = context,
                    fileNamePrefix = "report",
                    documentTitle = title,
                    subtitle = subtitle,
                    sections = sections
                )
            }
            if (pdfFile == null) {
                _toastMessage.value = "خطا در تولید فایل PDF گزارش."
                return@launch
            }
            when (mode) {
                PdfActionMode.VIEW -> FileStorageHelper.openExternalFile(context, pdfFile.absolutePath, "application/pdf")
                PdfActionMode.SHARE -> FileStorageHelper.shareFile(context, pdfFile.absolutePath, "application/pdf", title)
                PdfActionMode.PRINT -> PdfExportHelper.printPdfFile(context, pdfFile, title)
            }
        }
    }

    private data class TempBaseState(
        val currentUser: UserEntity?,
        val users: List<UserEntity>,
        val visibleCases: List<CaseEntity>,
        val accessList: List<CaseAccessEntity>,
        val dark: Boolean
    )

    private data class TempSecondaryState(
        val docs: List<DocumentEntity>,
        val stages: List<CaseStageEntity>,
        val briefs: List<BriefEntity>,
        val laws: List<LawEntity>,
        val rulings: List<RulingEntity>
    )
}

enum class PdfActionMode {
    VIEW, SHARE, PRINT
}
