package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ShopEntity
import com.example.data.local.entity.SmsLogEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BakiKhataDao {

    // --- SHOPS ---
    @Query("SELECT * FROM shops")
    fun getAllShops(): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops WHERE shopId = :shopId LIMIT 1")
    suspend fun getShopById(shopId: String): ShopEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: ShopEntity)

    @Update
    suspend fun updateShop(shop: ShopEntity)

    // --- CUSTOMERS ---
    @Query("SELECT * FROM customers WHERE shopId = :shopId AND status = 'ACTIVE' ORDER BY updatedAt DESC")
    fun getCustomersByShop(shopId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE shopId = :shopId AND status = 'ACTIVE' ORDER BY totalDuePoisha DESC")
    fun getCustomersByHighestDue(shopId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE customerId = :customerId LIMIT 1")
    suspend fun getCustomerById(customerId: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE customerId = :customerId LIMIT 1")
    fun getCustomerFlow(customerId: String): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE phone = :phone LIMIT 1")
    suspend fun getCustomerByPhone(phone: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE phone = :phone AND status = 'ACTIVE'")
    fun getCustomerShopsByPhone(phone: String): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET status = 'ARCHIVED' WHERE customerId = :customerId")
    suspend fun archiveCustomer(customerId: String)

    @Query("DELETE FROM customers WHERE customerId = :customerId")
    suspend fun deleteCustomer(customerId: String)

    // --- TRANSACTIONS ---
    @Query("SELECT * FROM transactions WHERE shopId = :shopId ORDER BY dateEpochMillis DESC")
    fun getAllTransactionsByShop(shopId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY dateEpochMillis DESC")
    fun getTransactionsByCustomer(customerId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE shopId = :shopId AND dateEpochMillis >= :startOfDay AND dateEpochMillis <= :endOfDay ORDER BY dateEpochMillis DESC")
    fun getTransactionsForDate(shopId: String, startOfDay: Long, endOfDay: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE shopId = :shopId AND dateEpochMillis >= :startOfMonth ORDER BY dateEpochMillis DESC")
    fun getTransactionsForMonth(shopId: String, startOfMonth: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE customerId = :customerId")
    suspend fun deleteTransactionsByCustomer(customerId: String)

    // --- ATOMIC FINANCIAL METHODS ---
    @Transaction
    suspend fun recordDueTransaction(
        transaction: TransactionEntity,
        notification: NotificationEntity? = null,
        smsLog: SmsLogEntity? = null
    ): Long {
        val customer = getCustomerById(transaction.customerId) ?: return -1L
        val newDue = customer.totalDuePoisha + transaction.amountPoisha
        val updatedCustomer = customer.copy(
            totalDuePoisha = newDue,
            updatedAt = System.currentTimeMillis()
        )
        val finalTransaction = transaction.copy(balanceAfterPoisha = newDue)
        
        insertTransaction(finalTransaction)
        updateCustomer(updatedCustomer)
        
        notification?.let { insertNotification(it) }
        smsLog?.let { insertSmsLog(it) }
        
        return newDue
    }

    @Transaction
    suspend fun recordPaymentTransaction(
        transaction: TransactionEntity,
        notification: NotificationEntity? = null,
        smsLog: SmsLogEntity? = null
    ): Long {
        val customer = getCustomerById(transaction.customerId) ?: return -1L
        val newDue = (customer.totalDuePoisha - transaction.amountPoisha).coerceAtLeast(0L)
        val newPaid = customer.totalPaidPoisha + transaction.amountPoisha
        val updatedCustomer = customer.copy(
            totalDuePoisha = newDue,
            totalPaidPoisha = newPaid,
            updatedAt = System.currentTimeMillis()
        )
        val finalTransaction = transaction.copy(balanceAfterPoisha = newDue)
        
        insertTransaction(finalTransaction)
        updateCustomer(updatedCustomer)
        
        notification?.let { insertNotification(it) }
        smsLog?.let { insertSmsLog(it) }
        
        return newDue
    }

    // --- NOTIFICATIONS ---
    @Query("SELECT * FROM notifications WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getNotificationsForCustomer(customerId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE shopId = :shopId ORDER BY timestamp DESC")
    fun getNotificationsForShop(shopId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE customerId = :customerId")
    suspend fun markAllCustomerNotificationsAsRead(customerId: String)

    // --- SMS LOGS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmsLog(log: SmsLogEntity)

    @Query("SELECT * FROM sms_logs WHERE shopId = :shopId ORDER BY timestamp DESC")
    fun getSmsLogs(shopId: String): Flow<List<SmsLogEntity>>

    // --- RESET / PURGE (FOR PLAY STORE DATA DELETION) ---
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    @Query("DELETE FROM sms_logs")
    suspend fun deleteAllSmsLogs()
}
