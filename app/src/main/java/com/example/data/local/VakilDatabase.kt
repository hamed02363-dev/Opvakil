package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.AccessLevel
import com.example.data.model.AuthorityType
import com.example.data.model.BriefEntity
import com.example.data.model.BriefType
import com.example.data.model.CaseAccessEntity
import com.example.data.model.CaseEntity
import com.example.data.model.CaseLegalRefEntity
import com.example.data.model.CaseStageEntity
import com.example.data.model.CaseStatus
import com.example.data.model.CaseType
import com.example.data.model.DocumentCategory
import com.example.data.model.DocumentEntity
import com.example.data.model.DocumentFileType
import com.example.data.model.LawCategory
import com.example.data.model.LawEntity
import com.example.data.model.LegalRefType
import com.example.data.model.RulingAuthority
import com.example.data.model.RulingEntity
import com.example.data.model.StageStatus
import com.example.data.model.StageType
import com.example.data.model.UserEntity
import com.example.data.model.UserRole

class VakilTypeConverters {
    @TypeConverter fun fromUserRole(v: UserRole): String = v.name
    @TypeConverter fun toUserRole(v: String): UserRole = runCatching { UserRole.valueOf(v) }.getOrDefault(UserRole.ASSOCIATE)

    @TypeConverter fun fromAccessLevel(v: AccessLevel): String = v.name
    @TypeConverter fun toAccessLevel(v: String): AccessLevel = runCatching { AccessLevel.valueOf(v) }.getOrDefault(AccessLevel.VIEW_ONLY)

    @TypeConverter fun fromCaseType(v: CaseType): String = v.name
    @TypeConverter fun toCaseType(v: String): CaseType = runCatching { CaseType.valueOf(v) }.getOrDefault(CaseType.CIVIL)

    @TypeConverter fun fromAuthorityType(v: AuthorityType): String = v.name
    @TypeConverter fun toAuthorityType(v: String): AuthorityType = runCatching { AuthorityType.valueOf(v) }.getOrDefault(AuthorityType.COURT)

    @TypeConverter fun fromCaseStatus(v: CaseStatus): String = v.name
    @TypeConverter fun toCaseStatus(v: String): CaseStatus = runCatching { CaseStatus.valueOf(v) }.getOrDefault(CaseStatus.UNDER_REVIEW)

    @TypeConverter fun fromDocCat(v: DocumentCategory): String = v.name
    @TypeConverter fun toDocCat(v: String): DocumentCategory = runCatching { DocumentCategory.valueOf(v) }.getOrDefault(DocumentCategory.OTHER)

    @TypeConverter fun fromDocType(v: DocumentFileType): String = v.name
    @TypeConverter fun toDocType(v: String): DocumentFileType = runCatching { DocumentFileType.valueOf(v) }.getOrDefault(DocumentFileType.OTHER)

    @TypeConverter fun fromStageType(v: StageType): String = v.name
    @TypeConverter fun toStageType(v: String): StageType = runCatching { StageType.valueOf(v) }.getOrDefault(StageType.FILING)

    @TypeConverter fun fromStageStatus(v: StageStatus): String = v.name
    @TypeConverter fun toStageStatus(v: String): StageStatus = runCatching { StageStatus.valueOf(v) }.getOrDefault(StageStatus.PENDING_ACTION)

    @TypeConverter fun fromBriefType(v: BriefType): String = v.name
    @TypeConverter fun toBriefType(v: String): BriefType = runCatching { BriefType.valueOf(v) }.getOrDefault(BriefType.DEFENSE_BRIEF)

    @TypeConverter fun fromLawCat(v: LawCategory): String = v.name
    @TypeConverter fun toLawCat(v: String): LawCategory = runCatching { LawCategory.valueOf(v) }.getOrDefault(LawCategory.CIVIL)

    @TypeConverter fun fromRulingAuth(v: RulingAuthority): String = v.name
    @TypeConverter fun toRulingAuth(v: String): RulingAuthority = runCatching { RulingAuthority.valueOf(v) }.getOrDefault(RulingAuthority.SUPREME_COURT_UNITY)

    @TypeConverter fun fromRefType(v: LegalRefType): String = v.name
    @TypeConverter fun toRefType(v: String): LegalRefType = runCatching { LegalRefType.valueOf(v) }.getOrDefault(LegalRefType.LAW)
}

@Database(
    entities = [
        UserEntity::class,
        CaseEntity::class,
        CaseAccessEntity::class,
        DocumentEntity::class,
        CaseStageEntity::class,
        BriefEntity::class,
        LawEntity::class,
        RulingEntity::class,
        CaseLegalRefEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(VakilTypeConverters::class)
abstract class VakilDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun caseDao(): CaseDao
    abstract fun documentDao(): DocumentDao
    abstract fun stageDao(): StageDao
    abstract fun briefDao(): BriefDao
    abstract fun legalResourceDao(): LegalResourceDao

    companion object {
        @Volatile
        private var INSTANCE: VakilDatabase? = null

        fun getInstance(context: Context): VakilDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    VakilDatabase::class.java,
                    "vakilyar_offline.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
