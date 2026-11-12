package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.dao.BakiKhataDao
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ShopEntity
import com.example.data.local.entity.SmsLogEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class BakiKhataRepository(private val dao: BakiKhataDao) {

    // --- SHOPS ---
    fun getAllShops(): Flow<List<ShopEntity>> = dao.getAllShops()

    suspend fun getShop(shopId: String): ShopEntity? = dao.getShopById(shopId)

    suspend fun saveShop(shop: ShopEntity) = dao.insertShop(shop)

    // --- CUSTOMERS ---
    fun getCustomers(shopId: String): Flow<List<CustomerEntity>> = dao.getCustomersByShop(shopId)

    fun getCustomersByHighestDue(shopId: String): Flow<List<CustomerEntity>> = dao.getCustomersByHighestDue(shopId)

    fun getCustomerFlow(customerId: String): Flow<CustomerEntity?> = dao.getCustomerFlow(customerId)

    suspend fun getCustomerById(customerId: String): CustomerEntity? = dao.getCustomerById(customerId)

    fun getCustomerShopsByPhone(phone: String): Flow<List<CustomerEntity>> = dao.getCustomerShopsByPhone(phone)

    suspend fun addCustomer(
        shopId: String,
        name: String,
        phone: String,
        address: String,
        initialDuePoisha: Long = 0L
    ): String {
        val customerId = "cust_" + UUID.randomUUID().toString().take(8)
        val customer = CustomerEntity(
            customerId = customerId,
            shopId = shopId,
            name = name.trim(),
            phone = phone.trim(),
            address = address.trim(),
            totalDuePoisha = initialDuePoisha,
            totalPaidPoisha = 0L,
            isVerified = true,
            status = "ACTIVE"
        )
        dao.insertCustomer(customer)

        if (initialDuePoisha > 0L) {
            val initialTx = TransactionEntity(
                transactionId = "tx_" + UUID.randomUUID().toString().take(8),
                shopId = shopId,
                customerId = customerId,
                type = "DUE",
                amountPoisha = initialDuePoisha,
                productName = "পূর্বের বাকি (Previous Due)",
                balanceAfterPoisha = initialDuePoisha,
                note = "খাতা খোলার সময় পূর্বের বাকি"
            )
            dao.insertTransaction(initialTx)
        }
        return customerId
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        dao.updateCustomer(customer.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun archiveCustomer(customerId: String) {
        dao.archiveCustomer(customerId)
    }

    suspend fun deleteCustomer(customerId: String) {
        dao.deleteTransactionsByCustomer(customerId)
        dao.deleteCustomer(customerId)
    }

    // --- TRANSACTIONS ---
    fun getTransactionsForCustomer(customerId: String): Flow<List<TransactionEntity>> =
        dao.getTransactionsByCustomer(customerId)

    fun getAllTransactions(shopId: String): Flow<List<TransactionEntity>> =
        dao.getAllTransactionsByShop(shopId)

    suspend fun addDueTransaction(
        shopId: String,
        customerId: String,
        amountPoisha: Long,
        productName: String,
        quantity: Double,
        unitPricePoisha: Long,
        note: String,
        dateEpochMillis: Long = System.currentTimeMillis(),
        sendSms: Boolean = true
    ): Boolean {
        if (amountPoisha <= 0L) return false

        val customer = dao.getCustomerById(customerId) ?: return false
        val shop = dao.getShopById(shopId)
        val shopName = shop?.name ?: "দোকান"

        val transactionId = "tx_" + UUID.randomUUID().toString().take(8)
        val transaction = TransactionEntity(
            transactionId = transactionId,
            shopId = shopId,
            customerId = customerId,
            type = "DUE",
            amountPoisha = amountPoisha,
            productName = productName.ifBlank { "পণ্য বাকি" },
            quantity = quantity,
            unitPricePoisha = unitPricePoisha,
            dateEpochMillis = dateEpochMillis,
            note = note
        )

        val expectedTotalDue = customer.totalDuePoisha + amountPoisha
        val formattedAmount = formatCurrency(amountPoisha)
        val formattedTotal = formatCurrency(expectedTotalDue)

        val notification = NotificationEntity(
            notificationId = "notif_" + UUID.randomUUID().toString().take(8),
            shopId = shopId,
            customerId = customerId,
            title = "নতুন বাকি যোগ হয়েছে",
            messageBengali = "প্রিয় ${customer.name}, $shopName থেকে আপনার নতুন $formattedAmount টাকা বাকি যোগ হয়েছে। আপনার মোট বাকি এখন $formattedTotal।",
            type = "DUE_ADDED",
            timestamp = System.currentTimeMillis()
        )

        val smsLog = if (sendSms && customer.phone.isNotBlank()) {
            SmsLogEntity(
                smsId = "sms_" + UUID.randomUUID().toString().take(8),
                shopId = shopId,
                customerPhone = customer.phone,
                customerName = customer.name,
                message = "$shopName: আপনার নতুন বাকি $formattedAmount টাকা যোগ হয়েছে। মোট বাকি $formattedTotal। ধন্যবাদ।",
                status = "SIMULATED",
                timestamp = System.currentTimeMillis()
            )
        } else null

        val resultDue = dao.recordDueTransaction(transaction, notification, smsLog)
        return resultDue >= 0L
    }

    suspend fun addPaymentTransaction(
        shopId: String,
        customerId: String,
        amountPoisha: Long,
        paymentMethod: String = "CASH",
        note: String = "",
        dateEpochMillis: Long = System.currentTimeMillis(),
        sendSms: Boolean = true
    ): Boolean {
        if (amountPoisha <= 0L) return false

        val customer = dao.getCustomerById(customerId) ?: return false
        val shop = dao.getShopById(shopId)
        val shopName = shop?.name ?: "দোকান"

        val transactionId = "tx_" + UUID.randomUUID().toString().take(8)
        val transaction = TransactionEntity(
            transactionId = transactionId,
            shopId = shopId,
            customerId = customerId,
            type = "PAYMENT",
            amountPoisha = amountPoisha,
            productName = "জমা ($paymentMethod)",
            dateEpochMillis = dateEpochMillis,
            note = note,
            paymentMethod = paymentMethod
        )

        val expectedRemainingDue = (customer.totalDuePoisha - amountPoisha).coerceAtLeast(0L)
        val formattedAmount = formatCurrency(amountPoisha)
        val formattedRemaining = formatCurrency(expectedRemainingDue)

        val notification = NotificationEntity(
            notificationId = "notif_" + UUID.randomUUID().toString().take(8),
            shopId = shopId,
            customerId = customerId,
            title = "টাকা জমা গৃহীত হয়েছে",
            messageBengali = "প্রিয় ${customer.name}, $shopName এ আপনার $formattedAmount টাকা জমা গৃহীত হয়েছে। বর্তমান বাকি $formattedRemaining। ধন্যবাদ।",
            type = "PAYMENT_RECEIVED",
            timestamp = System.currentTimeMillis()
        )

        val smsLog = if (sendSms && customer.phone.isNotBlank()) {
            SmsLogEntity(
                smsId = "sms_" + UUID.randomUUID().toString().take(8),
                shopId = shopId,
                customerPhone = customer.phone,
                customerName = customer.name,
                message = "$shopName: আপনার $formattedAmount টাকা জমা সম্পন্ন হয়েছে। বর্তমান বাকি $formattedRemaining টাকা। ধন্যবাদ।",
                status = "SIMULATED",
                timestamp = System.currentTimeMillis()
            )
        } else null

        val resultDue = dao.recordPaymentTransaction(transaction, notification, smsLog)
        return resultDue >= 0L
    }

    // --- NOTIFICATIONS ---
    fun getNotificationsForCustomer(customerId: String): Flow<List<NotificationEntity>> =
        dao.getNotificationsForCustomer(customerId)

    suspend fun markNotificationsRead(customerId: String) =
        dao.markAllCustomerNotificationsAsRead(customerId)

    // --- SMS LOGS ---
    fun getSmsLogs(shopId: String): Flow<List<SmsLogEntity>> =
        dao.getSmsLogs(shopId)

    // --- DATA SAFETY & ACCOUNT DELETION ---
    suspend fun purgeAllUserData() {
        dao.deleteAllTransactions()
        dao.deleteAllNotifications()
        dao.deleteAllSmsLogs()
        dao.deleteAllCustomers()
    }

    companion object {
        // Bengali Number digits converter
        fun toBengaliDigits(input: String): String {
            val englishToBengaliMap = mapOf(
                '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
                '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯',
                ',' to ',', '.' to '.'
            )
            return input.map { englishToBengaliMap[it] ?: it }.joinToString("")
        }

        // Formats poisha into ৳ ১,৫০০ or ৳ ১,৫০০.৫০
        fun formatCurrency(poisha: Long, showPoishaIfZero: Boolean = false): String {
            val taka = poisha / 100.0
            val formatter = NumberFormat.getNumberInstance(Locale.US)
            val formatted = if (poisha % 100 == 0L && !showPoishaIfZero) {
                formatter.format(poisha / 100)
            } else {
                String.format(Locale.US, "%.2f", taka)
            }
            return "৳" + toBengaliDigits(formatted)
        }
    }
}
