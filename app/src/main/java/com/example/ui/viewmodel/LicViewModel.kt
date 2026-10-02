package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.PolicyDateHelper
import com.example.data.model.PolicyEntity
import com.example.data.model.PolicyWithDetails
import com.example.data.model.PremiumStatus
import com.example.data.model.UserEntity
import com.example.data.repository.LicRepository
import com.example.data.supabase.SupabaseConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class LicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LicRepository = LicRepository(AppDatabase.getInstance(application))

    val allPolicies: StateFlow<List<PolicyWithDetails>> = repository.getAllPoliciesWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPolicyId = MutableStateFlow<Long?>(null)
    val selectedPolicyId: StateFlow<Long?> = _selectedPolicyId.asStateFlow()

    val selectedPolicy: StateFlow<PolicyWithDetails?> = combine(
        allPolicies,
        _selectedPolicyId
    ) { policies, id ->
        if (id != null) {
            policies.find { it.policy.id == id } ?: policies.firstOrNull()
        } else {
            policies.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterStatus = MutableStateFlow<PremiumStatus?>(null)
    val filterStatus: StateFlow<PremiumStatus?> = _filterStatus.asStateFlow()

    val filteredPolicies: StateFlow<List<PolicyWithDetails>> = combine(
        allPolicies,
        _searchQuery,
        _filterStatus
    ) { list, query, status ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.policy.policyNumber.contains(query, ignoreCase = true) ||
                item.policy.policyName.contains(query, ignoreCase = true) ||
                item.user.fullName.contains(query, ignoreCase = true) ||
                item.user.mobileNumber.contains(query, ignoreCase = true) ||
                item.policy.agentName.contains(query, ignoreCase = true)

            val matchesStatus = status == null || item.status == status

            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin / Agent Mode toggle
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    // Supabase Sync States
    private val _supabaseStatus = MutableStateFlow("Connected (${SupabaseConfig.projectId})")
    val supabaseStatus: StateFlow<String> = _supabaseStatus.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    // Preview Document Modal state
    private val _previewDocument = MutableStateFlow<DocumentPreviewData?>(null)
    val previewDocument: StateFlow<DocumentPreviewData?> = _previewDocument.asStateFlow()

    // Snackbar event
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    fun selectPolicy(id: Long) {
        _selectedPolicyId.value = id
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterStatus(status: PremiumStatus?) {
        _filterStatus.value = status
    }

    fun toggleAdminMode() {
        _isAdminMode.value = !_isAdminMode.value
    }

    fun showDocumentPreview(title: String, subtitle: String, imageUri: String?, isAsset: Boolean = false) {
        _previewDocument.value = DocumentPreviewData(title, subtitle, imageUri, isAsset)
    }

    fun closeDocumentPreview() {
        _previewDocument.value = null
    }

    fun registerNewPolicy(
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
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val (newPolicyId, supabaseMsg) = repository.registerUserAndPolicy(
                    fullName = fullName,
                    dob = dob,
                    mobileNumber = mobileNumber,
                    email = email,
                    address = address,
                    profilePhotoUri = profilePhotoUri,
                    policyNumber = policyNumber,
                    policyName = policyName,
                    policyType = policyType,
                    sumAssured = sumAssured,
                    premiumAmount = premiumAmount,
                    frequencyMonths = frequencyMonths,
                    startDate = startDate,
                    nomineeName = nomineeName,
                    nomineeRelation = nomineeRelation,
                    agentName = agentName,
                    agentCode = agentCode,
                    policyDocUri = policyDocUri,
                    kycDocUri = kycDocUri,
                    notes = notes
                )
                _selectedPolicyId.value = newPolicyId
                _userMessage.emit("Policy $policyNumber saved & $supabaseMsg")
                onSuccess()
            } catch (e: Exception) {
                _userMessage.emit("Error registering policy: ${e.message}")
            }
        }
    }

    fun recordPayment(
        policyId: Long,
        amount: Double,
        paymentDate: String,
        paymentMode: String,
        transactionRef: String,
        receiptImageUri: String?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.recordPayment(
                    policyId = policyId,
                    amount = amount,
                    paymentDate = paymentDate,
                    paymentMode = paymentMode,
                    transactionRef = transactionRef,
                    receiptImageUri = receiptImageUri,
                    advanceCycle = true
                )
                _userMessage.emit("Payment of ${PolicyDateHelper.formatCurrency(amount)} recorded & synced with Supabase!")
                onSuccess()
            } catch (e: Exception) {
                _userMessage.emit("Failed to record payment: ${e.message}")
            }
        }
    }

    fun syncAllWithSupabase() {
        viewModelScope.launch {
            _isSyncing.value = true
            _userMessage.emit("Syncing all policies to Supabase (${SupabaseConfig.projectId})...")
            try {
                val (count, error) = repository.syncAllWithSupabase()
                if (count > 0) {
                    _userMessage.emit("Cloud Sync Complete: $count policies synced to Supabase ☁️")
                    _supabaseStatus.value = "Synced: $count policies"
                } else if (error.isNotBlank()) {
                    _userMessage.emit("Supabase Cloud Notice: $error")
                } else {
                    _userMessage.emit("Supabase Cloud is up to date ☁️")
                }
            } catch (e: Exception) {
                _userMessage.emit("Sync issue: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun deletePolicy(policyId: Long) {
        viewModelScope.launch {
            try {
                repository.deletePolicy(policyId)
                if (_selectedPolicyId.value == policyId) {
                    _selectedPolicyId.value = null
                }
                _userMessage.emit("Policy deleted successfully.")
            } catch (e: Exception) {
                _userMessage.emit("Failed to delete policy: ${e.message}")
            }
        }
    }
}

data class DocumentPreviewData(
    val title: String,
    val subtitle: String,
    val imageUri: String?,
    val isAsset: Boolean = false
)
