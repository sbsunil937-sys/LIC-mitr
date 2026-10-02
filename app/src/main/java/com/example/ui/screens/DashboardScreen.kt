package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.LicGoldLight
import com.example.ui.theme.LicNavyDark
import com.example.ui.theme.LicNavyPrimary
import com.example.ui.theme.LicRedContainer
import com.example.ui.theme.LicRedOverdue
import com.example.ui.theme.LicTextPrimary
import com.example.ui.viewmodel.LicViewModel

@Composable
fun DashboardScreen(
    viewModel: LicViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    onNavigateToHistory: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val policies by viewModel.allPolicies.collectAsState()
    val selectedPolicy by viewModel.selectedPolicy.collectAsState()
    val previewDoc by viewModel.previewDocument.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()
    val context = LocalContext.current

    var showPaymentDialog by remember { mutableStateOf(false) }

    // Dialogs
    previewDoc?.let { doc ->
        DocumentViewerDialog(data = doc, onDismiss = { viewModel.closeDocumentPreview() })
    }

    if (showPaymentDialog && selectedPolicy != null) {
        RecordPaymentDialog(
            item = selectedPolicy!!,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { amount, date, mode, ref, uri ->
                viewModel.recordPayment(
                    policyId = selectedPolicy!!.policy.id,
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Banner
        item {
            HeroHeaderSection(
                onRegisterClick = onNavigateToRegister,
                onSyncSupabaseClick = { viewModel.syncAllWithSupabase() },
                isSyncing = isSyncing,
                supabaseStatus = supabaseStatus
            )
        }

        // Multi-Policy Selector Chip Row (if more than 1 policy)
        if (policies.size > 1) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Your Active Policies (${policies.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(policies) { item ->
                            val isSelected = item.policy.id == selectedPolicy?.policy?.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectPolicy(item.policy.id) },
                                label = {
                                    Text("Policy #${item.policy.policyNumber}")
                                },
                                leadingIcon = {
                                    val statusColor = when (item.status) {
                                        PremiumStatus.PAID -> LicEmerald
                                        PremiumStatus.DUE_SOON -> LicAmberDue
                                        PremiumStatus.OVERDUE -> LicRedOverdue
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LicNavyPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("policy_chip_${item.policy.id}")
                            )
                        }
                    }
                }
            }
        }

        // Main User Dashboard Card (The iconic requested card format!)
        item {
            if (selectedPolicy != null) {
                UserDashboardPolicyCard(
                    item = selectedPolicy!!,
                    onViewDetails = { onNavigateToDetails(selectedPolicy!!.policy.id) },
                    onViewHistory = { onNavigateToHistory(selectedPolicy!!.policy.id) },
                    onUploadReceipt = { showPaymentDialog = true },
                    onViewUserPhoto = {
                        viewModel.showDocumentPreview(
                            title = "${selectedPolicy!!.user.fullName}'s Profile Photo",
                            subtitle = "Policy #${selectedPolicy!!.policy.policyNumber}",
                            imageUri = selectedPolicy!!.user.profilePhotoUri ?: "asset://img_user_avatar"
                        )
                    },
                    onSharePolicy = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                """
                                LIC Policy Summary
                                Holder: ${selectedPolicy!!.user.fullName}
                                Policy No: ${selectedPolicy!!.policy.policyNumber}
                                Plan: ${selectedPolicy!!.policy.policyName}
                                Next Premium Due: ${selectedPolicy!!.policy.nextPremiumDate}
                                Premium Amount: ${PolicyDateHelper.formatCurrency(selectedPolicy!!.policy.premiumAmount)}
                                Status: ${selectedPolicy!!.status.name}
                                """.trimIndent()
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Policy Details"))
                    }
                )
            } else {
                EmptyPolicyStateCard(onRegisterClick = onNavigateToRegister)
            }
        }

        // Quick Highlights / Plan Features
        if (selectedPolicy != null) {
            item {
                PolicyHighlightsSection(item = selectedPolicy!!)
            }
        }

        // LIC Customer Support & Help Card
        item {
            LicHelplineCard()
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroHeaderSection(
    onRegisterClick: () -> Unit,
    onSyncSupabaseClick: () -> Unit,
    isSyncing: Boolean,
    supabaseStatus: String
) {
    Card(
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = LicNavyPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_header")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background subtle gradient overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                LicNavyDark.copy(alpha = 0.85f),
                                LicNavyPrimary.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = LicGoldLight,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_app_icon),
                                    contentDescription = "LIC Mitr Emblem",
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "LIC Mitr",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Yogakshemam Vahamyaham",
                                style = MaterialTheme.typography.labelSmall,
                                color = LicGoldLight,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }

                    Button(
                        onClick = onRegisterClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LicGoldAccent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_new_policy_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Policy", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Supabase Cloud Sync Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { onSyncSupabaseClick() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("supabase_sync_pill"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSyncing) Icons.Default.CloudSync else Icons.Default.CloudDone,
                                contentDescription = "Supabase Status",
                                tint = LicGoldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSyncing) "Syncing with Supabase cloud..." else "Supabase DB: fldgbvzimoxfnjavzdpg",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "Sync Now ↻",
                            color = LicGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Banner graphic & quick greeting
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_lic_hero),
                            contentDescription = "Insurance Protection",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Track & Manage Premiums",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Automated due dates, 6-month tracking, receipt uploads & digital bond vault.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * The exact LIC Policy Card format requested by the user:
 * ┌─────────────────────────────┐
 * │       LIC POLICY            │
 * │      👤 User Photo          │
 * │                             │
 * │ Name: Sunil                 │
 * │ DOB: 16/12/2003             │
 * │ Policy No: XXXXX12345       │
 * │                             │
 * │ Premium: ₹5,000             │
 * │ Frequency: 6 Months         │
 * │                             │
 * │ Next Premium                │
 * │ 10 April 2027               │
 * │                             │
 * │     190 Days Remaining      │
 * │                             │
 * │ [Policy Details]            │
 * │ [Premium History]           │
 * │ [Upload Receipt]            │
 * └─────────────────────────────┘
 */
@Composable
fun UserDashboardPolicyCard(
    item: PolicyWithDetails,
    onViewDetails: () -> Unit,
    onViewHistory: () -> Unit,
    onUploadReceipt: () -> Unit,
    onViewUserPhoto: () -> Unit,
    onSharePolicy: () -> Unit
) {
    val policy = item.policy
    val user = item.user

    // Status config
    val (statusLabel, statusColor, statusBg, statusIcon) = when (item.status) {
        PremiumStatus.PAID -> Quad(
            "Premium Paid 🟢",
            LicEmerald,
            LicEmeraldContainer,
            Icons.Default.CheckCircle
        )
        PremiumStatus.DUE_SOON -> Quad(
            "Due Soon 🟡",
            LicAmberDue,
            LicAmberContainer,
            Icons.Default.Warning
        )
        PremiumStatus.OVERDUE -> Quad(
            "Premium Pending 🔴",
            LicRedOverdue,
            LicRedContainer,
            Icons.Default.Warning
        )
    }

    val daysText = when {
        item.daysRemaining > 0 -> "${item.daysRemaining} Days Remaining"
        item.daysRemaining == 0L -> "Due Today!"
        else -> "${kotlin.math.abs(item.daysRemaining)} Days Overdue!"
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.5.dp, LicGoldLight.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = LicNavyPrimary.copy(alpha = 0.2f))
            .testTag("lic_policy_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar inside Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = LicGoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIC POLICY",
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LicNavyPrimary,
                        fontSize = 14.sp
                    )
                }

                IconButton(
                    onClick = onSharePolicy,
                    modifier = Modifier.size(32.dp).testTag("share_policy_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = LicNavyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User Photo with Gold Ring
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(LicNavyPrimary)
                    .border(3.dp, LicGoldLight, CircleShape)
                    .clickable { onViewUserPhoto() }
                    .testTag("user_photo_avatar"),
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
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = user.fullName,
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customer Name & DOB
            Text(
                text = "Name: ${user.fullName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LicTextPrimary
            )
            Text(
                text = "DOB: ${user.dob}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Policy Number
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Policy No: ${policy.policyNumber}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = LicNavyPrimary
                )
            }

            Text(
                text = policy.policyName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Premium & Frequency Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Premium",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = PolicyDateHelper.formatCurrency(policy.premiumAmount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = LicNavyPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                        .background(Color(0xFFE2E8F0))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Frequency",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${policy.frequencyMonths} Months",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = LicGoldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Next Premium Date & Remaining Days Box
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = statusBg),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Next Premium Due",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF334155)
                    )
                    Text(
                        text = PolicyDateHelper.formatDisplayDate(policy.nextPremiumDate),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = LicNavyDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = statusColor,
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = daysText,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons requested: [Policy Details] [Premium History] [Upload Receipt]
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary Action: [Upload Receipt / Record Payment]
                Button(
                    onClick = onUploadReceipt,
                    colors = ButtonDefaults.buttonColors(containerColor = LicNavyPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("upload_receipt_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Receipt / Pay Premium", fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // [Policy Details]
                    OutlinedButton(
                        onClick = onViewDetails,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, LicNavyPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("policy_details_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = LicNavyPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Policy Details",
                            fontWeight = FontWeight.SemiBold,
                            color = LicNavyPrimary,
                            fontSize = 12.sp
                        )
                    }

                    // [Premium History]
                    OutlinedButton(
                        onClick = onViewHistory,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, LicGoldAccent),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("premium_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = LicGoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Premium History",
                            fontWeight = FontWeight.SemiBold,
                            color = LicGoldAccent,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PolicyHighlightsSection(item: PolicyWithDetails) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("policy_highlights_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Key Policy Highlights",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                InfoHighlightCell(
                    label = "Sum Assured",
                    value = PolicyDateHelper.formatCurrency(item.policy.sumAssured),
                    modifier = Modifier.weight(1f)
                )
                InfoHighlightCell(
                    label = "Nominee",
                    value = "${item.policy.nomineeName} (${item.policy.nomineeRelation})",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                InfoHighlightCell(
                    label = "Policy Inception",
                    value = item.policy.startDate,
                    modifier = Modifier.weight(1f)
                )
                InfoHighlightCell(
                    label = "Servicing Agent",
                    value = "${item.policy.agentName} [${item.policy.agentCode}]",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InfoHighlightCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = LicNavyPrimary
        )
    }
}

@Composable
private fun LicHelplineCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        border = BorderStroke(1.dp, LicEmerald.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("lic_helpline_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(LicEmerald.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Helpline",
                    tint = LicEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Official LIC Customer Care",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF065F46)
                )
                Text(
                    text = "Call 022 6827 6827 • SMS LICHELP <PolicyNo> to 92224 92224",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF047857)
                )
            }
        }
    }
}

@Composable
private fun EmptyPolicyStateCard(onRegisterClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = LicNavyPrimary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Policies Registered Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Register your LIC policy to automatically track next premium due dates and upload receipts.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onRegisterClick,
                colors = ButtonDefaults.buttonColors(containerColor = LicNavyPrimary)
            ) {
                Text("Register First Policy")
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
