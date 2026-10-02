package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.PaymentReceiptEntity
import com.example.data.model.PolicyEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserEntity::class, PolicyEntity::class, PaymentReceiptEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun policyDao(): PolicyDao
    abstract fun paymentReceiptDao(): PaymentReceiptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lic_mitr_database.db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun ensureSeeded() {
        if (policyDao().getPolicyCount() == 0) {
            seedInitialData(this)
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.ensureSeeded()
            }
        }
    }
}

suspend fun seedInitialData(database: AppDatabase) {
    val userDao = database.userDao()
    val policyDao = database.policyDao()
    val receiptDao = database.paymentReceiptDao()

    // 1. Sunil (from user brief)
    val sunilId = userDao.insertUser(
        UserEntity(
            fullName = "Sunil",
            dob = "16/12/2003",
            mobileNumber = "+91 98765 43210",
            email = "sbsunil937@gmail.com",
            address = "Plot 42, Civil Lines, Kanpur, Uttar Pradesh 208001",
            profilePhotoUri = "asset://img_user_avatar"
        )
    )

    // Sunil's 6-Month LIC Policy (from example)
    val policy1Id = policyDao.insertPolicy(
        PolicyEntity(
            userId = sunilId,
            policyNumber = "8492012345",
            policyName = "LIC Jeevan Labh (Plan 936)",
            policyType = "Endowment / Savings",
            sumAssured = 500000.0,
            premiumAmount = 5000.0,
            frequencyMonths = 6,
            startDate = "10/10/2026",
            nextPremiumDate = "10/04/2027",
            lastPaymentDate = "10/10/2026",
            nomineeName = "Anita Devi",
            nomineeRelation = "Mother",
            agentName = "Rakesh Sharma",
            agentCode = "AGNT-038291",
            notes = "Half-yearly premium auto-calculated. Grace period 30 days."
        )
    )

    // Initial receipt for Policy 1
    receiptDao.insertReceipt(
        PaymentReceiptEntity(
            policyId = policy1Id,
            paymentDate = "10/10/2026",
            amountPaid = 5000.0,
            transactionRef = "UPI/261010/849201",
            paymentMode = "UPI / PhonePe",
            periodCovered = "Oct 2026 - Apr 2027"
        )
    )

    // Sunil's 2nd Policy (Due Soon example)
    val policy2Id = policyDao.insertPolicy(
        PolicyEntity(
            userId = sunilId,
            policyNumber = "9123847561",
            policyName = "LIC Tech Term (Plan 854)",
            policyType = "Pure Term Life",
            sumAssured = 2500000.0,
            premiumAmount = 4200.0,
            frequencyMonths = 1,
            startDate = "15/09/2026",
            nextPremiumDate = "15/10/2026",
            lastPaymentDate = "15/09/2026",
            nomineeName = "Anita Devi",
            nomineeRelation = "Mother",
            agentName = "Rakesh Sharma",
            agentCode = "AGNT-038291",
            notes = "Monthly pure protection cover."
        )
    )

    receiptDao.insertReceipt(
        PaymentReceiptEntity(
            policyId = policy2Id,
            paymentDate = "15/09/2026",
            amountPaid = 4200.0,
            transactionRef = "NETBANK/HDFC/99214",
            paymentMode = "Net Banking (HDFC)",
            periodCovered = "Sep 2026 - Oct 2026"
        )
    )

    // 2. Additional User for Agent / Admin Panel: Priya Verma (Overdue example)
    val priyaId = userDao.insertUser(
        UserEntity(
            fullName = "Priya Verma",
            dob = "05/08/1995",
            mobileNumber = "+91 91234 56789",
            email = "priya.verma@example.com",
            address = "Flat 302, Green Glen Layout, Bellandur, Bengaluru 560103"
        )
    )

    policyDao.insertPolicy(
        PolicyEntity(
            userId = priyaId,
            policyNumber = "7320491823",
            policyName = "LIC SIIP (Plan 852)",
            policyType = "ULIP / Investment",
            sumAssured = 1000000.0,
            premiumAmount = 12000.0,
            frequencyMonths = 3,
            startDate = "01/01/2026",
            nextPremiumDate = "01/09/2026", // overdue
            lastPaymentDate = "01/06/2026",
            nomineeName = "Rahul Verma",
            nomineeRelation = "Spouse",
            agentName = "Vikas Malhotra",
            agentCode = "AGNT-084729",
            notes = "Quarterly unit-linked insurance plan."
        )
    )
}
