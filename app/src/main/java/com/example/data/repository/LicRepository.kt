package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.PaymentReceiptEntity
import com.example.data.model.PolicyDateHelper
import com.example.data.model.PolicyEntity
import com.example.data.model.PolicyWithDetails
import com.example.data.model.UserEntity
import com.example.data.supabase.SupabaseClient
import com.example.data.supabase.model.SupabasePaymentReceiptDto
import com.example.data.supabase.model.SupabasePolicyDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import java.util.Date

class LicRepository(private val database: AppDatabase) {
    private val userDao = database.userDao()
    private val policyDao = database.policyDao()
    private val receiptDao = database.paymentReceiptDao()
    val supabaseClient = SupabaseClient()

    suspend fun ensureSeeded() {
        database.ensureSeeded()
    }

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    fun getUser(id: Long): Flow<UserEntity?> = userDao.getUserFlow(id)

    fun getAllPoliciesWithDetails(): Flow<List<PolicyWithDetails>> {
        return combine(
            policyDao.getAllPolicies(),
            userDao.getAllUsers(),
            receiptDao.getAllReceipts()
        ) { policies, users, allReceipts ->
            val userMap = users.associateBy { it.id }
            val receiptsByPolicy = allReceipts.groupBy { it.policyId }

            val now = Date()
            policies.map { policy ->
                val user = userMap[policy.userId] ?: UserEntity(
                    id = policy.userId,
                    fullName = "Policyholder",
                    dob = "--",
                    mobileNumber = "--",
                    email = "--",
                    address = "--"
                )
                val receipts = receiptsByPolicy[policy.id].orEmpty()
                val daysRemaining = PolicyDateHelper.calculateDaysRemaining(policy.nextPremiumDate, now)
                val status = PolicyDateHelper.determineStatus(daysRemaining)

                PolicyWithDetails(
                    policy = policy,
                    user = user,
                    receipts = receipts,
                    daysRemaining = daysRemaining,
                    status = status
                )
            }
        }
    }

    fun getPolicyWithDetails(policyId: Long): Flow<PolicyWithDetails?> {
        return combine(
            policyDao.getPolicyFlow(policyId),
            userDao.getAllUsers(),
            receiptDao.getReceiptsForPolicy(policyId)
        ) { policy, users, receipts ->
            if (policy == null) return@combine null
            val user = users.find { it.id == policy.userId } ?: UserEntity(
                id = policy.userId,
                fullName = "Policyholder",
                dob = "--",
                mobileNumber = "--",
                email = "--",
                address = "--"
            )
            val now = Date()
            val daysRemaining = PolicyDateHelper.calculateDaysRemaining(policy.nextPremiumDate, now)
            val status = PolicyDateHelper.determineStatus(daysRemaining)

            PolicyWithDetails(
                policy = policy,
                user = user,
                receipts = receipts,
                daysRemaining = daysRemaining,
                status = status
            )
        }
    }

    suspend fun registerUserAndPolicy(
        fullName: String,
        dob: String,
        mobileNumber: String,
        email: String,
        address: String,
        profilePhotoUri: String?,
        policyNumber: String,
        policyName: String,
        policyType: String,
        sumAssured: Double,
        premiumAmount: Double,
        frequencyMonths: Int,
        startDate: String,
        nomineeName: String,
        nomineeRelation: String,
        agentName: String,
        agentCode: String,
        policyDocUri: String?,
        kycDocUri: String?,
        notes: String
    ): Pair<Long, String> {
        val user = UserEntity(
            fullName = fullName.trim(),
            dob = dob.trim(),
            mobileNumber = mobileNumber.trim(),
            email = email.trim(),
            address = address.trim(),
            profilePhotoUri = profilePhotoUri
        )
        val userId = userDao.insertUser(user)

        val nextDate = PolicyDateHelper.calculateNextPremiumDate(startDate, frequencyMonths)
        val policy = PolicyEntity(
            userId = userId,
            policyNumber = policyNumber.trim(),
            policyName = policyName.trim(),
            policyType = policyType.trim(),
            sumAssured = sumAssured,
            premiumAmount = premiumAmount,
            frequencyMonths = frequencyMonths,
            startDate = startDate.trim(),
            nextPremiumDate = nextDate,
            lastPaymentDate = startDate.trim(),
            nomineeName = nomineeName.trim(),
            nomineeRelation = nomineeRelation.trim(),
            agentName = agentName.trim(),
            agentCode = agentCode.trim(),
            policyDocUri = policyDocUri,
            kycDocUri = kycDocUri,
            notes = notes.trim()
        )
        val policyId = policyDao.insertPolicy(policy)

        // Add initial payment record
        receiptDao.insertReceipt(
            PaymentReceiptEntity(
                policyId = policyId,
                paymentDate = startDate.trim(),
                amountPaid = premiumAmount,
                transactionRef = "INITIAL-REG-${policyNumber.takeLast(4)}",
                paymentMode = "Policy Inception Payment",
                periodCovered = "Start: $startDate"
            )
        )

        // Direct Cloud Sync to Supabase!
        var supabaseStatusMsg = "Stored locally"
        try {
            val supabasePolicy = SupabasePolicyDto(
                policyNumber = policyNumber.trim(),
                customerName = fullName.trim(),
                customerDob = dob.trim(),
                mobileNumber = mobileNumber.trim(),
                email = email.trim(),
                address = address.trim(),
                policyName = policyName.trim(),
                policyType = policyType.trim(),
                sumAssured = sumAssured,
                premiumAmount = premiumAmount,
                frequencyMonths = frequencyMonths,
                startDate = startDate.trim(),
                nextPremiumDate = nextDate,
                lastPaymentDate = startDate.trim(),
                nomineeName = nomineeName.trim(),
                nomineeRelation = nomineeRelation.trim(),
                agentName = agentName.trim(),
                agentCode = agentCode.trim(),
                status = "ACTIVE",
                notes = notes.trim()
            )
            val result = supabaseClient.syncPolicy(supabasePolicy)
            supabaseStatusMsg = if (result.isSuccess) {
                "Synced to Supabase ☁️"
            } else {
                "Local saved (Supabase queued)"
            }
        } catch (_: Exception) {
            // Local persistence remains intact even if network unavailable
            supabaseStatusMsg = "Saved locally"
        }

        return Pair(policyId, supabaseStatusMsg)
    }

    suspend fun recordPayment(
        policyId: Long,
        amount: Double,
        paymentDate: String,
        paymentMode: String,
        transactionRef: String,
        receiptImageUri: String?,
        advanceCycle: Boolean = true
    ) {
        val policy = policyDao.getPolicyById(policyId) ?: return

        val ref = transactionRef.ifBlank { "TXN-${System.currentTimeMillis() % 1000000}" }
        receiptDao.insertReceipt(
            PaymentReceiptEntity(
                policyId = policyId,
                paymentDate = paymentDate,
                amountPaid = amount,
                transactionRef = ref,
                paymentMode = paymentMode,
                receiptImageUri = receiptImageUri,
                periodCovered = "Due: ${policy.nextPremiumDate}"
            )
        )

        val updatedNextDate = if (advanceCycle) {
            PolicyDateHelper.calculateNextPremiumDate(
                policy.nextPremiumDate,
                policy.frequencyMonths
            )
        } else {
            policy.nextPremiumDate
        }

        policyDao.updatePolicy(
            policy.copy(
                lastPaymentDate = paymentDate,
                nextPremiumDate = updatedNextDate
            )
        )

        // Sync receipt and updated dates to Supabase
        try {
            val receiptDto = SupabasePaymentReceiptDto(
                policyNumber = policy.policyNumber,
                paymentDate = paymentDate,
                amountPaid = amount,
                paymentMode = paymentMode,
                transactionRef = ref,
                periodCovered = "Due: ${policy.nextPremiumDate}"
            )
            supabaseClient.syncReceipt(receiptDto)

            val user = userDao.getUserById(policy.userId)
            val updatedPolicyDto = SupabasePolicyDto(
                policyNumber = policy.policyNumber,
                customerName = user?.fullName ?: "Policyholder",
                customerDob = user?.dob ?: "",
                mobileNumber = user?.mobileNumber ?: "",
                email = user?.email ?: "",
                address = user?.address ?: "",
                policyName = policy.policyName,
                policyType = policy.policyType,
                sumAssured = policy.sumAssured,
                premiumAmount = policy.premiumAmount,
                frequencyMonths = policy.frequencyMonths,
                startDate = policy.startDate,
                nextPremiumDate = updatedNextDate,
                lastPaymentDate = paymentDate,
                nomineeName = policy.nomineeName,
                nomineeRelation = policy.nomineeRelation,
                agentName = policy.agentName,
                agentCode = policy.agentCode,
                status = "ACTIVE",
                notes = policy.notes
            )
            supabaseClient.syncPolicy(updatedPolicyDto)
        } catch (_: Exception) {
            // Graceful handling
        }
    }

    suspend fun syncAllWithSupabase(): Pair<Int, String> {
        val policies = getAllPoliciesWithDetails().firstOrNull().orEmpty()
        var syncedCount = 0
        var lastError = ""

        for (item in policies) {
            val dto = SupabasePolicyDto(
                policyNumber = item.policy.policyNumber,
                customerName = item.user.fullName,
                customerDob = item.user.dob,
                mobileNumber = item.user.mobileNumber,
                email = item.user.email,
                address = item.user.address,
                policyName = item.policy.policyName,
                policyType = item.policy.policyType,
                sumAssured = item.policy.sumAssured,
                premiumAmount = item.policy.premiumAmount,
                frequencyMonths = item.policy.frequencyMonths,
                startDate = item.policy.startDate,
                nextPremiumDate = item.policy.nextPremiumDate,
                lastPaymentDate = item.policy.lastPaymentDate,
                nomineeName = item.policy.nomineeName,
                nomineeRelation = item.policy.nomineeRelation,
                agentName = item.policy.agentName,
                agentCode = item.policy.agentCode,
                status = item.status.name,
                notes = item.policy.notes
            )
            val result = supabaseClient.syncPolicy(dto)
            if (result.isSuccess) {
                syncedCount++
            } else {
                lastError = result.exceptionOrNull()?.message ?: "Sync error"
            }
        }

        return Pair(syncedCount, lastError)
    }

    suspend fun updatePolicy(policy: PolicyEntity) {
        policyDao.updatePolicy(policy)
    }

    suspend fun deletePolicy(policyId: Long) {
        policyDao.deletePolicyById(policyId)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }
}
