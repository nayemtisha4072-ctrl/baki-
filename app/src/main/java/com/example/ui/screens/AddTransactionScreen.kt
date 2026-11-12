package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CustomerEntity
import com.example.data.repository.BakiKhataRepository
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.DueRed
import com.example.ui.theme.DueRedContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.viewmodel.BakiKhataViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: BakiKhataViewModel,
    initialTab: Int = 0, // 0 = Due, 1 = Payment
    preselectedCustomerId: String? = null,
    onTransactionSaved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val customers by viewModel.filteredCustomers.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(initialTab) }

    var selectedCustomer by remember {
        mutableStateOf(
            if (preselectedCustomerId != null) {
                customers.find { it.customerId == preselectedCustomerId } ?: customers.firstOrNull()
            } else {
                customers.firstOrNull()
            }
        )
    }

    LaunchedEffect(customers) {
        if (selectedCustomer == null && customers.isNotEmpty()) {
            selectedCustomer = if (preselectedCustomerId != null) {
                customers.find { it.customerId == preselectedCustomerId } ?: customers.first()
            } else {
                customers.first()
            }
        }
    }

    var customerDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("add_transaction_screen")
    ) {
        // Tab Row: বাকি যোগ vs জমা গ্রহণ
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = if (selectedTabIndex == 0) DueRed else EmeraldGreenPrimary,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = if (selectedTabIndex == 0) DueRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "বাকি যোগ (Credit Due)",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == 0) DueRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = if (selectedTabIndex == 1) EmeraldGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "জমা নিন (Receive Payment)",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == 1) EmeraldGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Customer Selector Card
            Text(
                text = "গ্রাহক নির্বাচন করুন",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { customerDropdownExpanded = true }
                        .testTag("select_customer_dropdown"),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmeraldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = EmeraldGreenDark
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedCustomer?.name ?: "গ্রাহক নির্বাচন করুন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (selectedCustomer != null) {
                                Text(
                                    text = "মোবাইল: ${BakiKhataRepository.toBengaliDigits(selectedCustomer!!.phone)} | বর্তমান বাকি: ${BakiKhataRepository.formatCurrency(selectedCustomer!!.totalDuePoisha)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selectedCustomer!!.totalDuePoisha > 0) DueRed else PaidGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown"
                        )
                    }
                }

                DropdownMenu(
                    expanded = customerDropdownExpanded,
                    onDismissRequest = { customerDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    customers.forEach { cust ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(text = cust.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${cust.phone} • বাকি: ${BakiKhataRepository.formatCurrency(cust.totalDuePoisha)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (cust.totalDuePoisha > 0) DueRed else PaidGreen
                                    )
                                }
                            },
                            onClick = {
                                selectedCustomer = cust
                                customerDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTabIndex == 0) {
                // DUE TRANSACTION FORM
                DueTransactionForm(
                    selectedCustomer = selectedCustomer,
                    onSave = { amountPoisha, product, qty, unitPrice, note, sendSms ->
                        selectedCustomer?.let { cust ->
                            viewModel.addDueTransaction(
                                customerId = cust.customerId,
                                amountPoisha = amountPoisha,
                                productName = product,
                                quantity = qty,
                                unitPricePoisha = unitPrice,
                                note = note,
                                sendSms = sendSms
                            )
                            onTransactionSaved(cust.customerId)
                        }
                    }
                )
            } else {
                // PAYMENT TRANSACTION FORM
                PaymentTransactionForm(
                    selectedCustomer = selectedCustomer,
                    onSave = { amountPoisha, paymentMethod, note, sendSms ->
                        selectedCustomer?.let { cust ->
                            viewModel.addPaymentTransaction(
                                customerId = cust.customerId,
                                amountPoisha = amountPoisha,
                                paymentMethod = paymentMethod,
                                note = note,
                                sendSms = sendSms
                            )
                            onTransactionSaved(cust.customerId)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DueTransactionForm(
    selectedCustomer: CustomerEntity?,
    onSave: (amountPoisha: Long, product: String, qty: Double, unitPricePoisha: Long, note: String, sendSms: Boolean) -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var unitPriceText by remember { mutableStateOf("") }
    var manualTotalText by remember { mutableStateOf("") }
    var isManualTotalOverride by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var sendSms by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Automatic calculation: Quantity × Unit Price = Total Amount
    val calculatedTotalTaka = remember(quantityText, unitPriceText, isManualTotalOverride, manualTotalText) {
        if (isManualTotalOverride && manualTotalText.isNotBlank()) {
            manualTotalText.toDoubleOrNull() ?: 0.0
        } else {
            val q = quantityText.toDoubleOrNull() ?: 1.0
            val p = unitPriceText.toDoubleOrNull() ?: 0.0
            q * p
        }
    }

    val quickItems = listOf("চাল", "ডাল", "সয়াবিন তেল", "চিনি", "আটা", "ডিম", "সাবান", "চা পাতা")

    Column {
        Text(
            text = "পণ্যের বিবরণ",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("পণ্যের নাম / বিবরণ *") },
            placeholder = { Text("যেমন: নাজিরশাইল চাল ২ কেজি") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("due_product_name_input"),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Quick suggestions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickItems.take(4).forEach { item ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            productName = if (productName.isBlank()) item else "$productName, $item"
                        },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "+ $item",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quantity & Unit Price Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = quantityText,
                onValueChange = {
                    quantityText = it
                    isManualTotalOverride = false
                },
                label = { Text("পরিমাণ (সংখ্যা/কেজি)") },
                placeholder = { Text("1") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .weight(1f)
                    .testTag("due_quantity_input"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = unitPriceText,
                onValueChange = {
                    unitPriceText = it
                    isManualTotalOverride = false
                },
                label = { Text("একক দর (৳) *") },
                placeholder = { Text("যেমন: ৭০") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("due_unit_price_input"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Auto Calculated Total Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = DueRedContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = DueRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "মোট বাকি টাকা (স্বয়ংক্রিয় হিসাব):",
                            style = MaterialTheme.typography.bodySmall,
                            color = DueRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "পরিমাণ × একক দর = মোট",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = BakiKhataRepository.formatCurrency((calculatedTotalTaka * 100).toLong()),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DueRed
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Optional Notes
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("অতিরিক্ত মন্তব্য/নোট (ঐচ্ছিক)") },
            placeholder = { Text("যেমন: জরুরি বাকিতে নেওয়া") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // SMS Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { sendSms = !sendSms }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = sendSms,
                onCheckedChange = { sendSms = it },
                colors = CheckboxDefaults.colors(checkedColor = EmeraldGreenPrimary)
            )
            Icon(
                imageVector = Icons.Default.Sms,
                contentDescription = null,
                tint = EmeraldGreenPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "গ্রাহককে স্বয়ংক্রিয় এসএমএস ও নোটিফিকেশন পাঠান",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "নতুন বাকি যোগ হওয়ার সাথে সাথে বার্তা পৌঁছে যাবে",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = DueRed,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Button (Debounced / Double tap prevention)
        Button(
            onClick = {
                if (isSubmitting) return@Button
                if (selectedCustomer == null) {
                    errorMessage = "অনুগ্রহ করে একজন গ্রাহক নির্বাচন করুন।"
                    return@Button
                }
                if (productName.isBlank()) {
                    errorMessage = "অনুগ্রহ করে পণ্যের নাম অথবা বিবরণ লিখুন।"
                    return@Button
                }
                if (calculatedTotalTaka <= 0.0) {
                    errorMessage = "দয়া করে সঠিক দর বা টাকার পরিমাণ লিখুন।"
                    return@Button
                }

                isSubmitting = true
                val amountPoisha = (calculatedTotalTaka * 100).toLong()
                val qty = quantityText.toDoubleOrNull() ?: 1.0
                val unitPricePoisha = ((unitPriceText.toDoubleOrNull() ?: 0.0) * 100).toLong()

                onSave(amountPoisha, productName, qty, unitPricePoisha, note, sendSms)
            },
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("submit_due_transaction_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = DueRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSubmitting) "সংরক্ষণ হচ্ছে..." else "বাকি সংরক্ষণ করুন (SAVE)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun PaymentTransactionForm(
    selectedCustomer: CustomerEntity?,
    onSave: (amountPoisha: Long, paymentMethod: String, note: String, sendSms: Boolean) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("CASH") } // CASH, BKASH, NAGAD, ROCKET, BANK
    var note by remember { mutableStateOf("") }
    var sendSms by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentDuePoisha = selectedCustomer?.totalDuePoisha ?: 0L

    Column {
        // Current Due Banner
        if (selectedCustomer != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (currentDuePoisha > 0) DueRedContainer else PaidGreenContainer,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "বর্তমান বকেয়া বাকি:",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (currentDuePoisha > 0) DueRed else PaidGreen
                        )
                        Text(
                            text = BakiKhataRepository.formatCurrency(currentDuePoisha),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (currentDuePoisha > 0) DueRed else PaidGreen
                        )
                    }

                    if (currentDuePoisha > 0) {
                        Button(
                            onClick = {
                                val taka = currentDuePoisha / 100.0
                                amountText = if (currentDuePoisha % 100 == 0L) (currentDuePoisha / 100).toString() else String.format(Locale.US, "%.2f", taka)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("সম্পূর্ণ পরিশোধ", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Amount Field
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("জমা গ্রহণের পরিমাণ (টাকা) *") },
            placeholder = { Text("যেমন: ৫০০") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("payment_amount_input"),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Payment Method Chips (ক্যাশ, বিকাশ, নগদ, ব্যাংক)
        Text(
            text = "পরিশোধের মাধ্যম (Payment Method)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        val methods = listOf("CASH" to "ক্যাশ নগদ", "BKASH" to "বিকাশ (bKash)", "NAGAD" to "নগদ (Nagad)", "BANK" to "ব্যাংক")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            methods.forEach { (key, label) ->
                FilterChip(
                    selected = selectedMethod == key,
                    onClick = { selectedMethod = key },
                    label = { Text(label, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Notes
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("রিসিট নম্বর / নোট (ঐচ্ছিক)") },
            placeholder = { Text("যেমন: বিকাশ TrxID: 9X2A88") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // SMS Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { sendSms = !sendSms }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = sendSms,
                onCheckedChange = { sendSms = it },
                colors = CheckboxDefaults.colors(checkedColor = EmeraldGreenPrimary)
            )
            Icon(
                imageVector = Icons.Default.Sms,
                contentDescription = null,
                tint = EmeraldGreenPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "গ্রাহককে জমার ডিজিটাল রসিদ ও এসএমএস পাঠান",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = DueRed,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Payment Button
        Button(
            onClick = {
                if (isSubmitting) return@Button
                if (selectedCustomer == null) {
                    errorMessage = "অনুগ্রহ করে একজন গ্রাহক নির্বাচন করুন।"
                    return@Button
                }
                val enteredTaka = amountText.replace(",", "").toDoubleOrNull() ?: 0.0
                if (enteredTaka <= 0.0) {
                    errorMessage = "অনুগ্রহ করে সঠিক জমার পরিমাণ লিখুন।"
                    return@Button
                }

                isSubmitting = true
                val amountPoisha = (enteredTaka * 100).toLong()
                onSave(amountPoisha, selectedMethod, note, sendSms)
            },
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("submit_payment_transaction_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSubmitting) "সংরক্ষণ হচ্ছে..." else "টাকা জমা সম্পন্ন করুন (RECEIVE)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
