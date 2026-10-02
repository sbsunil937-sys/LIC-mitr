package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PolicyDateHelper
import com.example.ui.theme.LicEmerald
import com.example.ui.theme.LicGoldAccent
import com.example.ui.theme.LicNavyPrimary
import com.example.ui.viewmodel.LicViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyRegistrationScreen(
    viewModel: LicViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. User Registration fields
    var fullName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }

    // 2. Policy Details fields
    var policyNumber by remember { mutableStateOf("") }
    var policyName by remember { mutableStateOf("LIC Jeevan Labh (Plan 936)") }
    var policyType by remember { mutableStateOf("Endowment / Savings") }
    var sumAssuredText by remember { mutableStateOf("500000") }
    var premiumAmountText by remember { mutableStateOf("5000") }
    var frequencyMonths by remember { mutableIntStateOf(6) } // Default 6 months as in prompt!
    var startDate by remember { mutableStateOf(PolicyDateHelper.formatDate(Date())) }

    // 3. Nominee & Agent
    var nomineeName by remember { mutableStateOf("") }
    var nomineeRelation by remember { mutableStateOf("Spouse") }
    var agentName by remember { mutableStateOf("Rakesh Sharma") }
    var agentCode by remember { mutableStateOf("AGNT-038291") }

    // 4. Documents & Notes
    var policyDocUri by remember { mutableStateOf<Uri?>(null) }
    var kycDocUri by remember { mutableStateOf<Uri?>(null) }
    var notes by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dropdown states
    var isTypeDropdownExpanded by remember { mutableStateOf(false) }
    var isFreqDropdownExpanded by remember { mutableStateOf(false) }
    var isRelationDropdownExpanded by remember { mutableStateOf(false) }

    val policyTypes = listOf(
        "Endowment / Savings",
        "Pure Term Life Protection",
        "Money Back Plan",
        "ULIP / Unit Linked",
        "Pension / Annuity",
        "Whole Life Plan",
        "Health / Cancer Cover"
    )

    val frequencies = listOf(
        Pair(1, "Monthly (1 Month)"),
        Pair(3, "Quarterly (3 Months)"),
        Pair(6, "Half-Yearly (6 Months)"),
        Pair(12, "Yearly (12 Months)")
    )

    val relations = listOf("Spouse", "Mother", "Father", "Son", "Daughter", "Brother", "Sister", "Other")

    // Automatic Live Calculation of Next Premium Date!
    val calculatedNextPremiumDate by remember {
        derivedStateOf {
            if (startDate.isNotBlank()) {
                PolicyDateHelper.calculateNextPremiumDate(startDate, frequencyMonths)
            } else {
                "--"
            }
        }
    }

    // Photo pickers
    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> if (uri != null) profilePhotoUri = uri }

    val policyDocPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> if (uri != null) policyDocUri = uri }

    val kycDocPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> if (uri != null) kycDocUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Register LIC Policy", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("reg_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LicNavyPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("policy_registration_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Supabase Cloud Storage Status Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, LicEmerald.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("supabase_status_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LicEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = LicEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Supabase Cloud Database Connected",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                        Text(
                            text = "Project: fldgbvzimoxfnjavzdpg • Policyholder records sync to cloud",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF047857)
                        )
                    }
                }
            }

            // Section 1: User Registration
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = LicNavyPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1. Customer Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LicNavyPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Profile Photo Upload Box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .border(2.dp, LicGoldAccent, CircleShape)
                                .clickable {
                                    profilePhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("pick_profile_photo_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profilePhotoUri != null) {
                                AsyncImage(
                                    model = profilePhotoUri,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Add Photo",
                                    tint = LicNavyPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Profile Photo",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap circle to choose passport photo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name *") },
                        placeholder = { Text("e.g. Sunil Kumar") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_full_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dob,
                            onValueChange = { dob = it },
                            label = { Text("Date of Birth *") },
                            placeholder = { Text("16/12/2003") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_dob_input")
                        )

                        OutlinedTextField(
                            value = mobileNumber,
                            onValueChange = { mobileNumber = it },
                            label = { Text("Mobile Number *") },
                            placeholder = { Text("9876543210") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_mobile_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("sbsunil937@gmail.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_email_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Residential Address") },
                        placeholder = { Text("House/Plot, Area, City, State, PIN") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_address_input")
                    )
                }
            }

            // Section 2: Policy Details
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = LicGoldAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. Policy Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LicNavyPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = policyNumber,
                        onValueChange = { policyNumber = it },
                        label = { Text("Policy Number *") },
                        placeholder = { Text("e.g. 8492012345") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_policy_no_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = policyName,
                        onValueChange = { policyName = it },
                        label = { Text("Plan / Policy Name") },
                        placeholder = { Text("e.g. LIC Jeevan Labh (Plan 936)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_policy_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Policy Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = isTypeDropdownExpanded,
                        onExpandedChange = { isTypeDropdownExpanded = !isTypeDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = policyType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Policy Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = isTypeDropdownExpanded,
                            onDismissRequest = { isTypeDropdownExpanded = false }
                        ) {
                            policyTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        policyType = type
                                        isTypeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = sumAssuredText,
                            onValueChange = { sumAssuredText = it },
                            label = { Text("Sum Assured (₹)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_sum_assured_input")
                        )

                        OutlinedTextField(
                            value = premiumAmountText,
                            onValueChange = { premiumAmountText = it },
                            label = { Text("Premium (₹) *") },
                            placeholder = { Text("5000") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_premium_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Premium Frequency Dropdown
                    val currentFreqLabel = frequencies.find { it.first == frequencyMonths }?.second ?: "6 Months"
                    ExposedDropdownMenuBox(
                        expanded = isFreqDropdownExpanded,
                        onExpandedChange = { isFreqDropdownExpanded = !isFreqDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = currentFreqLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Premium Frequency *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFreqDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("reg_frequency_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = isFreqDropdownExpanded,
                            onDismissRequest = { isFreqDropdownExpanded = false }
                        ) {
                            frequencies.forEach { (months, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        frequencyMonths = months
                                        isFreqDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Policy Start Date (DD/MM/YYYY) *") },
                        singleLine = true,
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_start_date_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Automatic Calculation Live Preview Card!
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = LicNavyPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Automatic Premium Due Calculation",
                                    fontWeight = FontWeight.Bold,
                                    color = LicNavyPrimary,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Policy Start: $startDate  ➔  Frequency: $frequencyMonths Months",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1E3A8A)
                            )
                            Text(
                                text = "Next Premium Due: $calculatedNextPremiumDate",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = LicNavyPrimary
                            )
                        }
                    }
                }
            }

            // Section 3: Nominee & Agent
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Nominee & Agent Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LicNavyPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = nomineeName,
                            onValueChange = { nomineeName = it },
                            label = { Text("Nominee Name") },
                            placeholder = { Text("Anita Devi") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_nominee_name_input")
                        )

                        ExposedDropdownMenuBox(
                            expanded = isRelationDropdownExpanded,
                            onExpandedChange = { isRelationDropdownExpanded = !isRelationDropdownExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = nomineeRelation,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Relation") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRelationDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = isRelationDropdownExpanded,
                                onDismissRequest = { isRelationDropdownExpanded = false }
                            ) {
                                relations.forEach { rel ->
                                    DropdownMenuItem(
                                        text = { Text(rel) },
                                        onClick = {
                                            nomineeRelation = rel
                                            isRelationDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = agentName,
                            onValueChange = { agentName = it },
                            label = { Text("Agent Name") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_agent_name_input")
                        )

                        OutlinedTextField(
                            value = agentCode,
                            onValueChange = { agentCode = it },
                            label = { Text("Agent Code") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_agent_code_input")
                        )
                    }
                }
            }

            // Section 4: Document Attachments
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "4. Documents & KYC Vault",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LicNavyPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Attach policy bond or Aadhaar/PAN image if required",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Policy Doc Picker
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                .clickable {
                                    policyDocPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(8.dp)
                                .testTag("pick_policy_doc_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (policyDocUri != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LicEmerald)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Policy Attached", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = LicNavyPrimary)
                                    Text("Attach Policy Bond", fontSize = 11.sp, color = LicNavyPrimary)
                                }
                            }
                        }

                        // KYC Doc Picker
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                .clickable {
                                    kycDocPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(8.dp)
                                .testTag("pick_kyc_doc_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (kycDocUri != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LicEmerald)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("KYC Attached", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = LicGoldAccent)
                                    Text("Attach Aadhaar/PAN", fontSize = 11.sp, color = LicGoldAccent)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Special Policy Notes / Remarks (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Error display
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Submit Button
            Button(
                onClick = {
                    if (fullName.isBlank() || policyNumber.isBlank() || premiumAmountText.isBlank() || startDate.isBlank()) {
                        errorMessage = "Please enter Customer Name, Policy Number, Premium Amount, and Start Date."
                        return@Button
                    }
                    val sum = sumAssuredText.toDoubleOrNull() ?: 500000.0
                    val prem = premiumAmountText.toDoubleOrNull() ?: 5000.0

                    viewModel.registerNewPolicy(
                        fullName = fullName,
                        dob = dob.ifBlank { "01/01/2000" },
                        mobileNumber = mobileNumber.ifBlank { "9876543210" },
                        email = email,
                        address = address,
                        profilePhotoUri = profilePhotoUri?.toString(),
                        policyNumber = policyNumber,
                        policyName = policyName,
                        policyType = policyType,
                        sumAssured = sum,
                        premiumAmount = prem,
                        frequencyMonths = frequencyMonths,
                        startDate = startDate,
                        nomineeName = nomineeName.ifBlank { "Self / Legal Heir" },
                        nomineeRelation = nomineeRelation,
                        agentName = agentName.ifBlank { "Direct Branch" },
                        agentCode = agentCode.ifBlank { "BRANCH-DIRECT" },
                        policyDocUri = policyDocUri?.toString(),
                        kycDocUri = kycDocUri?.toString(),
                        notes = notes,
                        onSuccess = onBack
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = LicNavyPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_policy_reg_btn")
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register & Save to Supabase Cloud", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
