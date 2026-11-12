package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.entity.SmsLogEntity
import com.example.data.repository.BakiKhataRepository
import com.example.ui.components.FinancialStatCard
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: BakiKhataViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customers by viewModel.filteredCustomers.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val smsLogs by viewModel.smsLogs.collectAsState()

    var timeFilterIndex by remember { mutableIntStateOf(2) } // 0=Today, 1=Week, 2=Month, 3=All
    var activeTab by remember { mutableIntStateOf(0) } // 0=Overview & Top Due, 1=SMS Logs

    val now = System.currentTimeMillis()
    val cal = Calendar.getInstance()

    val filterStartTime = remember(timeFilterIndex) {
        when (timeFilterIndex) {
            0 -> { // Today
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            1 -> now - (7 * 86_400_000L) // Last 7 days
            2 -> { // This Month
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.timeInMillis
            }
            else -> 0L // All time
        }
    }

    val periodTransactions = allTransactions.filter { it.dateEpochMillis >= filterStartTime }
    val periodDuePoisha = periodTransactions.filter { it.type == "DUE" }.sumOf { it.amountPoisha }
    val periodPaymentPoisha = periodTransactions.filter { it.type == "PAYMENT" }.sumOf { it.amountPoisha }
    val totalOutstandingDuePoisha = customers.sumOf { it.totalDuePoisha }

    val topDueCustomers = customers.filter { it.totalDuePoisha > 0 }.sortedByDescending { it.totalDuePoisha }.take(5)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Time Period Selector Filter Chips
        item {
            Column {
                Text(
                    text = "রিপোর্ট ও বিশ্লেষণ (Reports & Analytics)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val periods = listOf("আজ", "গত ৭ দিন", "চলতি মাস", "সার্বিক")
                    periods.forEachIndexed { index, title ->
                        FilterChip(
                            selected = timeFilterIndex == index,
                            onClick = { timeFilterIndex = index },
                            label = { Text(title, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // 2. Overview Stats (Due vs Paid)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FinancialStatCard(
                    title = "মোট বাকি যোগ",
                    amountPoisha = periodDuePoisha,
                    icon = Icons.Default.TrendingDown,
                    containerColor = DueRedContainer,
                    contentColor = DueRed,
                    modifier = Modifier.weight(1f)
                )
                FinancialStatCard(
                    title = "মোট নগদ আদায়",
                    amountPoisha = periodPaymentPoisha,
                    icon = Icons.Default.TrendingUp,
                    containerColor = PaidGreenContainer,
                    contentColor = PaidGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Tab Bar (হিসাব বিবরণী vs এসএমএস লগ)
        item {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("শীর্ষ বাকিদার ও চার্ট", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("এসএমএস ডেলিভারি লগ", fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (activeTab == 0) {
            // 4. Top Debtors List with progress indicators
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "শীর্ষ বাকিদার গ্রাহক (Top Debtors)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (topDueCustomers.isEmpty()) {
                            Text(
                                text = "কোনো বাকিদার নেই। সকল বাকি পরিশোধিত!",
                                color = PaidGreen,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            topDueCustomers.forEach { cust ->
                                val fraction = if (totalOutstandingDuePoisha > 0) {
                                    (cust.totalDuePoisha.toFloat() / totalOutstandingDuePoisha.toFloat()).coerceIn(0f, 1f)
                                } else 0f

                                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = cust.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = BakiKhataRepository.formatCurrency(cust.totalDuePoisha),
                                            fontWeight = FontWeight.Bold,
                                            color = DueRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { fraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = DueRed,
                                        trackColor = DueRedContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. AdMob Banner
            item {
                AdMobBannerView()
            }

            // 6. Selected Period Transactions
            item {
                Text(
                    text = "নির্বাচিত সময়ের লেনদেনসমূহ (${periodTransactions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (periodTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("এই সময়ে কোনো লেনদেন পাওয়া যায়নি।")
                    }
                }
            } else {
                items(periodTransactions) { tx ->
                    TransactionRowItem(transaction = tx)
                }
            }
        } else {
            // SMS Logs Tab
            item {
                Text(
                    text = "গ্রাহকদের পাঠানো এসএমএস হিস্ট্রি",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (smsLogs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("এখনো কোনো এসএমএস পাঠানো হয়নি।")
                    }
                }
            } else {
                items(smsLogs) { log ->
                    SmsLogRowItem(log = log)
                }
            }
        }
    }
}

@Composable
fun SmsLogRowItem(log: SmsLogEntity) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val formatted = dateFormat.format(Date(log.timestamp))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(EmeraldContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Sms, contentDescription = null, tint = EmeraldGreenDark, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = log.customerName, fontWeight = FontWeight.Bold)
                    Text(
                        text = "ডেলিভার্ড",
                        color = PaidGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "প্রাপক: ${BakiKhataRepository.toBengaliDigits(log.customerPhone)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = log.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = BakiKhataRepository.toBengaliDigits(formatted),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
