package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val identifier: String, // Mobile number or Email
    val passwordHash: String,
    val passwordSalt: String,
    val role: UserRole,
    val isActive: Boolean = true,
    val twoFactorEnabled: Boolean = false,
    val securityQuestion: String = "نام شهر محل تولد شما چیست؟",
    val securityAnswerHash: String = "",
    val securityAnswerSalt: String = "",
    val barLicenseNumber: String = "",
    val lastLoginAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cases")
data class CaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val officeCaseNumber: String,
    val courtCaseNumber: String,
    val caseType: CaseType,
    val authorityType: AuthorityType,
    val authorityBranch: String,
    val plaintiffName: String,
    val defendantName: String,
    val subject: String,
    val filingDateMillis: Long,
    val nextHearingMillis: Long?,
    val opposingCounsel: String,
    val status: CaseStatus,
    val summary: String,
    val encryptedLawyerNotes: String, // Stored encrypted via SecurityUtils
    val importantDatesNote: String = "",
    val reminderEnabled: Boolean = false,
    val reminderMillis: Long? = null,
    val hasPendingAction: Boolean = false,
    val ownerUserId: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "case_access",
    primaryKeys = ["caseId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = CaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["caseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("caseId")]
)
data class CaseAccessEntity(
    val caseId: Long,
    val userId: Long,
    val accessLevel: AccessLevel,
    val isEnabled: Boolean = true,
    val assignedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(
            entity = CaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["caseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("caseId")]
)
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseId: Long,
    val title: String,
    val description: String,
    val category: DocumentCategory,
    val fileType: DocumentFileType,
    val localPath: String,
    val mimeType: String,
    val sizeBytes: Long,
    val extractedText: String = "", // Offline transcript / searchable notes
    val uploadedByName: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "case_stages",
    foreignKeys = [
        ForeignKey(
            entity = CaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["caseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("caseId")]
)
data class CaseStageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseId: Long,
    val stageType: StageType,
    val title: String,
    val dateMillis: Long,
    val description: String,
    val relatedDocumentId: Long? = null,
    val responsibleName: String,
    val status: StageStatus,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "briefs")
data class BriefEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val briefType: BriefType,
    val caseId: Long? = null,
    val caseTitleSnapshot: String = "",
    val content: String,
    val isTemplate: Boolean = false,
    val authorName: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "laws")
data class LawEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lawName: String,
    val category: LawCategory,
    val articleNumber: String,
    val tabsera: String = "",
    val fullText: String,
    val personalNote: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "rulings")
data class RulingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val rulingNumber: String,
    val authority: RulingAuthority,
    val persianYear: String,
    val topicTag: String,
    val keywords: String,
    val fullText: String,
    val personalNote: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "case_legal_refs",
    foreignKeys = [
        ForeignKey(
            entity = CaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["caseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("caseId")]
)
data class CaseLegalRefEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseId: Long,
    val refType: LegalRefType,
    val refId: Long,
    val refTitle: String,
    val refSubtitle: String,
    val addedAt: Long = System.currentTimeMillis()
)
