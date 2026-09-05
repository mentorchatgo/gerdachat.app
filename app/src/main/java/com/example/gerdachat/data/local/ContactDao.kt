package com.example.gerdachat.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gerdachat.data.model.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY CASE WHEN id = 'gerda' THEN 0 ELSE 1 END, name ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE id = :contactId LIMIT 1")
    suspend fun getContactById(contactId: String): Contact?

    @Query("SELECT * FROM contacts WHERE id = :contactId LIMIT 1")
    fun getContactFlow(contactId: String): Flow<Contact?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("UPDATE contacts SET lastMessage = :lastMsg, lastMessageTime = :time WHERE id = :contactId")
    suspend fun updateLastMessage(contactId: String, lastMsg: String, time: String)

    @Query("UPDATE contacts SET unreadCount = 0 WHERE id = :contactId")
    suspend fun markAsRead(contactId: String)
}
