package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BakiKhataDao
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ShopEntity
import com.example.data.local.entity.SmsLogEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ShopEntity::class,
        CustomerEntity::class,
        TransactionEntity::class,
        NotificationEntity::class,
        SmsLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bakiKhataDao(): BakiKhataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        const val DEFAULT_SHOP_ID = "shop_default_bd_001"
        const val DEMO_CUSTOMER_RAHIM_ID = "cust_rahim_001"
        const val DEMO_CUSTOMER_KARIM_ID = "cust_karim_002"
        const val DEMO_CUSTOMER_SALMA_ID = "cust_salma_003"
        const val DEMO_CUSTOMER_HOSSAIN_ID = "cust_hossain_004"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "baki_khata.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial authentic Bangladeshi ledger data
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database.bakiKhataDao())
                    }
                }
            }
        }

        suspend fun seedInitialData(dao: BakiKhataDao) {
            val now = System.currentTimeMillis()
            val dayMillis = 86_400_000L

            // 1. Initial Shopkeeper & Shop
            val shop = ShopEntity(
                shopId = DEFAULT_SHOP_ID,
                name = "মায়ের দোয়া জেনারেল স্টোর",
                ownerName = "মোহাম্মদ রফিকুল ইসলাম",
                phone = "01711002233",
                address = "দোকান নং ১২, কাঁচাবাজার রোড",
                upazila = "মিরপুর",
                district = "ঢাকা",
                smsSenderId = "BAKIKHATA",
                smsEnabled = true,
                createdAt = now - (30 * dayMillis)
            )
            dao.insertShop(shop)

            // 2. Customers
            val custRahim = CustomerEntity(
                customerId = DEMO_CUSTOMER_RAHIM_ID,
                shopId = DEFAULT_SHOP_ID,
                name = "রহিম মিয়া",
                phone = "01812345678",
                address = "বাড়ি নং ৪৫, ব্লক সি, মিরপুর-১০",
                totalDuePoisha = 150000L, // ৳1,500.00
                totalPaidPoisha = 85000L,  // ৳850.00
                isVerified = true,
                status = "ACTIVE",
                createdAt = now - (15 * dayMillis),
                updatedAt = now - (2 * 3600_000L)
            )
            val custKarim = CustomerEntity(
                customerId = DEMO_CUSTOMER_KARIM_ID,
                shopId = DEFAULT_SHOP_ID,
                name = "করিম চৌধুরী",
                phone = "01998765432",
                address = "সেকশন ৬, মিরপুর, ঢাকা",
                totalDuePoisha = 320000L, // ৳3,200.00
                totalPaidPoisha = 120000L,
                isVerified = true,
                status = "ACTIVE",
                createdAt = now - (20 * dayMillis),
                updatedAt = now - (5 * 3600_000L)
            )
            val custSalma = CustomerEntity(
                customerId = DEMO_CUSTOMER_SALMA_ID,
                shopId = DEFAULT_SHOP_ID,
                name = "সালমা বেগম",
                phone = "01755443322",
                address = "রোড ৪, হাউস ৮, পল্লবী",
                totalDuePoisha = 75000L, // ৳750.00
                totalPaidPoisha = 250000L,
                isVerified = true,
                status = "ACTIVE",
                createdAt = now - (25 * dayMillis),
                updatedAt = now - dayMillis
            )
            val custHossain = CustomerEntity(
                customerId = DEMO_CUSTOMER_HOSSAIN_ID,
                shopId = DEFAULT_SHOP_ID,
                name = "হোসনেয়ারা আখতার",
                phone = "01611223344",
                address = "কালশী রোড, মিরপুর-১১",
                totalDuePoisha = 0L, // পরিশোধিত (Settled)
                totalPaidPoisha = 450000L,
                isVerified = true,
                status = "ACTIVE",
                createdAt = now - (10 * dayMillis),
                updatedAt = now - (3 * dayMillis)
            )

            dao.insertCustomer(custRahim)
            dao.insertCustomer(custKarim)
            dao.insertCustomer(custSalma)
            dao.insertCustomer(custHossain)

            // 3. Transactions for Rahim
            dao.insertTransaction(
                TransactionEntity(
                    transactionId = "tx_rahim_01",
                    shopId = DEFAULT_SHOP_ID,
                    customerId = DEMO_CUSTOMER_RAHIM_ID,
                    type = "DUE",
                    amountPoisha = 118000L, // ৳1,180
                    productName = "নাজিরশাইল চাল ১০ কেজি, ডাল ২ কেজি",
                    quantity = 1.0,
                    unitPricePoisha = 118000L,
                    balanceAfterPoisha = 118000L,
                    dateEpochMillis = now - (5 * dayMillis),
                    note = "মাসিক মুদি সদাই"
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    transactionId = "tx_rahim_02",
                    shopId = DEFAULT_SHOP_ID,
                    customerId = DEMO_CUSTOMER_RAHIM_ID,
                    type = "PAYMENT",
                    amountPoisha = 50000L, // ৳500
                    productName = "ক্যাশ জমা",
                    balanceAfterPoisha = 68000L,
                    dateEpochMillis = now - (3 * dayMillis),
                    note = "বাকি পরিশোধ",
                    paymentMethod = "CASH"
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    transactionId = "tx_rahim_03",
                    shopId = DEFAULT_SHOP_ID,
                    customerId = DEMO_CUSTOMER_RAHIM_ID,
                    type = "DUE",
                    amountPoisha = 82000L, // ৳820
                    productName = "সয়াবিন তেল ২ লিটার, চিনি ১ কেজি",
                    quantity = 1.0,
                    unitPricePoisha = 82000L,
                    balanceAfterPoisha = 150000L,
                    dateEpochMillis = now - (2 * 3600_000L),
                    note = "জরুরি কেনাকাটা"
                )
            )

            // Notifications for Rahim
            dao.insertNotification(
                NotificationEntity(
                    notificationId = "notif_rahim_01",
                    shopId = DEFAULT_SHOP_ID,
                    customerId = DEMO_CUSTOMER_RAHIM_ID,
                    title = "নতুন বাকি যোগ হয়েছে",
                    messageBengali = "প্রিয় রহিম মিয়া, মায়ের দোয়া জেনারেল স্টোর থেকে আপনার নতুন ৳৮২০ টাকা বাকি যোগ হয়েছে। আপনার মোট বাকি এখন ৳১,৫০০।",
                    type = "DUE_ADDED",
                    timestamp = now - (2 * 3600_000L),
                    isRead = false
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    notificationId = "notif_rahim_02",
                    shopId = DEFAULT_SHOP_ID,
                    customerId = DEMO_CUSTOMER_RAHIM_ID,
                    title = "টাকা জমা গৃহীত হয়েছে",
                    messageBengali = "প্রিয় রহিম মিয়া, আপনার ৳৫০০ নগদ জমা গ্রহণ করা হয়েছে। ধন্যবাদ।",
                    type = "PAYMENT_RECEIVED",
                    timestamp = now - (3 * dayMillis),
                    isRead = true
                )
            )

            // Transactions for Karim
            dao.insertTransaction(
                TransactionEntity(
                    transactionId = "tx_karim_01",
                    shopId = DEFAULT_SHOP_ID,
                    customerId = DEMO_CUSTOMER_KARIM_ID,
                    type = "DUE",
                    amountPoisha = 320000L, // ৳3,200
                    productName = "মিনিকেট চাল ২৫ কেজি বস্তা",
                    quantity = 1.0,
                    unitPricePoisha = 320000L,
                    balanceAfterPoisha = 320000L,
                    dateEpochMillis = now - (5 * 3600_000L),
                    note = "চাল কেনা"
                )
            )

            // SMS Log
            dao.insertSmsLog(
                SmsLogEntity(
                    smsId = "sms_init_01",
                    shopId = DEFAULT_SHOP_ID,
                    customerPhone = "01812345678",
                    customerName = "রহিম মিয়া",
                    message = "মায়ের দোয়া স্টোর: আপনার নতুন বাকি ৳৮২০। মোট বাকি ৳১,৫০০। ধন্যবাদ।",
                    status = "SIMULATED",
                    timestamp = now - (2 * 3600_000L)
                )
            )
        }
    }
}
