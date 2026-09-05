package com.example.gerdachat.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gerdachat.data.model.CallRecord
import com.example.gerdachat.data.model.ChatMessage
import com.example.gerdachat.data.model.Contact
import com.example.gerdachat.data.model.Memory

@Database(
    entities = [ChatMessage::class, Contact::class, CallRecord::class, Memory::class],
    version = 1,
    exportSchema = false
)
abstract class GerdaDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao
    abstract fun callDao(): CallDao
    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile
        private var INSTANCE: GerdaDatabase? = null

        fun getDatabase(context: Context): GerdaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GerdaDatabase::class.java,
                    "gerda_chat_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
