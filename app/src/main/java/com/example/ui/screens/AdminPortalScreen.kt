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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.PolicyWithDetails
import com.example.data.model.PremiumStatus
import com.example.ui.components.DocumentViewerDialog
import com.example.ui.components.RecordPaymentDialog
import com.example.ui.theme.LicAmberContainer
import com.example.ui.theme.LicAmberDue
import com.example.ui.theme.LicEmerald
import com.example.ui.theme.LicEmeraldContainer
import com.example.ui.theme.LicGoldAccent
import com.example.ui.theme.LicNavyDark
import com.example.ui.theme.LicNavyPrimary
import com.example.ui.theme.LicRedContainer
import com.example.ui.theme.LicRedOverdue
import com.example.ui.viewmodel.LicViewModel

@Composable
fun AdminPortalScreen(
    viewModel: LicViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    onNavigateToHistory: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allPolicies by viewModel.allPolicies.collectAsState()
    val filteredPolicies by viewModel.filteredPolicies.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val previewDoc by viewModel.previewDocument.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val context = LocalContext.current

    var policyForPayment by remember { mutableStateOf<PolicyWithDetails?>(null) }
    var policyToDelete by remember { mutableStateOf<PolicyWithDetails?>(null) }
    var showExportModal by remember { mutableStateOf(false) }

    // Dialogs
    previewDoc?.let { doc ->
        DocumentViewerDialog(data = doc, onDismiss = { viewModel.closeDocumentPreview() })
    }

    policyForPayment?.let { target ->
        RecordPaymentDialog(
            item = target,
            onDismiss = { policyForPayment = null },
            onConfirm = { amount, date, mode, ref, uri ->
                viewModel.recordPayment(
                    policyId = target.policy.id,
                    amount = amount,
                    paymentDate = date,
                    paymentMode = mode,
                    transactionRef = ref,
                    receiptImageUri = uri
                )
                policyForPayment = null
            }
        )
    }

    if (policyToDelete != null) {
        AlertDialog(
            onDismissRequest = { policyToDelete = null },
            title = { Text("Delete Policy #${policyToDelete!!.policy.policyNumber}?") },
            text = { Text("Are you sure you want to delete this policy from agent management? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePolicy(policyToDelete!!.policy.id)
                        policyToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LicRedOverdue)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { policyToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Statistics computation
    val totalPolicies = allPolicies.size
    val overdueCount = allPolicies.count { it.status == PremiumStatus.OVERDUE }
    val dueSoonCount = allPolicies.count { it.status == PremiumStatus.DUE_SOON }
    val totalPremiumValue = allPolicies.sumOf { it.policy.premiumAmount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_portal_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin Header Banner
        item {
            Card(
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = LicNavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = LicGoldAccent,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "LIC Agent / Admin Portal",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Policy Management & Due Reminders",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LicGoldAccent
                                )
                            }
                        }

                        // Action Buttons: Sync Cloud & Export
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.syncAllWithSupabase() },
                                colors = ButtonDefaults.buttonColors(containerColor = LicNavyPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("admin_sync_cloud_btn")
                            ) {
                                Icon(
                                    Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = LicGoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (isSyncing) "Syncing..." else "Sync Cloud",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = {
                                    val csvReport = buildString {
                                        appendLine("Policy Number,Customer Name,Mobile,Plan Name,Premium (INR),Frequency,Next Due Date,Status")
                                        allPolicies.forEach { p ->
                                            appendLine("${p.policy.policyNumber},${p.user.fullName},${p.user.mobileNumber},${p.policy.policyName},${p.policy.premiumAmount},${p.policy.frequencyMonths}M,${p.policy.nextPremiumDate},${p.status.name}")
                                        }
                                    }
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "=== LIC MITR AGENT POLICY REPORT ===\n\n$csvReport")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Export Policy Report"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("admin_export_btn")
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Stat Cards in 2x2 Grid
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AdminStatPill(
                            title = "Total Policies",
                            value = totalPolicies.toString(),
                            accentColor = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatPill(
                            title = "Total Premium",
                            value = PolicyDateHelper.formatCurrency(totalPremiumValue),
                            accentColor = LicGoldAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AdminStatPill(
                            title = "🟡 Due Soon (30d)",
                            value = dueSoonCount.toString(),
                            accentColor = LicAmberDue,
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatPill(
                            title = "🔴 Overdue / Pending",
                            value = overdueCount.toString(),
                            accentColor = LicRedOverdue,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Search & Filter Section
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search Policy Number, Customer Name, Mobile...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = filterStatus == null,
                            onClick = { viewModel.setFilterStatus(null) },
                            label = { Text("All (${allPolicies.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterStatus == PremiumStatus.OVERDUE,
                            onClick = { viewModel.setFilterStatus(PremiumStatus.OVERDUE) },
                            label = { Text("🔴 Overdue ($overdueCount)") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = LicRedContainer)
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterStatus == PremiumStatus.DUE_SOON,
                            onClick = { viewModel.setFilterStatus(PremiumStatus.DUE_SOON) },
                            label = { Text("🟡 Due Soon ($dueSoonCount)") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = LicAmberContainer)
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterStatus == PremiumStatus.PAID,
                            onClick = { viewModel.setFilterStatus(PremiumStatus.PAID) },
                            label = { Text("🟢 Paid (${allPolicies.size - overdueCount - dueSoonCount})") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = LicEmeraldContainer)
                        )
                    }
                }
            }
        }

        // Header for list
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customer Policies (${filteredPolicies.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onNavigateToRegister,
                    colors = ButtonDefaults.buttonColors(containerColor = LicNavyPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("admin_add_policy_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Policy", fontSize = 12.sp)
                }
            }
        }

        // Policy Cards List
        if (filteredPolicies.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No matching policies found", fontWeight = FontWeight.Bold)
                        Text("Try clearing filters or search query.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredPolicies) { item ->
                AdminPolicyItemCard(
                    item = item,
                    onViewDetails = { onNavigateToDetails(item.policy.id) },
                    onRecordPayment = { policyForPayment = item },
                    onDelete = { policyToDelete = item },
                    onViewDocuments = {
                        viewModel.showDocumentPreview(
                            title = "${item.user.fullName} - Documents",
                            subtitle = "Policy #${item.policy.policyNumber}",
                            imageUri = item.policy.policyDocUri ?: item.user.profilePhotoUri ?: "asset://img_user_avatar"
                        )
                    },
                    onRemindCustomer = {
                        val smsIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Dear ${item.user.fullName}, your LIC Policy #${item.policy.policyNumber} premium of ${PolicyDateHelper.formatCurrency(item.policy.premiumAmount)} is due on ${item.policy.nextPremiumDate}. Please pay on time to keep insurance coverage active."
                            )
                        }
                        context.startActivity(Intent.createChooser(smsIntent, "Send Due Reminder"))
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AdminStatPill(title: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
    }
}

@Composable
private fun AdminPolicyItemCard(
    item: PolicyWithDetails,
    onViewDetails: () -> Unit,
    onRecordPayment: () -> Unit,
    onDelete: () -> Unit,
    onViewDocuments: () -> Unit,
    onRemindCustomer: () -> Unit
) {
    val policy = item.policy
    val user = item.user

    val (statusLabel, statusColor, statusBg) = when (item.status) {
        PremiumStatus.PAID -> Triple("Paid 🟢", LicEmerald, LicEmeraldContainer)
        PremiumStatus.DUE_SOON -> Triple("Due Soon 🟡", LicAmberDue, LicAmberContainer)
        PremiumStatus.OVERDUE -> Triple("Pending 🔴", LicRedOverdue, LicRedContainer)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("admin_policy_card_${policy.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Customer row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.profilePhotoUri != null && user.profilePhotoUri.startsWith("asset://img_user_avatar")) {
                        Image(
                            painter = painterResource(id = R.drawable.img_user_avatar),
                            contentDescription = user.fullName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (user.profilePhotoUri != null && user.profilePhotoUri.isNotBlank()) {
                        AsyncImage(
                            model = user.profilePhotoUri,
                            contentDescription = user.fullName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = LicNavyPrimary)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LicNavyPrimary
                    )
                    Text(
                        text = "📱 ${user.mobileNumber} • DOB: ${user.dob}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Policy row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Policy Number", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = policy.policyNumber,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = LicNavyPrimary
                    )
                    Text(
                        text = policy.policyName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Premium & Freq", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${PolicyDateHelper.formatCurrency(policy.premiumAmount)} / ${policy.frequencyMonths}M",
                        fontWeight = FontWeight.Bold,
                        color = LicGoldAccent
                    )
                    Text(
                        text = "Next: ${policy.nextPremiumDate}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onRecordPayment,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pay", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onRemindCustomer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = LicAmberDue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Remind", fontSize = 11.sp, color = LicAmberDue)
                }

                OutlinedButton(
                    onClick = onViewDocuments,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Docs", fontSize = 11.sp)
                }

                Button(
                    onClick = onViewDetails,
                    colors = ButtonDefaults.buttonColors(containerColor = LicNavyPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Details", fontSize = 11.sp)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LicRedOverdue, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
