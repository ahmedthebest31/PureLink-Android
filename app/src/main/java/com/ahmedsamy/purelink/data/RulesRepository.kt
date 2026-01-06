package com.ahmedsamy.purelink.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class RulesRepository(private val context: Context) {

    private val rulesFile = File(context.filesDir, "rules_v1.json")
    private val rulesUrl = "https://raw.githubusercontent.com/ahmedthebest31/PureLink/main/rules.json"

    suspend fun fetchAndSaveRules(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(rulesUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000 // 10s
            connection.readTimeout = 10000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                
                // Validate JSON structure locally
                val jsonObject = JSONObject(jsonString)
                if (jsonObject.has("blocklist")) {
                    rulesFile.writeText(jsonString)
                    return@withContext true
                }
            }
            return@withContext false
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun loadRules(): List<String>? = withContext(Dispatchers.IO) {
        if (!rulesFile.exists()) return@withContext null
        try {
            val jsonString = rulesFile.readText()
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("blocklist")
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
