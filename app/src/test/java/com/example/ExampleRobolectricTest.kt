package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.VakilDatabase
import com.example.data.model.AccessLevel
import com.example.data.model.CaseAccessEntity
import com.example.data.model.CaseEntity
import com.example.data.model.CaseStatus
import com.example.data.model.CaseType
import com.example.data.model.AuthorityType
import com.example.data.model.UserRole
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.VakilRepository
import com.example.util.CaseReportBuilder
import com.example.util.PersianDateUtils
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Persian branding`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("وکیل‌یار", appName)
    }

    @Test
    fun `jalali conversion round trip is accurate`() {
        val (gy, gm, gd) = PersianDateUtils.jalaliToGregorian(1405, 9, 4)
        val jalali = PersianDateUtils.gregorianToJalali(gy, gm, gd)
        assertEquals(1405, jalali.year)
        assertEquals(9, jalali.month)
        assertEquals(4, jalali.day)
        assertEquals("۱۴۰۵/۰۹/۰۴", PersianDateUtils.toPersianDigits("1405/09/04"))
    }

    @Test
    fun `security utils salted hash and encryption work offline`() {
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashSecret("MySecret123", salt)
        assertTrue(SecurityUtils.verifySecret("MySecret123", salt, hash))
        assertFalse(SecurityUtils.verifySecret("WrongPwd", salt, hash))

        val secretNote = "یادداشت محرمانه پرونده حقوقی"
        val encrypted = SecurityUtils.encryptText(secretNote)
        assertNotEquals(secretNote, encrypted)
        assertEquals(secretNote, SecurityUtils.decryptText(encrypted))
    }

    @Test
    fun `repository seeds data and enforces RBAC permissions`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, VakilDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val prefs = PreferencesRepository(context)
        val repo = VakilRepository(db, prefs)

        repo.ensureSeeded()
        val users = repo.allUsers.first()
        val cases = repo.allCases.first()
        assertTrue(users.size >= 2)
        assertTrue(cases.size >= 2)

        val lead = users.first { it.role == UserRole.LEAD_LAWYER }
        val assoc = users.first { it.role == UserRole.ASSOCIATE }
        val case1 = cases.first()
        val case2 = cases.last()

        val accessList = listOf(
            CaseAccessEntity(caseId = case1.id, userId = assoc.id, accessLevel = AccessLevel.VIEW_ONLY, isEnabled = true)
        )
        assertEquals(AccessLevel.FULL_CASE_ACCESS, repo.effectiveAccessForCase(lead, case2, accessList))
        assertEquals(AccessLevel.VIEW_ONLY, repo.effectiveAccessForCase(assoc, case1, accessList))
        assertNull(repo.effectiveAccessForCase(assoc, case2, accessList))

        val digest = CaseReportBuilder.buildLatestStatusReport(case1, emptyList(), 0)
        assertTrue(digest.contains(case1.officeCaseNumber))

        db.close()
    }
}
