package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.BakiKhataRepository

class BakiKhataApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { BakiKhataRepository(database.bakiKhataDao()) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: BakiKhataApp
            private set
    }
}
