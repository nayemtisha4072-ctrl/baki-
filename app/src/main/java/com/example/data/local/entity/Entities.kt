package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shops")
data class ShopEntity(
    @PrimaryKey val shopId: String,
    val name: String,
    val ownerName: String,
    val phone: String,
    val address: String,
    val upazila: String,
    val district: String,
    val smsSenderId: String = "BAKIKHATA",
    val smsEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val customerId: String,
    val shopId: String,
    val name: String,
    val phone: String,
    val address: String,
    val totalDuePoisha: Long = 0L,     // 100 poisha = 1 BDT (prevents float rounding errors)
    val totalPaidPoisha: Long = 0L,
    val isVerified: Boolean = true,
    val status: String = "ACTIVE",     // ACTIVE, ARCHIVED
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val transactionId: String,
    val shopId: String,
    val customerId: String,
    val type: String,                   // DUE (বাকি), PAYMENT (জমা), ADJUSTMENT
    val amountPoisha: Long,
    val productName: String = "",
    val quantity: Double = 1.0,
    val unitPricePoisha: Long = 0L,
    val balanceAfterPoisha: Long = 0L,
    val dateEpochMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    val paymentMethod: String = "CASH", // CASH, BKASH, NAGAD, ROCKET, BANK
    val syncStatus: String = "SYNCED",  // SYNCED, PENDING_SYNC
    val isReversed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val notificationId: String,
    val shopId: String,
    val customerId: String,
    val title: String,
    val messageBengali: String,
    val type: String = "DUE_ADDED",    // DUE_ADDED, PAYMENT_RECEIVED, REMINDER
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "sms_logs")
data class SmsLogEntity(
    @PrimaryKey val smsId: String,
    val shopId: String,
    val customerPhone: String,
    val customerName: String,
    val message: String,
    val status: String = "SIMULATED",   // SENT, SIMULATED, FAILED
    val timestamp: Long = System.currentTimeMillis()
)
