package com.example.gerdachat

import android.app.Application
import com.example.gerdachat.data.local.GerdaDatabase
import com.example.gerdachat.data.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GerdaChatApp : Application() {
    val database by lazy { GerdaDatabase.getDatabase(this) }
    val repository by lazy { ChatRepository(database) }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            repository.initDefaultData()
            com.example.gerdachat.service.SpontaneousMessageManager.init(this@GerdaChatApp, repository)
        }
    }
}
