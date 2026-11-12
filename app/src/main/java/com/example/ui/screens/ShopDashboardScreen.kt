package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.admob.AdMobBannerView
import com.example.data.repository.BakiKhataRepository
import com.example.ui.components.FinancialStatCard
import com.example.ui.components.TransactionRowItem
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.DueRed
import com.example.ui.theme.DueRedContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.viewmodel.BakiKhataViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ShopDashboardScreen(
    viewModel: BakiKhataViewModel,
    onNavigateToAddDue: () -> Unit,
    onNavigateToReceivePayment: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToReports: () -> Unit,
    onSelectCustomer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shop by viewModel.currentShop.collectAsState()
    val customers by viewModel.filteredCustomers.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()

    // Financial Metrics Calculation
    val totalDuePoisha = customers.sumOf { it.totalDuePoisha }
    val totalCustomersCount = customers.size

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfToday = calendar.timeInMillis

    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val startOfMonth = calendar.timeInMillis

    val todayTxs = allTransactions.filter { it.dateEpochMillis >= startOfToday }
    val monthTxs = allTransactions.filter { it.dateEpochMillis >= startOfMonth }

    val todayDuePoisha = todayTxs.filter { it.type == "DUE" }.sumOf { it.amountPoisha }
    val todayCollectionPoisha = todayTxs.filter { it.type == "PAYMENT" }.sumOf { it.amountPoisha }

    val monthlySalesPoisha = monthTxs.filter { it.type == "DUE" }.sumOf { it.amountPoisha }
    val monthlyCollectionPoisha = monthTxs.filter { it.type == "PAYMENT" }.sumOf { it.amountPoisha }

    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("bn", "BD"))
    val todayFormatted = dateFormat.format(Date())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("shop_dashboard_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Shop Banner & Date
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = EmeraldGreenPrimary
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = shop?.name ?: "মায়ের দোয়া জেনারেল স্টোর",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${shop?.address ?: "কাঁচাবাজার রোড"}, ${shop?.upazila ?: "মিরপুর"}, ${shop?.district ?: "ঢাকা"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "আজ: ${BakiKhataRepository.toBengaliDigits(todayFormatted)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // 2. Primary Stat Grid (মোট বাকি, আজকের বাকি, আজকের জমা, মোট গ্রাহক)
        item {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinancialStatCard(
                        title = "মোট বাকি (বকেয়া)",
                        amountPoisha = totalDuePoisha,
                        icon = Icons.Default.TrendingDown,
                        containerColor = DueRedContainer,
                        contentColor = DueRed,
                        subtext = "সকল গ্রাহকের সর্বমোট",
                        modifier = Modifier.weight(1f)
                    )
                    FinancialStatCard(
                        title = "আজকের জমা (নগদ)",
                        amountPoisha = todayCollectionPoisha,
                        icon = Icons.Default.TrendingUp,
                        containerColor = PaidGreenContainer,
                        contentColor = PaidGreen,
                        subtext = "আজকের সংগৃহীত টাকা",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinancialStatCard(
                        title = "আজকের বাকি",
                        amountPoisha = todayDuePoisha,
                        icon = Icons.Default.Receipt,
                        containerColor = GoldContainer,
                        contentColor = DeepNavy,
                        subtext = "আজ নতুন দেওয়া বাকি",
                        modifier = Modifier.weight(1f)
                    )
                    FinancialStatCard(
                        title = "মোট গ্রাহক সংখ্যা",
                        amountPoisha = (totalCustomersCount * 100).toLong(), // formatted with digits
                        icon = Icons.Default.Group,
                        containerColor = EmeraldContainer,
                        contentColor = EmeraldGreenDark,
                        subtext = "খাতায় মোট রেজিস্টার্ড",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Monthly Summary Strip
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "চলতি মাসের মোট বিক্রি/বাকি",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = BakiKhataRepository.formatCurrency(monthlySalesPoisha),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "চলতি মাসের মোট আদায়",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = BakiKhataRepository.formatCurrency(monthlyCollectionPoisha),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PaidGreen
                            )
                        }
                    }
                }
            }
        }

        // 3. Quick Action Buttons
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "দ্রুত কার্যক্রম (Quick Actions)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToAddDue,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("action_add_due"),
                        colors = ButtonDefaults.buttonColors(containerColor = DueRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("বাকি যোগ", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Button(
                        onClick = onNavigateToReceivePayment,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("action_receive_payment"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("জমা নিন", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToCustomers,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("action_view_customers"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("নতুন গ্রাহক")
                    }

                    OutlinedButton(
                        onClick = onNavigateToReports,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("action_view_reports"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("রিপোর্ট ও হিসাব")
                    }
                }
            }
        }

        // 4. AdMob Banner (Policy Compliant non-intrusive placement)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            AdMobBannerView()
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 5. Recent Transactions Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সাম্প্রতিক লেনদেনসমূহ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "সকল খাতা দেখুন",
                    style = MaterialTheme.typography.labelMedium,
                    color = EmeraldGreenPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToCustomers() }
                )
            }
        }

        // 6. Recent Transaction Items
        if (allTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "এখনো কোনো লেনদেন রেকর্ড করা হয়নি।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(allTransactions.take(8)) { tx ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    TransactionRowItem(transaction = tx)
                }
            }
        }
    }
}
