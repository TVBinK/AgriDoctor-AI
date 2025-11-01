package com.baothanhbin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.baothanhbin.core.database.model.ChatEntity

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity): Long
    
    @Update
    suspend fun updateChat(chat: ChatEntity)
    
    @Query("SELECT * FROM chats WHERE id = :id")
    suspend fun getChatById(id: Long): ChatEntity?
    
    @Query("SELECT * FROM chats ORDER BY updatedAt DESC")
    suspend fun getAllChats(): List<ChatEntity>
    
    @Query("DELETE FROM chats WHERE id = :id")
    suspend fun deleteChat(id: Long)
    
    @Query("DELETE FROM chats")
    suspend fun deleteAllChats()
}

