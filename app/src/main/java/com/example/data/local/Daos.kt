package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BriefEntity
import com.example.data.model.CaseAccessEntity
import com.example.data.model.CaseEntity
import com.example.data.model.CaseLegalRefEntity
import com.example.data.model.CaseStageEntity
import com.example.data.model.DocumentEntity
import com.example.data.model.LawEntity
import com.example.data.model.RulingEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY role ASC, fullName ASC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(identifier) = LOWER(:identifier) LIMIT 1")
    suspend fun getUserByIdentifier(identifier: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Delete
    suspend fun delete(user: UserEntity)
}

@Dao
interface CaseDao {
    @Query("SELECT * FROM cases ORDER BY updatedAt DESC")
    fun observeAllCases(): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE id = :caseId LIMIT 1")
    fun observeCaseById(caseId: Long): Flow<CaseEntity?>

    @Query("SELECT * FROM cases WHERE id = :caseId LIMIT 1")
    suspend fun getCaseById(caseId: Long): CaseEntity?

    @Query("SELECT COUNT(*) FROM cases")
    suspend fun countCases(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(caseEntity: CaseEntity): Long

    @Update
    suspend fun update(caseEntity: CaseEntity)

    @Query("DELETE FROM cases WHERE id = :caseId")
    suspend fun deleteById(caseId: Long)

    @Query("SELECT * FROM case_access")
    fun observeAllCaseAccess(): Flow<List<CaseAccessEntity>>

    @Query("SELECT * FROM case_access WHERE caseId = :caseId")
    fun observeAccessForCase(caseId: Long): Flow<List<CaseAccessEntity>>

    @Query("SELECT * FROM case_access WHERE userId = :userId")
    fun observeAccessForUser(userId: Long): Flow<List<CaseAccessEntity>>

    @Query("SELECT * FROM case_access WHERE caseId = :caseId AND userId = :userId LIMIT 1")
    suspend fun getAccess(caseId: Long, userId: Long): CaseAccessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccess(access: CaseAccessEntity)

    @Query("DELETE FROM case_access WHERE caseId = :caseId AND userId = :userId")
    suspend fun deleteAccess(caseId: Long, userId: Long)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun observeAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE caseId = :caseId ORDER BY createdAt DESC")
    fun observeDocumentsForCase(caseId: Long): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :docId LIMIT 1")
    suspend fun getById(docId: Long): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: DocumentEntity): Long

    @Update
    suspend fun update(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :docId")
    suspend fun deleteById(docId: Long)
}

@Dao
interface StageDao {
    @Query("SELECT * FROM case_stages ORDER BY dateMillis DESC")
    fun observeAllStages(): Flow<List<CaseStageEntity>>

    @Query("SELECT * FROM case_stages WHERE caseId = :caseId ORDER BY dateMillis DESC")
    fun observeStagesForCase(caseId: Long): Flow<List<CaseStageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stage: CaseStageEntity): Long

    @Update
    suspend fun update(stage: CaseStageEntity)

    @Query("DELETE FROM case_stages WHERE id = :stageId")
    suspend fun deleteById(stageId: Long)
}

@Dao
interface BriefDao {
    @Query("SELECT * FROM briefs ORDER BY isTemplate DESC, updatedAt DESC")
    fun observeAllBriefs(): Flow<List<BriefEntity>>

    @Query("SELECT COUNT(*) FROM briefs WHERE isTemplate = 1")
    suspend fun countTemplates(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(brief: BriefEntity): Long

    @Update
    suspend fun update(brief: BriefEntity)

    @Query("DELETE FROM briefs WHERE id = :briefId")
    suspend fun deleteById(briefId: Long)
}

@Dao
interface LegalResourceDao {
    @Query("SELECT * FROM laws ORDER BY category ASC, id ASC")
    fun observeAllLaws(): Flow<List<LawEntity>>

    @Query("SELECT COUNT(*) FROM laws")
    suspend fun countLaws(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaw(law: LawEntity): Long

    @Update
    suspend fun updateLaw(law: LawEntity)

    @Query("DELETE FROM laws WHERE id = :lawId")
    suspend fun deleteLaw(lawId: Long)

    @Query("SELECT * FROM rulings ORDER BY persianYear DESC, id ASC")
    fun observeAllRulings(): Flow<List<RulingEntity>>

    @Query("SELECT COUNT(*) FROM rulings")
    suspend fun countRulings(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuling(ruling: RulingEntity): Long

    @Update
    suspend fun updateRuling(ruling: RulingEntity)

    @Query("DELETE FROM rulings WHERE id = :rulingId")
    suspend fun deleteRuling(rulingId: Long)

    @Query("SELECT * FROM case_legal_refs ORDER BY addedAt DESC")
    fun observeAllCaseLegalRefs(): Flow<List<CaseLegalRefEntity>>

    @Query("SELECT * FROM case_legal_refs WHERE caseId = :caseId ORDER BY addedAt DESC")
    fun observeRefsForCase(caseId: Long): Flow<List<CaseLegalRefEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaseRef(ref: CaseLegalRefEntity): Long

    @Query("DELETE FROM case_legal_refs WHERE id = :refRowId")
    suspend fun deleteCaseRef(refRowId: Long)
}
