package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PaymentReceiptEntity
import com.example.data.model.PolicyDateHelper
import com.example.ui.components.DocumentViewerDialog
import com.example.ui.components.RecordPaymentDialog
import com.example.ui.theme.LicEmerald
import com.example.ui.theme.LicEmeraldContainer
import com.example.ui.theme.LicGoldAccent
import com.example.ui.theme.LicNavyPrimary
import com.example.ui.viewmodel.LicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumHistoryScreen(
    policyId: Long,
    viewModel: LicViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val policies by viewModel.allPolicies.collectAsState()
    val item = policies.find { it.policy.id == policyId }
    val previewDoc by viewModel.previewDocument.collectAsState()

    var showPaymentDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("history_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LicNavyPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (item != null) {
                FloatingActionButton(
                    onClick = { showPaymentDialog = true },
                    containerColor = LicNavyPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_payment_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Payment")
                }
            }
        }
    ) { innerPadding ->
        if (item == null) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Policy not found.")
            }
            return@Scaffold
        }

        val receipts = item.receipts
        val totalPaid = receipts.sumOf { it.amountPaid }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("premium_history_list"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                // Summary Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Policy #${item.policy.policyNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LicNavyPrimary
                        )
                        Text(
                            text = item.policy.policyName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Collected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = PolicyDateHelper.formatCurrency(totalPaid),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = LicEmerald
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Installments", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${receipts.size} Payments",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = LicNavyPrimary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Receipts & Records Timeline",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (receipts.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = LicNavyPrimary.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Payment Receipts Recorded Yet", fontWeight = FontWeight.Bold)
                            Text("Tap + to upload a receipt and record your first premium.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(receipts) { receipt ->
                    ReceiptTimelineCard(
                        receipt = receipt,
                        onViewReceipt = {
                            viewModel.showDocumentPreview(
                                title = "Premium Receipt #${receipt.id}",
                                subtitle = "Paid: ${PolicyDateHelper.formatCurrency(receipt.amountPaid)} on ${receipt.paymentDate}",
                                imageUri = receipt.receiptImageUri
                            )
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun ReceiptTimelineCard(
    receipt: PaymentReceiptEntity,
    onViewReceipt: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LicEmeraldContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = LicEmerald,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = PolicyDateHelper.formatCurrency(receipt.amountPaid),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LicNavyPrimary
                    )
                    Text(
                        text = receipt.paymentDate,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Mode: ${receipt.paymentMode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Ref: ${receipt.transactionRef}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (receipt.periodCovered.isNotBlank()) {
                    Text(
                        text = receipt.periodCovered,
                        style = MaterialTheme.typography.labelSmall,
                        color = LicGoldAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Receipt thumbnail button
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                    .clickable { onViewReceipt() },
                contentAlignment = Alignment.Center
            ) {
                if (receipt.receiptImageUri != null) {
                    AsyncImage(
                        model = receipt.receiptImageUri,
                        contentDescription = "Receipt",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "View receipt",
                        tint = LicNavyPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
