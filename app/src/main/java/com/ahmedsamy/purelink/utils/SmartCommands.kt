package com.ahmedsamy.purelink.utils

import android.content.Context
import android.util.Base64
import com.ahmedsamy.purelink.R
import java.util.UUID

object SmartCommands {

    data class CommandResult(
        val output: String,
        val toastResId: Int
    )

    fun parseAndExecute(text: String, context: Context): CommandResult? {
        val trimmed = text.trim()

        if (!trimmed.startsWith("/")) return null

        val input = trimmed.removePrefix("/")

        return when {
            input.startsWith("wa ") -> {
                val number = input.removePrefix("wa ").trim()
                if (number.isEmpty()) return null
                val cleaned = number.replace("+", "").replace(" ", "").replace("-", "")
                CommandResult("https://wa.me/$cleaned", R.string.toast_smart_wa)
            }
            input.startsWith("tg ") -> {
                val username = input.removePrefix("tg ").trim()
                if (username.isEmpty()) return null
                val cleaned = username.replace("@", "")
                CommandResult("https://t.me/$cleaned", R.string.toast_smart_tg)
            }
            input.startsWith("b64e ") -> {
                val data = input.removePrefix("b64e ").trim()
                if (data.isEmpty()) return null
                val encoded = Base64.encodeToString(data.toByteArray(), Base64.NO_WRAP)
                CommandResult(encoded, R.string.toast_smart_b64e)
            }
            input.startsWith("b64d ") -> {
                val data = input.removePrefix("b64d ").trim()
                if (data.isEmpty()) return null
                val decoded = try {
                    String(Base64.decode(data, Base64.NO_WRAP))
                } catch (_: Exception) {
                    return null
                }
                CommandResult(decoded, R.string.toast_smart_b64d)
            }
            input == "uuid" -> {
                CommandResult(UUID.randomUUID().toString(), R.string.toast_smart_uuid)
            }
            input == "upper" || input.startsWith("upper ") -> {
                val content = input.removePrefix("upper").trim()
                CommandResult(if (content.isEmpty()) trimmed.removePrefix("/upper").trim().uppercase() else content.uppercase(), R.string.toast_smart_upper)
            }
            input == "lower" || input.startsWith("lower ") -> {
                val content = input.removePrefix("lower").trim()
                CommandResult(if (content.isEmpty()) trimmed.removePrefix("/lower").trim().lowercase() else content.lowercase(), R.string.toast_smart_lower)
            }
            input.startsWith("capitalize") -> {
                val content = input.removePrefix("capitalize").trim()
                val result = if (content.isEmpty()) trimmed.removePrefix("/capitalize").trim() else content
                CommandResult(result.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }, R.string.toast_smart_capitalize)
            }
            input == "reverse" || input.startsWith("reverse ") -> {
                val content = input.removePrefix("reverse").trim()
                CommandResult((if (content.isEmpty()) trimmed.removePrefix("/reverse").trim() else content).reversed(), R.string.toast_smart_reverse)
            }
            input == "clear" || input.startsWith("clear ") -> {
                val content = input.removePrefix("clear").trim()
                CommandResult(if (content.isEmpty()) trimmed.removePrefix("/clear").trim() else content, R.string.toast_smart_clear)
            }
            input == "trim" || input.startsWith("trim ") -> {
                val content = input.removePrefix("trim").trim()
                CommandResult((if (content.isEmpty()) trimmed.removePrefix("/trim").trim() else content).trim(), R.string.toast_smart_trim)
            }
            else -> null
        }
    }
}
