package com.example.data.sms

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object SmsGatewayManager {

    data class GatewayConfig(
        val providerName: String = "Greenweb BD (Default)",
        val apiKeyMasked: String = "gw_live_••••••••38f",
        val senderId: String = "BAKIKHATA",
        val isEnabled: Boolean = true
    )

    var currentConfig = GatewayConfig()

    /**
     * Builds Bengali SMS text for a customer reminder or transaction.
     */
    fun buildDueSms(
        shopName: String,
        customerName: String,
        amountFormatted: String,
        totalDueFormatted: String
    ): String {
        return "$shopName: প্রিয় $customerName, আপনার নতুন বাকি $amountFormatted যোগ হয়েছে। বর্তমান মোট বাকি $totalDueFormatted। ধন্যবাদ।"
    }

    fun buildPaymentSms(
        shopName: String,
        customerName: String,
        amountFormatted: String,
        remainingDueFormatted: String
    ): String {
        return "$shopName: প্রিয় $customerName, আপনার $amountFormatted টাকা জমা গৃহীত হয়েছে। অবশিষ্ট বাকি $remainingDueFormatted। ধন্যবাদ।"
    }

    fun buildReminderSms(
        shopName: String,
        customerName: String,
        totalDueFormatted: String,
        shopPhone: String
    ): String {
        return "$shopName: প্রিয় $customerName, বিনীত অনুরোধ যে আপনার পূর্বের বাকি $totalDueFormatted টাকা অতিসত্বর পরিশোধ করার ব্যবস্থা করবেন। যোগাযোগ: $shopPhone"
    }

    /**
     * Creates an Intent to send the Bengali reminder message directly via WhatsApp.
     */
    fun createWhatsAppReminderIntent(phone: String, message: String): Intent {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val bdPhone = when {
            cleanPhone.startsWith("880") -> cleanPhone
            cleanPhone.startsWith("0") -> "88$cleanPhone"
            else -> "880$cleanPhone"
        }
        val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$bdPhone&text=$encodedMessage")
        return Intent(Intent.ACTION_VIEW, uri)
    }

    /**
     * Creates an Intent to send native SMS via Android SMS app.
     */
    fun createNativeSmsIntent(phone: String, message: String): Intent {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phone")
            putExtra("sms_body", message)
        }
        return intent
    }
}
