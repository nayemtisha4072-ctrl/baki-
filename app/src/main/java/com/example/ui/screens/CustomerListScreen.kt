package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.repository.BakiKhataRepository
import com.example.ui.components.CustomerCardItem
import com.example.ui.theme.DueRed
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.viewmodel.BakiKhataViewModel
import com.example.ui.viewmodel.CustomerFilter
import com.example.ui.viewmodel.CustomerSort
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    viewModel: BakiKhataViewModel,
    onCustomerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val customers by viewModel.filteredCustomers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val currentSort by viewModel.sort.collectAsState()

    var showAddCustomerSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("customer_list_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_search_input"),
                        placeholder = { Text("নাম অথবা মোবাইল নম্বর দিয়ে খুঁজুন...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Filter & Sort Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = currentFilter == CustomerFilter.ALL,
                            onClick = { viewModel.setFilter(CustomerFilter.ALL) },
                            label = { Text("সকল (${customers.size})", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldContainer,
                                selectedLabelColor = EmeraldGreenDark
                            )
                        )
                        FilterChip(
                            selected = currentFilter == CustomerFilter.WITH_DUE,
                            onClick = { viewModel.setFilter(CustomerFilter.WITH_DUE) },
                            label = { Text("বাকি আছে", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DueRed.copy(alpha = 0.15f),
                                selectedLabelColor = DueRed
                            )
                        )
                        FilterChip(
                            selected = currentFilter == CustomerFilter.SETTLED,
                            onClick = { viewModel.setFilter(CustomerFilter.SETTLED) },
                            label = { Text("পরিশোধিত", fontSize = 12.sp) }
                        )
                    }

                    // Sort Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "ক্রম:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "সর্বোচ্চ বাকি",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (currentSort == CustomerSort.HIGHEST_DUE) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentSort == CustomerSort.HIGHEST_DUE) EmeraldGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { viewModel.setSort(CustomerSort.HIGHEST_DUE) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Text(
                            text = "সাম্প্রতিক",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (currentSort == CustomerSort.RECENT_TRANSACTION) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentSort == CustomerSort.RECENT_TRANSACTION) EmeraldGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { viewModel.setSort(CustomerSort.RECENT_TRANSACTION) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Text(
                            text = "নাম",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (currentSort == CustomerSort.NAME) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentSort == CustomerSort.NAME) EmeraldGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { viewModel.setSort(CustomerSort.NAME) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 3. Customer List
            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(EmeraldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = EmeraldGreenPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "কোনো মিল পাওয়া যায়নি" else "কোনো গ্রাহকের খাতা তৈরি করা হয়নি",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "নিচের বাটনে চাপ দিয়ে নতুন গ্রাহক যুক্ত করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(customers, key = { it.customerId }) { customer ->
                        CustomerCardItem(
                            customer = customer,
                            onClick = {
                                viewModel.selectCustomer(customer.customerId)
                                onCustomerClick(customer.customerId)
                            }
                        )
                    }
                }
            }
        }

        // 4. Floating Action Button: Add Customer
        FloatingActionButton(
            onClick = { showAddCustomerSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_customer"),
            containerColor = EmeraldGreenPrimary,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Customer")
                Spacer(modifier = Modifier.width(8.dp))
                Text("নতুন গ্রাহক", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add Customer Bottom Sheet
    if (showAddCustomerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddCustomerSheet = false },
            sheetState = sheetState
        ) {
            AddCustomerBottomSheetContent(
                onSave = { name, phone, address, initialDue ->
                    viewModel.createCustomer(name, phone, address, initialDue)
                    coroutineScope.launch {
                        sheetState.hide()
                        showAddCustomerSheet = false
                    }
                },
                onCancel = {
                    coroutineScope.launch {
                        sheetState.hide()
                        showAddCustomerSheet = false
                    }
                }
            )
        }
    }
}

@Composable
fun AddCustomerBottomSheetContent(
    onSave: (name: String, phone: String, address: String, initialDuePoisha: Long) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var initialDueText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .padding(bottom = 30.dp)
    ) {
        Text(
            text = "নতুন গ্রাহক যুক্ত করুন",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "গ্রাহকের নাম ও মোবাইল নম্বর দিয়ে খাতা খুলুন",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("গ্রাহকের পুরো নাম *") },
            placeholder = { Text("যেমন: মোহাম্মদ রহিম") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("new_customer_name_input"),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("মোবাইল নম্বর *") },
            placeholder = { Text("০১৮xxxxxxxx") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("new_customer_phone_input"),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("ঠিকানা (ঐচ্ছিক)") },
            placeholder = { Text("গ্রাম/রোড, থানা, জেলা") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = initialDueText,
            onValueChange = { initialDueText = it },
            label = { Text("পূর্বের কোনো বাকি থাকলে (টাকা)") },
            placeholder = { Text("০") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("new_customer_initial_due_input"),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text("বাতিল")
            }

            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "দয়া করে গ্রাহকের নাম লিখুন"
                        return@Button
                    }
                    if (phone.isBlank()) {
                        errorMessage = "দয়া করে সঠিক মোবাইল নম্বর লিখুন"
                        return@Button
                    }
                    val cleanDueTaka = initialDueText.replace(",", "").toDoubleOrNull() ?: 0.0
                    val duePoisha = (cleanDueTaka * 100).toLong()
                    onSave(name, phone, address, duePoisha)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("submit_new_customer_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
            ) {
                Text("খাতা তৈরি করুন", fontWeight = FontWeight.Bold)
            }
        }
    }
}
