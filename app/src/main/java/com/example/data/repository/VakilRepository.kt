package com.example.data.repository

import com.example.data.local.SeedData
import com.example.data.local.VakilDatabase
import com.example.data.model.AccessLevel
import com.example.data.model.BriefEntity
import com.example.data.model.CaseAccessEntity
import com.example.data.model.CaseEntity
import com.example.data.model.CaseLegalRefEntity
import com.example.data.model.CaseStageEntity
import com.example.data.model.DocumentEntity
import com.example.data.model.LawEntity
import com.example.data.model.LegalRefType
import com.example.data.model.RulingEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File

class VakilRepository(
    private val db: VakilDatabase,
    private val prefs: PreferencesRepository
) {
    private val userDao = db.userDao()
    private val caseDao = db.caseDao()
    private val documentDao = db.documentDao()
    private val stageDao = db.stageDao()
    private val briefDao = db.briefDao()
    private val legalDao = db.legalResourceDao()

    val allUsers: Flow<List<UserEntity>> = userDao.observeAllUsers()
    val allCases: Flow<List<CaseEntity>> = caseDao.observeAllCases()
    val allCaseAccess: Flow<List<CaseAccessEntity>> = caseDao.observeAllCaseAccess()
    val allDocuments: Flow<List<DocumentEntity>> = documentDao.observeAllDocuments()
    val allStages: Flow<List<CaseStageEntity>> = stageDao.observeAllStages()
    val allBriefs: Flow<List<BriefEntity>> = briefDao.observeAllBriefs()
    val allLaws: Flow<List<LawEntity>> = legalDao.observeAllLaws()
    val allRulings: Flow<List<RulingEntity>> = legalDao.observeAllRulings()
    val allCaseLegalRefs: Flow<List<CaseLegalRefEntity>> = legalDao.observeAllCaseLegalRefs()

    fun observeUserById(userId: Long): Flow<UserEntity?> = userDao.observeUserById(userId)

    suspend fun ensureSeeded() {
        val alreadySeeded = prefs.isSeeded.first()
        if (alreadySeeded && userDao.countUsers() > 0) return

        var leadId = 0L
        var assocId = 0L
        if (userDao.countUsers() == 0) {
            val users = SeedData.initialUsers()
            leadId = userDao.insert(users[0])
            assocId = userDao.insert(users[1])
        }
        if (caseDao.countCases() == 0 && leadId > 0L) {
            val cases = SeedData.initialCases(leadId)
            val c1Id = caseDao.insert(cases[0])
            val c2Id = caseDao.insert(cases[1])
            if (assocId > 0L) {
                caseDao.upsertAccess(SeedData.initialAccess(c1Id, assocId))
            }
            SeedData.initialStages(c1Id, c2Id, "استاد دکتر امیرحسین رادمنش").forEach {
                stageDao.insert(it)
            }
        }
        if (briefDao.countTemplates() == 0) {
            SeedData.legalTemplates().forEach { briefDao.insert(it) }
        }
        if (legalDao.countLaws() == 0) {
            SeedData.initialLaws().forEach { legalDao.insertLaw(it) }
        }
        if (legalDao.countRulings() == 0) {
            SeedData.initialRulings().forEach { legalDao.insertRuling(it) }
        }
        prefs.markSeeded()
    }

    // --- Authentication & Users ---

    suspend fun authenticate(identifier: String, password: String): Result<UserEntity> {
        val cleanId = identifier.trim()
        val user = userDao.getUserByIdentifier(cleanId)
            ?: return Result.failure(IllegalArgumentException("کاربری با این شماره موبایل یا ایمیل یافت نشد."))
        if (!user.isActive) {
            return Result.failure(IllegalStateException("حساب کاربری شما توسط وکیل اصلی غیرفعال شده است."))
        }
        if (!SecurityUtils.verifySecret(password, user.passwordSalt, user.passwordHash)) {
            return Result.failure(IllegalArgumentException("رمز عبور اشتباه است."))
        }
        return Result.success(user)
    }

    suspend fun completeLogin(user: UserEntity) {
        val updated = user.copy(lastLoginAt = System.currentTimeMillis())
        userDao.update(updated)
        prefs.setLoggedInUserId(updated.id)
    }

    suspend fun logout() {
        prefs.setLoggedInUserId(null)
    }

    suspend fun registerUser(
        fullName: String,
        identifier: String,
        password: String,
        role: UserRole,
        barLicense: String,
        securityQuestion: String,
        securityAnswer: String
    ): Result<UserEntity> {
        val cleanId = identifier.trim()
        if (userDao.getUserByIdentifier(cleanId) != null) {
            return Result.failure(IllegalStateException("این شماره موبایل یا ایمیل قبلاً ثبت شده است."))
        }
        val pwdSalt = SecurityUtils.generateSalt()
        val ansSalt = SecurityUtils.generateSalt()
        val entity = UserEntity(
            fullName = fullName.trim(),
            identifier = cleanId,
            passwordHash = SecurityUtils.hashSecret(password, pwdSalt),
            passwordSalt = pwdSalt,
            role = role,
            isActive = true,
            securityQuestion = securityQuestion.trim(),
            securityAnswerHash = SecurityUtils.hashSecret(securityAnswer.trim(), ansSalt),
            securityAnswerSalt = ansSalt,
            barLicenseNumber = barLicense.trim(),
            lastLoginAt = System.currentTimeMillis()
        )
        val id = userDao.insert(entity)
        return Result.success(entity.copy(id = id))
    }

    suspend fun recoverPassword(
        identifier: String,
        securityAnswer: String,
        newPassword: String
    ): Result<Unit> {
        val user = userDao.getUserByIdentifier(identifier.trim())
            ?: return Result.failure(IllegalArgumentException("کاربری با این مشخصات یافت نشد."))
        if (user.securityAnswerHash.isBlank() ||
            !SecurityUtils.verifySecret(securityAnswer.trim(), user.securityAnswerSalt, user.securityAnswerHash)
        ) {
            return Result.failure(IllegalArgumentException("پاسخ سوال امنیتی صحیح نیست."))
        }
        val newSalt = SecurityUtils.generateSalt()
        userDao.update(
            user.copy(
                passwordHash = SecurityUtils.hashSecret(newPassword, newSalt),
                passwordSalt = newSalt
            )
        )
        return Result.success(Unit)
    }

    suspend fun changePassword(userId: Long, currentPassword: String, newPassword: String): Result<Unit> {
        val user = userDao.getUserById(userId)
            ?: return Result.failure(IllegalStateException("کاربر یافت نشد."))
        if (!SecurityUtils.verifySecret(currentPassword, user.passwordSalt, user.passwordHash)) {
            return Result.failure(IllegalArgumentException("رمز عبور فعلی اشتباه است."))
        }
        val newSalt = SecurityUtils.generateSalt()
        userDao.update(
            user.copy(
                passwordHash = SecurityUtils.hashSecret(newPassword, newSalt),
                passwordSalt = newSalt
            )
        )
        return Result.success(Unit)
    }

    suspend fun updateUserSecuritySettings(
        userId: Long,
        twoFactorEnabled: Boolean,
        securityQuestion: String,
        securityAnswer: String?
    ) {
        val user = userDao.getUserById(userId) ?: return
        var ansHash = user.securityAnswerHash
        var ansSalt = user.securityAnswerSalt
        if (!securityAnswer.isNullOrBlank()) {
            ansSalt = SecurityUtils.generateSalt()
            ansHash = SecurityUtils.hashSecret(securityAnswer.trim(), ansSalt)
        }
        userDao.update(
            user.copy(
                twoFactorEnabled = twoFactorEnabled,
                securityQuestion = securityQuestion.trim(),
                securityAnswerHash = ansHash,
                securityAnswerSalt = ansSalt
            )
        )
    }

    suspend fun setUserActive(userId: Long, active: Boolean) {
        val user = userDao.getUserById(userId) ?: return
        userDao.update(user.copy(isActive = active))
    }

    // --- RBAC Access Helpers ---

    fun effectiveAccessForCase(
        user: UserEntity,
        caseEntity: CaseEntity,
        accessList: List<CaseAccessEntity>
    ): AccessLevel? {
        if (user.role == UserRole.LEAD_LAWYER) return AccessLevel.FULL_CASE_ACCESS
        if (caseEntity.ownerUserId == user.id) return AccessLevel.FULL_CASE_ACCESS
        val grant = accessList.firstOrNull { it.caseId == caseEntity.id && it.userId == user.id }
        return if (grant != null && grant.isEnabled) grant.accessLevel else null
    }

    suspend fun assignCaseAccess(caseId: Long, userId: Long, level: AccessLevel, enabled: Boolean) {
        caseDao.upsertAccess(
            CaseAccessEntity(
                caseId = caseId,
                userId = userId,
                accessLevel = level,
                isEnabled = enabled
            )
        )
    }

    suspend fun revokeCaseAccess(caseId: Long, userId: Long) {
        caseDao.deleteAccess(caseId, userId)
    }

    // --- Cases CRUD (with encrypted lawyer notes) ---

    suspend fun saveCase(caseEntity: CaseEntity, plainLawyerNotes: String): Long {
        val encrypted = SecurityUtils.encryptText(plainLawyerNotes)
        val toSave = caseEntity.copy(
            encryptedLawyerNotes = encrypted,
            updatedAt = System.currentTimeMillis()
        )
        return if (toSave.id == 0L) {
            caseDao.insert(toSave)
        } else {
            caseDao.update(toSave)
            toSave.id
        }
    }

    fun decryptLawyerNotes(caseEntity: CaseEntity): String =
        SecurityUtils.decryptText(caseEntity.encryptedLawyerNotes)

    suspend fun deleteCase(caseId: Long) {
        caseDao.deleteById(caseId)
    }

    // --- Documents ---

    suspend fun insertDocument(doc: DocumentEntity): Long = documentDao.insert(doc)

    suspend fun updateDocument(doc: DocumentEntity) = documentDao.update(doc)

    suspend fun deleteDocument(doc: DocumentEntity) {
        runCatching { File(doc.localPath).delete() }
        documentDao.deleteById(doc.id)
    }

    // --- Stages ---

    suspend fun saveStage(stage: CaseStageEntity): Long {
        return if (stage.id == 0L) stageDao.insert(stage) else {
            stageDao.update(stage)
            stage.id
        }
    }

    suspend fun deleteStage(stageId: Long) = stageDao.deleteById(stageId)

    // --- Briefs ---

    suspend fun saveBrief(brief: BriefEntity): Long {
        val updated = brief.copy(updatedAt = System.currentTimeMillis())
        return if (updated.id == 0L) briefDao.insert(updated) else {
            briefDao.update(updated)
            updated.id
        }
    }

    suspend fun deleteBrief(briefId: Long) = briefDao.deleteById(briefId)

    // --- Laws & Rulings ---

    suspend fun saveLaw(law: LawEntity): Long {
        val updated = law.copy(updatedAt = System.currentTimeMillis())
        return if (updated.id == 0L) legalDao.insertLaw(updated) else {
            legalDao.updateLaw(updated)
            updated.id
        }
    }

    suspend fun deleteLaw(lawId: Long) = legalDao.deleteLaw(lawId)

    suspend fun saveRuling(ruling: RulingEntity): Long {
        val updated = ruling.copy(updatedAt = System.currentTimeMillis())
        return if (updated.id == 0L) legalDao.insertRuling(updated) else {
            legalDao.updateRuling(updated)
            updated.id
        }
    }

    suspend fun deleteRuling(rulingId: Long) = legalDao.deleteRuling(rulingId)

    suspend fun attachLegalRefToCase(
        caseId: Long,
        refType: LegalRefType,
        refId: Long,
        title: String,
        subtitle: String
    ) {
        legalDao.insertCaseRef(
            CaseLegalRefEntity(
                caseId = caseId,
                refType = refType,
                refId = refId,
                refTitle = title,
                refSubtitle = subtitle
            )
        )
    }

    suspend fun removeCaseLegalRef(id: Long) = legalDao.deleteCaseRef(id)
}
