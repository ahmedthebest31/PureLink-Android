package com.ahmedsamy.purelink.utils

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Singleton utility for URL cleaning, extraction, and resolution.
 * Handles mixed text (multiple URLs in one block).
 */
object UrlCleaner {
    // Regex to find http/https URLs. \S+ matches non-whitespace characters.
    private val URL_EXTRACTOR_REGEX = Regex("https?://\\S+")

    private val TRACKING_PARAMS = listOf(
        "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
        "fbclid", "si", "ref", "gclid", "gclsrc", "dclid", "msclkid",
        "mc_eid", "_ga", "yclid", "vero_conv", "vero_id", "wickedid",
        "share_id", "igshid"
    )

    // Pre-compiled regex for parameters to improve performance
    private val PARAMS_REGEX = Regex("[?&](${TRACKING_PARAMS.joinToString("|") { Regex.escape(it) }})=[^&]*", RegexOption.IGNORE_CASE)

    /**
     * Replaces all URLs in the text using the provided transform function.
     */
    fun replaceUrls(inputText: String, transform: (String) -> String): String {
        if (!inputText.contains("http", ignoreCase = true)) return inputText
        return URL_EXTRACTOR_REGEX.replace(inputText) { matchResult ->
            transform(matchResult.value)
        }
    }

    /**
     * Scans the input text for URLs, cleans them, and returns the modified text.
     * Preserves non-URL text around the links.
     */
    fun cleanMixedText(inputText: String):
        String {
        return replaceUrls(inputText) { url -> cleanSingleUrl(url) }
    }

    /**
     * Cleans a single URL string by removing tracking parameters.
     */
    fun cleanSingleUrl(url: String): String {
        var result = url

        // Remove tracking parameters
        result = PARAMS_REGEX.replace(result, "")

        // Clean up malformed query strings (e.g. "?&" -> "?", "&&" -> "&")
        result = result.replace(Regex("?&?"), "?")
        result = result.replace(Regex("&&+"), "&")
        
        // Remove trailing separators
        if (result.endsWith("?") || result.endsWith("&")) {
            result = result.dropLast(1)
        }
        if (result.endsWith("?")) {
            result = result.dropLast(1)
        }

        return result
    }

    /**
     * Resolves shortened URLs (e.g., bit.ly) to their destination.
     * Suspend function for background execution.
     */
    suspend fun resolveUrl(shortUrl: String): String = withContext(Dispatchers.IO) {
        try {
            var urlStr = shortUrl.trim()
            if (!urlStr.startsWith("http")) urlStr = "https://$urlStr"
            
            val connection = URL(urlStr).openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.instanceFollowRedirects = false
            connection.connect()
            
            val location = connection.getHeaderField("Location") ?: connection.url.toString()
            if (location.isNotEmpty()) location else shortUrl
        } catch (e: Exception) {
            shortUrl
        }
    }
}