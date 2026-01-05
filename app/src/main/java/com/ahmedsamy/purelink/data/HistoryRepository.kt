package com.ahmedsamy.purelink.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class HistoryItem(val id: Long, val url: String, val timestamp: Long)

class HistoryDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_HISTORY (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_URL TEXT NOT NULL UNIQUE,
                $COLUMN_TIMESTAMP INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "purelink_history.db"
        const val DATABASE_VERSION = 1
        const val TABLE_HISTORY = "history"
        const val COLUMN_ID = "_id"
        const val COLUMN_URL = "url"
        const val COLUMN_TIMESTAMP = "timestamp"
    }
}

class HistoryRepository(context: Context) {
    private val dbHelper = HistoryDbHelper(context)

    suspend fun getRecentHistory(): List<HistoryItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<HistoryItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            HistoryDbHelper.TABLE_HISTORY,
            null,
            null,
            null,
            null,
            null,
            "${HistoryDbHelper.COLUMN_TIMESTAMP} DESC",
            "10" // Limit to 10
        )
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(HistoryDbHelper.COLUMN_ID))
                val url = it.getString(it.getColumnIndexOrThrow(HistoryDbHelper.COLUMN_URL))
                val timestamp = it.getLong(it.getColumnIndexOrThrow(HistoryDbHelper.COLUMN_TIMESTAMP))
                items.add(HistoryItem(id, url, timestamp))
            }
        }
        items
    }

    suspend fun addUrl(url: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        
        // 1. Deduplication: Remove existing if present (to re-insert at top with new timestamp)
        // OR simply update the timestamp. Let's use INSERT OR REPLACE strategy effectively.
        // Actually, to ensure it's "moved to top", updating timestamp is best.
        // But to simplify, we can delete old entry with same URL and insert new.
        
        db.beginTransaction()
        try {
            // Delete if exists
            db.delete(HistoryDbHelper.TABLE_HISTORY, "${HistoryDbHelper.COLUMN_URL} = ?", arrayOf(url))
            
            // Insert new
            val values = ContentValues().apply {
                put(HistoryDbHelper.COLUMN_URL, url)
                put(HistoryDbHelper.COLUMN_TIMESTAMP, System.currentTimeMillis())
            }
            db.insert(HistoryDbHelper.TABLE_HISTORY, null, values)
            
            // Prune excess (keep only latest 10)
            // Efficient way: Delete items not in the top 10.
            // "DELETE FROM history WHERE _id NOT IN (SELECT _id FROM history ORDER BY timestamp DESC LIMIT 10)"
            db.execSQL("DELETE FROM ${HistoryDbHelper.TABLE_HISTORY} WHERE ${HistoryDbHelper.COLUMN_ID} NOT IN (SELECT ${HistoryDbHelper.COLUMN_ID} FROM ${HistoryDbHelper.TABLE_HISTORY} ORDER BY ${HistoryDbHelper.COLUMN_TIMESTAMP} DESC LIMIT 10)")
            
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
