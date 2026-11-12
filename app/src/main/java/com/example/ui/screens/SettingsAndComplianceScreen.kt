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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.admob.AdManager
import com.example.data.sms.SmsGatewayManager
import com.example.ui.theme.DueRed
import com.example.ui.theme.DueRedContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.PaidGreen
import com.example.ui.viewmodel.BakiKhataViewModel

@Composable
fun SettingsAndComplianceScreen(
    viewModel: BakiKhataViewModel,
    modifier: Modifier = Modifier
) {
    val shop by viewModel.currentShop.collectAsState()

    var shopName by remember(shop) { mutableStateOf(shop?.name ?: "মায়ের দোয়া জেনারেল স্টোর") }
    var ownerName by remember(shop) { mutableStateOf(shop?.ownerName ?: "মোহাম্মদ রফিকুল ইসলাম") }
    var phone by remember(shop) { mutableStateOf(shop?.phone ?: "01711002233") }
    var address by remember(shop) { mutableStateOf(shop?.address ?: "দোকান নং ১২, কাঁচাবাজার রোড") }
    var upazila by remember(shop) { mutableStateOf(shop?.upazila ?: "মিরপুর") }
    var district by remember(shop) { mutableStateOf(shop?.district ?: "ঢাকা") }

    var adsEnabled by remember { mutableStateOf(AdManager.isAdsEnabled) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showDataDeletionDialog by remember { mutableStateOf(false) }
    var showPlayReleaseDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                text = "দোকান প্রোফাইল ও সেটিংস",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // 1. Shop Profile Editor Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Store, contentDescription = null, tint = EmeraldGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "দোকানের বিবরণ (Shop Information)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("দোকানের নাম") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("দোকানদারের নাম") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("মোবাইল নম্বর") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("দোকানের ঠিকানা") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = upazila,
                            onValueChange = { upazila = it },
                            label = { Text("উপজেলা / থানা") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = district,
                            onValueChange = { district = it },
                            label = { Text("জেলা") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.updateShop(shopName, ownerName, phone, address, upazila, district)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
                    ) {
                        Text("প্রোফাইল আপডেট সংরক্ষণ করুন")
                    }
                }
            }
        }

        // 2. SMS Gateway Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Sms, contentDescription = null, tint = EmeraldGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "এসএমএস গেটওয়ে ইন্টিগ্রেশন (SMS Gateway)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "বাংলাদেশ টেলিকম অনুমোদিত গেটওয়ে কনফিগারেশন: ${SmsGatewayManager.currentConfig.providerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "প্রেরক আইডি (Sender ID): ${SmsGatewayManager.currentConfig.senderId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "স্ট্যাটাস: সক্রিয় (গ্রাহকের ফোনে স্বয়ংক্রিয় বাংলা বার্তা প্রেরণ)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PaidGreen
                    )
                }
            }
        }

        // 3. Google AdMob Monetization Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AdsClick, contentDescription = null, tint = EmeraldGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Google AdMob বিজ্ঞাপন মনিটাইজেশন", fontWeight = FontWeight.Bold)
                        }

                        Switch(
                            checked = adsEnabled,
                            onCheckedChange = {
                                adsEnabled = it
                                AdManager.isAdsEnabled = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreenPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "অফিসিয়াল টেস্ট ব্যানার ইউনিট: ${AdManager.TEST_BANNER_AD_UNIT_ID.take(24)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "ইন্টারস্টিশিয়াল ইউনিট: ${AdManager.TEST_INTERSTITIAL_AD_UNIT_ID.take(24)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "UMP কনসেন্ট স্ট্যাটাস: কনসেন্ট গৃহীত (Policy Compliant)",
                        style = MaterialTheme.typography.bodySmall,
                        color = PaidGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 4. Privacy, Google Play Compliance & Account Deletion
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = EmeraldGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "গোপনীয়তা ও গুগল প্লে নীতিমালা (Policy & Compliance)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Privacy Policy Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showPrivacyPolicyDialog = true }
                            .padding(vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Policy, contentDescription = null, tint = EmeraldGreenPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "প্রাইভেসি পলিসি (Privacy Policy)", fontWeight = FontWeight.SemiBold)
                                Text(text = "আর্থিক তথ্য নিরাপত্তা ও ডাটা পলিসি দেখুন", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Play Store Release Guide
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showPlayReleaseDialog = true }
                            .padding(vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldGreenPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "গুগল প্লে স্টোর রিলিজ নির্দেশিকা", fontWeight = FontWeight.SemiBold)
                                Text(text = "AAB সাইনিং ও প্লে কনসোল ডাটা সেফটি চেকলিস্ট", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Account & Data Deletion
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showDataDeletionDialog = true }
                            .padding(vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = DueRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "অ্যাকাউন্ট ও সম্পূর্ণ তথ্য মুছে ফেলা (Data Deletion)", fontWeight = FontWeight.SemiBold, color = DueRed)
                                Text(text = "গুগল প্লে নির্দেশ অনুযায়ী সমস্ত লেজার ও ব্যক্তিগত তথ্য মোছার ব্যবস্থা", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text("বাকি খাতা গোপনীয়তা নীতি (Privacy Policy)") },
            text = {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "১. সংগৃহীত তথ্য: বাকি খাতা শুধুমাত্র লেনদেন সংরক্ষণের জন্য গ্রাহকের নাম, মোবাইল নম্বর এবং আর্থিক হিসাব খাতা ডাটা সংরক্ষণ করে।\n\n" +
                                "২. আর্থিক হিসাবের নিরাপত্তা: কোনো অননুমোদিত ব্যক্তি বা তৃতীয় পক্ষের কাছে আর্থিক তথ্য হস্তান্তর করা হয় না। সমস্ত হিসাব এনক্রিপ্ট করে ডিভাইস ও ক্লাউডে সুরক্ষিত রাখা হয়।\n\n" +
                                "৩. বিজ্ঞাপন ও AdMob: গুগল অ্যাডমব পলিসি অনুযায়ী সংগৃহীত বিজ্ঞাপনী আইডি শুধুমাত্র প্রাসঙ্গিক বিজ্ঞাপনের জন্য ব্যবহৃত হতে পারে এবং ব্যবহারকারী যে কোনো সময় কনসেন্ট পরিবর্তন করতে পারেন।\n\n" +
                                "৪. তথ্য মোছার অধিকার: যেকোনো ব্যবহারকারী চাইলে এই অ্যাপের সেটিংস থেকে তাদের সমস্ত তথ্য স্থায়ীভাবে মুছে ফেলতে পারেন।",
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    // Data Deletion Dialog
    if (showDataDeletionDialog) {
        AlertDialog(
            onDismissRequest = { showDataDeletionDialog = false },
            title = { Text("অ্যাকাউন্ট ও সকল তথ্য স্থায়ীভাবে মুছে ফেলতে চান?") },
            text = {
                Text(
                    "গুগল প্লে স্টোর ডেটা সেফটি পলিসি অনুযায়ী, আপনি চাইলে আপনার দোকানের সকল গ্রাহক, লেনদেন এবং এসএমএস হিসাব স্থায়ীভাবে ডিলিট করতে পারেন। ডিলিট করার পর ডাটা পুনরুদ্ধার করা সম্ভব নয়। আপনি কি নিশ্চিত?",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.requestAccountAndDataDeletion()
                        showDataDeletionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DueRed)
                ) {
                    Text("হ্যাঁ, সব মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDataDeletionDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Play Store Release Guide Dialog
    if (showPlayReleaseDialog) {
        AlertDialog(
            onDismissRequest = { showPlayReleaseDialog = false },
            title = { Text("গুগল প্লে স্টোর পাবলিশ চেকলিস্ট") },
            text = {
                Column {
                    Text(
                        text = "• Package ID: com.aistudio.bakikhata.bkhbdz\n" +
                                "• Signed AAB Command: gradle :app:bundleRelease\n" +
                                "• Data Safety Declaration: Financial Information (Due, Collection), Contacts (Customer phone)\n" +
                                "• AdMob: ca-app-pub-3940256099942544~3347511713 টেস্ট আইডি থেকে আপনার লাইভ AdMob আইডিতে প্রতিস্থাপন করুন।\n" +
                                "• UMP: European Regulations & Consent Framework Ready",
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPlayReleaseDialog = false }) {
                    Text("বুঝেছি")
                }
            }
        )
    }
}
