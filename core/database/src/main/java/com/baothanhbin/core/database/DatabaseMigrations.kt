package com.baothanhbin.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE chats ADD COLUMN ownerUserId TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE chats ADD COLUMN serverChatId TEXT")
        database.execSQL("ALTER TABLE chats ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 1")
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_chats_ownerUserId ON chats(ownerUserId)"
        )
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_chats_serverChatId ON chats(serverChatId)"
        )
    }
}
