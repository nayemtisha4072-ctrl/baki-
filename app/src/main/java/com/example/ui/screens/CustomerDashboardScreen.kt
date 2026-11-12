package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.admob.AdMobBannerView
import com.example.data.local.entity.NotificationEntity
import com.example.data.repository.BakiKhataRepository
import com.example.ui.components.TransactionRowItem
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

@Composable
fun CustomerDashboardScreen(
    viewModel: BakiKhataViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customer by viewModel.selectedCustomer.collectAsState()
    val shop by viewModel.currentShop.collectAsState()
    val transactions by viewModel.selectedCustomerTransactions.collectAsState()
    val notifications by viewModel.customerNotifications.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0=Home/Ledger, 1=Notifications, 2=Shops
    var showDisputeDialog by remember { mutableStateOf(false) }

    val hasDue = (customer?.totalDuePoisha ?: 0L) > 0L
    val lastTx = transactions.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("customer_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Customer Welcome Header
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = EmeraldGreenPrimary,
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "স্বাগতম, ${customer?.name ?: "গ্রাহক"}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "আপনার ব্যক্তিগত বাকি খাতা ও ডিজিটাল হিসাব স্টেটমেন্ট",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // 2. Navigation Tabs (হোম / খাতা, নোটিফিকেশন, আমার দোকান)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("খাতা ও লেনদেন", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        viewModel.markNotificationsRead()
                    },
                    text = {
                        Text(
                            text = "নোটিফিকেশন (${notifications.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // 3. Connected Shop Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(EmeraldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Store, contentDescription = null, tint = EmeraldGreenDark)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = shop?.name ?: "মায়ের দোয়া জেনারেল স্টোর",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = EmeraldGreenPrimary, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "${shop?.address ?: "কাঁচাবাজার রোড"}, ${shop?.upazila ?: "মিরপুর"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${shop?.phone ?: "01711002233"}")
                            }
                            context.startActivity(intent)
                        }) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call Shop", tint = EmeraldGreenPrimary)
                        }
                    }
                }
            }

            // 4. Financial Balance Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (hasDue) DueRedContainer else PaidGreenContainer,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, (if (hasDue) DueRed else PaidGreen).copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "দোকানে আপনার বর্তমান মোট বাকি:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (hasDue) DueRed else PaidGreen
                            )
                            Text(
                                text = if (hasDue) "বকেয়া" else "পরিশোধিত",
                                fontWeight = FontWeight.Bold,
                                color = if (hasDue) DueRed else PaidGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = BakiKhataRepository.formatCurrency(customer?.totalDuePoisha ?: 0L),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (hasDue) DueRed else PaidGreen
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "মোট নগদ পরিশোধিত: ${BakiKhataRepository.formatCurrency(customer?.totalPaidPoisha ?: 0L)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (lastTx != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            val lastDate = dateFormat.format(Date(lastTx.dateEpochMillis))
                            Text(
                                text = "সর্বশেষ লেনদেন: ${BakiKhataRepository.toBengaliDigits(lastDate)} (${lastTx.productName})",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // 5. Dispute / Report Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "আপনার সম্পূর্ণ হিসাব ইতিহাস",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = { showDisputeDialog = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("হিসাবে অমিল?", fontSize = 12.sp)
                    }
                }
            }

            // 6. Transactions
            if (transactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("কোনো লেনদেন রেকর্ড নেই।")
                    }
                }
            } else {
                items(transactions) { tx ->
                    TransactionRowItem(transaction = tx)
                }
            }

            // AdMob Banner
            item {
                Spacer(modifier = Modifier.height(10.dp))
                AdMobBannerView()
            }
        } else {
            // Notifications Tab
            if (notifications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("কোনো নতুন নোটিফিকেশন নেই।")
                    }
                }
            } else {
                items(notifications) { notif ->
                    CustomerNotificationCard(notification = notif)
                }
            }
        }
    }

    // Dispute / Contact Dialog
    if (showDisputeDialog) {
        AlertDialog(
            onDismissRequest = { showDisputeDialog = false },
            title = { Text("হিসাবে কোনো অমিল বা সমস্যা?") },
            text = {
                Text(
                    "গ্রাহক হিসাবে কোনো লেনদেন পরিবর্তন করতে পারেন না। হিসাবের নির্ভরযোগ্যতা রক্ষার্থে যেকোনো অমিল বা গরমিলের জন্য সরাসরি দোকানদার (${shop?.ownerName ?: "রফিকুল ইসলাম"}, ফোন: ${shop?.phone ?: "01711002233"}) এর সাথে যোগাযোগ করুন।"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${shop?.phone ?: "01711002233"}")
                        }
                        context.startActivity(intent)
                        showDisputeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
                ) {
                    Text("দোকানদারকে কল করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisputeDialog = false }) {
                    Text("বুঝেছি")
                }
            }
        )
    }
}

@Composable
fun CustomerNotificationCard(notification: NotificationEntity) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val formatted = dateFormat.format(Date(notification.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else EmeraldContainer.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = EmeraldGreenPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.messageBengali,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = BakiKhataRepository.toBengaliDigits(formatted),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
