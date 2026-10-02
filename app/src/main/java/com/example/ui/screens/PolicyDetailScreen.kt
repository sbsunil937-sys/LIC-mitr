package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.PolicyDateHelper
import com.example.data.model.PremiumStatus
import com.example.ui.components.DocumentViewerDialog
import com.example.ui.components.RecordPaymentDialog
import com.example.ui.theme.LicAmberContainer
import com.example.ui.theme.LicAmberDue
import com.example.ui.theme.LicEmerald
import com.example.ui.theme.LicEmeraldContainer
import com.example.ui.theme.LicGoldAccent
import com.example.ui.theme.LicNavyPrimary
import com.example.ui.theme.LicRedContainer
import com.example.ui.theme.LicRedOverdue
import com.example.ui.viewmodel.LicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyDetailScreen(
    policyId: Long,
    viewModel: LicViewModel,
    onBack: () -> Unit,
    onNavigateToHistory: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val policies by viewModel.allPolicies.collectAsState()
    val item = policies.find { it.policy.id == policyId }
    val previewDoc by viewModel.previewDocument.collectAsState()
    val context = LocalContext.current

    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    previewDoc?.let { doc ->
        DocumentViewerDialog(data = doc, onDismiss = { viewModel.closeDocumentPreview() })
    }

    if (item != null && showPaymentDialog) {
        RecordPaymentDialog(
            item = item,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { amount, date, mode, ref, uri ->
                viewModel.recordPayment(
                    policyId = item.policy.id,
                    amount = amount,
                    paymentDate = date,
                    paymentMode = mode,
                    transactionRef = ref,
                    receiptImageUri = uri
                )
                showPaymentDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog && item != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Policy?") },
            text = { Text("Are you sure you want to remove policy #${item.policy.policyNumber}? All associated payment records will also be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePolicy(item.policy.id)
                        showDeleteConfirmDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LicRedOverdue)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Policy Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (item != null) {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "LIC Policy: ${item.policy.policyNumber} (${item.policy.policyName})\nNext Due: ${item.policy.nextPremiumDate}\nPremium: ${PolicyDateHelper.formatCurrency(item.policy.premiumAmount)}"
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Policy"))
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LicNavyPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        if (item == null) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Policy not found or has been deleted.")
            }
            return@Scaffold
        }

        val policy = item.policy
        val user = item.user

        val (statusText, statusColor, statusBg, statusIcon) = when (item.status) {
            PremiumStatus.PAID -> Triple4("Premium Paid 🟢", LicEmerald, LicEmeraldContainer, Icons.Default.CheckCircle)
            PremiumStatus.DUE_SOON -> Triple4("Due Soon 🟡", LicAmberDue, LicAmberContainer, Icons.Default.Warning)
            PremiumStatus.OVERDUE -> Triple4("Premium Pending 🔴", LicRedOverdue, LicRedContainer, Icons.Default.Warning)
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Status & Policy No Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = statusBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Text(
                            text = policy.policyType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = policy.policyName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = LicNavyPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Policy Number: ${policy.policyNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Next Premium Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Next Premium Due Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = PolicyDateHelper.formatDisplayDate(policy.nextPremiumDate),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = LicNavyPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Countdown", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (item.daysRemaining >= 0) "${item.daysRemaining} Days Left" else "${kotlin.math.abs(item.daysRemaining)} Days Overdue",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = statusColor
                            )
                        }
                    }
                }
            }

            // Financial Summary Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Financial Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow("Sum Assured (Guaranteed):", PolicyDateHelper.formatCurrency(policy.sumAssured))
                    DetailRow("Premium Amount:", PolicyDateHelper.formatCurrency(policy.premiumAmount))
                    DetailRow("Premium Frequency:", PolicyDateHelper.getFrequencyLabel(policy.frequencyMonths))
                    DetailRow("Policy Start Date:", policy.startDate)
                    DetailRow("Last Payment Recorded:", policy.lastPaymentDate ?: "None")
                    DetailRow("Total Receipts Logged:", "${item.receipts.size} receipt(s)")
                }
            }

            // Policyholder & Nominee Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Policyholder & Nominee", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow("Policyholder Name:", user.fullName)
                    DetailRow("Date of Birth:", user.dob)
                    DetailRow("Mobile Number:", user.mobileNumber)
                    DetailRow("Email:", user.email.ifBlank { "--" })
                    DetailRow("Address:", user.address.ifBlank { "--" })
                    DetailRow("Nominee:", "${policy.nomineeName} (${policy.nomineeRelation})")
                    DetailRow("Servicing Agent:", "${policy.agentName} [${policy.agentCode}]")
                }
            }

            // Attached Documents & KYC Vault Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Attached Documents & KYC", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DocumentThumbItem(
                            label = "Customer Photo",
                            icon = Icons.Default.Person,
                            onClick = {
                                viewModel.showDocumentPreview(
                                    title = "${user.fullName} - Photo",
                                    subtitle = "Profile Picture",
                                    imageUri = user.profilePhotoUri ?: "asset://img_user_avatar"
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        DocumentThumbItem(
                            label = "Policy Bond",
                            icon = Icons.Default.Description,
                            onClick = {
                                viewModel.showDocumentPreview(
                                    title = "Policy Bond #${policy.policyNumber}",
                                    subtitle = policy.policyName,
                                    imageUri = policy.policyDocUri
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        DocumentThumbItem(
                            label = "Aadhaar / KYC",
                            icon = Icons.Default.Security,
                            onClick = {
                                viewModel.showDocumentPreview(
                                    title = "KYC Verification Proof",
                                    subtitle = "${user.fullName} ID",
                                    imageUri = policy.kycDocUri
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Primary Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showPaymentDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LicNavyPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("detail_pay_premium_btn")
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Receipt / Record Payment", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onNavigateToHistory(policy.id) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, LicGoldAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("detail_history_btn")
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = LicGoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Payment & Receipt History", color = LicGoldAccent, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally).testTag("delete_policy_btn")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = LicRedOverdue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Policy", color = LicRedOverdue)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun DocumentThumbItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = modifier
            .height(74.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = LicNavyPrimary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

private data class Triple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
