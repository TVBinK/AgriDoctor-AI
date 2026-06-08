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

    @Query("SELECT * FROM chats WHERE serverChatId = :serverChatId LIMIT 1")
    suspend fun getChatByServerChatId(serverChatId: String): ChatEntity?

    @Query("SELECT * FROM chats WHERE ownerUserId = :ownerUserId ORDER BY updatedAt DESC")
    suspend fun getAllChats(ownerUserId: String): List<ChatEntity>

    @Query("SELECT * FROM chats WHERE ownerUserId = :ownerUserId AND needsSync = 1 ORDER BY updatedAt ASC, id ASC")
    suspend fun getChatsNeedingSync(ownerUserId: String): List<ChatEntity>

    @Query("UPDATE chats SET ownerUserId = :ownerUserId WHERE ownerUserId = ''")
    suspend fun assignAnonymousChats(ownerUserId: String)

    @Query("DELETE FROM chats WHERE id = :id")
    suspend fun deleteChat(id: Long)

    @Query("DELETE FROM chats WHERE ownerUserId = :ownerUserId")
    suspend fun deleteAllChats(ownerUserId: String)
}
