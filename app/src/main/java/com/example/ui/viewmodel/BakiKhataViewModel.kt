package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BakiKhataApp
import com.example.data.admob.AdManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ShopEntity
import com.example.data.local.entity.SmsLogEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.pdf.PdfReportGenerator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

enum class UserRole {
    SHOPKEEPER, // দোকানদার
    CUSTOMER    // গ্রাহক
}

enum class CustomerFilter {
    ALL,        // সকল
    WITH_DUE,   // বাকি আছে
    SETTLED     // পরিশোধিত
}

enum class CustomerSort {
    HIGHEST_DUE,        // সর্বোচ্চ বাকি
    RECENT_TRANSACTION, // সাম্প্রতিক লেনদেন
    NAME                // নামানুসারে
}

class BakiKhataViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as BakiKhataApp).repository

    // Current Role (Shopkeeper vs Customer)
    private val _currentRole = MutableStateFlow(UserRole.SHOPKEEPER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Current Shop
    private val _currentShop = MutableStateFlow<ShopEntity?>(null)
    val currentShop: StateFlow<ShopEntity?> = _currentShop.asStateFlow()

    // Customer Selection
    private val _selectedCustomerId = MutableStateFlow<String?>(AppDatabase.DEMO_CUSTOMER_RAHIM_ID)
    val selectedCustomerId: StateFlow<String?> = _selectedCustomerId.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer.asStateFlow()

    // Customer Search & Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow(CustomerFilter.ALL)
    val filter: StateFlow<CustomerFilter> = _filter.asStateFlow()

    private val _sort = MutableStateFlow(CustomerSort.HIGHEST_DUE)
    val sort: StateFlow<CustomerSort> = _sort.asStateFlow()

    // Raw Customers list from DB
    private val rawCustomers: StateFlow<List<CustomerEntity>> = repository.getCustomers(AppDatabase.DEFAULT_SHOP_ID)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered & Sorted Customers
    val filteredCustomers: StateFlow<List<CustomerEntity>> = combine(
        rawCustomers,
        _searchQuery,
        _filter,
        _sort
    ) { list, query, currentFilter, currentSort ->
        val filtered = list.filter { cust ->
            val matchesQuery = query.isBlank() ||
                    cust.name.contains(query, ignoreCase = true) ||
                    cust.phone.contains(query) ||
                    cust.address.contains(query, ignoreCase = true)
            val matchesFilter = when (currentFilter) {
                CustomerFilter.ALL -> true
                CustomerFilter.WITH_DUE -> cust.totalDuePoisha > 0L
                CustomerFilter.SETTLED -> cust.totalDuePoisha <= 0L
            }
            matchesQuery && matchesFilter
        }

        when (currentSort) {
            CustomerSort.HIGHEST_DUE -> filtered.sortedByDescending { it.totalDuePoisha }
            CustomerSort.RECENT_TRANSACTION -> filtered.sortedByDescending { it.updatedAt }
            CustomerSort.NAME -> filtered.sortedBy { it.name }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Transactions for the Shop
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions(AppDatabase.DEFAULT_SHOP_ID)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions for selected customer
    private val _selectedCustomerTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val selectedCustomerTransactions: StateFlow<List<TransactionEntity>> = _selectedCustomerTransactions.asStateFlow()

    // Notifications for current customer
    private val _customerNotifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val customerNotifications: StateFlow<List<NotificationEntity>> = _customerNotifications.asStateFlow()

    // SMS Logs
    val smsLogs: StateFlow<List<SmsLogEntity>> = repository.getSmsLogs(AppDatabase.DEFAULT_SHOP_ID)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Interstitial Ad Trigger Event (for UI dialog simulation or AdMob)
    private val _showInterstitialEvent = MutableSharedFlow<String>()
    val showInterstitialEvent: SharedFlow<String> = _showInterstitialEvent.asSharedFlow()

    // Toast / Feedback message
    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    // PDF generation result
    private val _pdfGeneratedFile = MutableStateFlow<File?>(null)
    val pdfGeneratedFile: StateFlow<File?> = _pdfGeneratedFile.asStateFlow()

    init {
        loadShopAndInit()
    }

    private fun loadShopAndInit() {
        viewModelScope.launch {
            repository.getAllShops().collect { shops ->
                if (shops.isNotEmpty()) {
                    _currentShop.value = shops.first()
                } else {
                    AppDatabase.getInstance(getApplication()).bakiKhataDao().let {
                        AppDatabase.seedInitialData(it)
                    }
                }
            }
        }

        // Keep selected customer in sync
        viewModelScope.launch {
            _selectedCustomerId.collect { id ->
                if (id != null) {
                    repository.getCustomerFlow(id).collect { cust ->
                        _selectedCustomer.value = cust
                    }
                } else {
                    _selectedCustomer.value = null
                }
            }
        }

        // Keep transactions of selected customer in sync
        viewModelScope.launch {
            _selectedCustomerId.collect { id ->
                if (id != null) {
                    repository.getTransactionsForCustomer(id).collect { txs ->
                        _selectedCustomerTransactions.value = txs
                    }
                } else {
                    _selectedCustomerTransactions.value = emptyList()
                }
            }
        }

        // Keep customer notifications in sync
        viewModelScope.launch {
            _selectedCustomerId.collect { id ->
                if (id != null) {
                    repository.getNotificationsForCustomer(id).collect { notifs ->
                        _customerNotifications.value = notifs
                    }
                }
            }
        }
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
    }

    fun selectCustomer(customerId: String) {
        _selectedCustomerId.value = customerId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: CustomerFilter) {
        _filter.value = filter
    }

    fun setSort(sort: CustomerSort) {
        _sort.value = sort
    }

    // --- ACTIONS ---
    fun addDueTransaction(
        customerId: String,
        amountPoisha: Long,
        productName: String,
        quantity: Double,
        unitPricePoisha: Long,
        note: String,
        sendSms: Boolean = true
    ) {
        viewModelScope.launch {
            val shopId = _currentShop.value?.shopId ?: AppDatabase.DEFAULT_SHOP_ID
            val success = repository.addDueTransaction(
                shopId = shopId,
                customerId = customerId,
                amountPoisha = amountPoisha,
                productName = productName,
                quantity = quantity,
                unitPricePoisha = unitPricePoisha,
                note = note,
                sendSms = sendSms
            )
            if (success) {
                _uiMessage.emit("বাকি সফলভাবে যোগ করা হয়েছে!")
                checkAndTriggerInterstitial("নতুন বাকি সম্পন্ন হয়েছে")
            } else {
                _uiMessage.emit("বাকি যোগ করা সম্ভব হয়নি। সঠিক তথ্য দিন।")
            }
        }
    }

    fun addPaymentTransaction(
        customerId: String,
        amountPoisha: Long,
        paymentMethod: String,
        note: String,
        sendSms: Boolean = true
    ) {
        viewModelScope.launch {
            val shopId = _currentShop.value?.shopId ?: AppDatabase.DEFAULT_SHOP_ID
            val success = repository.addPaymentTransaction(
                shopId = shopId,
                customerId = customerId,
                amountPoisha = amountPoisha,
                paymentMethod = paymentMethod,
                note = note,
                sendSms = sendSms
            )
            if (success) {
                _uiMessage.emit("টাকা জমা সফলভাবে গ্রহণ করা হয়েছে!")
                checkAndTriggerInterstitial("জমা গ্রহণ সম্পন্ন হয়েছে")
            } else {
                _uiMessage.emit("টাকা জমা রেকর্ড করা সম্ভব হয়নি।")
            }
        }
    }

    fun createCustomer(
        name: String,
        phone: String,
        address: String,
        initialDuePoisha: Long
    ) {
        viewModelScope.launch {
            val shopId = _currentShop.value?.shopId ?: AppDatabase.DEFAULT_SHOP_ID
            val newId = repository.addCustomer(
                shopId = shopId,
                name = name,
                phone = phone,
                address = address,
                initialDuePoisha = initialDuePoisha
            )
            _selectedCustomerId.value = newId
            _uiMessage.emit("নতুন গ্রাহক যুক্ত করা হয়েছে!")
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
            _uiMessage.emit("গ্রাহকের তথ্য আপডেট হয়েছে")
        }
    }

    fun updateShop(
        name: String,
        ownerName: String,
        phone: String,
        address: String,
        upazila: String,
        district: String
    ) {
        viewModelScope.launch {
            val current = _currentShop.value ?: return@launch
            val updated = current.copy(
                name = name,
                ownerName = ownerName,
                phone = phone,
                address = address,
                upazila = upazila,
                district = district
            )
            repository.saveShop(updated)
            _currentShop.value = updated
            _uiMessage.emit("দোকানের প্রোফাইল সংরক্ষণ করা হয়েছে")
        }
    }

    fun generatePdfForCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            val shop = _currentShop.value ?: return@launch
            val txs = _selectedCustomerTransactions.value
            val pdfFile = PdfReportGenerator.generateCustomerStatementPdf(
                context = getApplication(),
                shop = shop,
                customer = customer,
                transactions = txs
            )
            _pdfGeneratedFile.value = pdfFile
            if (pdfFile != null) {
                _uiMessage.emit("পিডিএফ স্টেটমেন্ট সফলভাবে তৈরি হয়েছে!")
            } else {
                _uiMessage.emit("পিডিএফ ফাইল তৈরি করা সম্ভব হয়নি")
            }
        }
    }

    fun clearGeneratedPdf() {
        _pdfGeneratedFile.value = null
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            val customerId = _selectedCustomerId.value ?: return@launch
            repository.markNotificationsRead(customerId)
        }
    }

    fun requestAccountAndDataDeletion() {
        viewModelScope.launch {
            repository.purgeAllUserData()
            _uiMessage.emit("আপনার অ্যাকাউন্টের সকল লেনদেন ও গ্রাহকের তথ্য স্থায়ীভাবে মুছে ফেলা হয়েছে।")
        }
    }

    private fun checkAndTriggerInterstitial(contextTitle: String) {
        if (AdManager.shouldTriggerInterstitial()) {
            viewModelScope.launch {
                _showInterstitialEvent.emit(contextTitle)
            }
        }
    }
}
