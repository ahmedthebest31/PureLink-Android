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

    private var TRACKING_PARAMS = listOf(
        "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
        "fbclid", "si", "ref", "gclid", "gclsrc", "dclid", "msclkid",
        "mc_eid", "_ga", "yclid", "vero_conv", "vero_id", "wickedid",
        "share_id", "igshid"
    )

    // Pre-compiled regex for parameters to improve performance
    @Volatile
    private var PARAMS_REGEX = buildRegex(TRACKING_PARAMS)

    private fun buildRegex(params: List<String>): Regex {
        return Regex("[?&](${params.joinToString("|") { Regex.escape(it) }})=[^&]*", RegexOption.IGNORE_CASE)
    }

    suspend fun reloadRules(context: android.content.Context) {
        val repo = com.ahmedsamy.purelink.data.RulesRepository(context)
        val newRules = repo.loadRules()
        if (!newRules.isNullOrEmpty()) {
            // Merge or Replace? The prompt implies "using the updated list", usually replacing defaults is the goal of a dynamic update.
            // However, ensuring we don't lose core ones might be good.
            // Let's assume the remote list is the authority.
            TRACKING_PARAMS = newRules
            PARAMS_REGEX = buildRegex(TRACKING_PARAMS)
        }
    }

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
        result = result.replace(Regex("\\?&"), "?")
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
     * Processes the text: finds URLs, optionally unshortens them, and cleans them.
     * Handles mixed text with multiple URLs.
     */
    suspend fun processText(text: String, unshorten: Boolean): String = withContext(Dispatchers.IO) {
        val matches = URL_EXTRACTOR_REGEX.findAll(text).toList()
        if (matches.isEmpty()) return@withContext text

        val uniqueUrls = matches.map { it.value }.distinct()
        val urlMap = mutableMapOf<String, String>()

        uniqueUrls.forEach { url ->
            var processed = url
            if (unshorten) {
                processed = resolveUrl(url)
            }
            processed = cleanSingleUrl(processed)
            urlMap[url] = processed
        }

        URL_EXTRACTOR_REGEX.replace(text) { match ->
            urlMap[match.value] ?: match.value
        }
    }

    /**
     * Resolves shortened URLs (e.g., bit.ly) to their destination.
     * Suspend function for background execution.
     */
    suspend fun resolveUrl(shortUrl: String): String = withContext(Dispatchers.IO) {
        try {
            var urlStr = shortUrl.trim()
            // Validate URL format before attempting connection to avoid MalformedURLException on garbage
            if (!urlStr.startsWith("http")) urlStr = "https://$urlStr"
            
            // Basic validation: must contain a host
            try {
                val u = URL(urlStr)
                if (u.host.isNullOrEmpty()) return@withContext shortUrl
            } catch (e: Exception) {
                return@withContext shortUrl
            }
            
            val connection = URL(urlStr).openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 5000 // 5s timeout
            connection.readTimeout = 5000
            connection.connect()
            
            val location = connection.getHeaderField("Location") ?: connection.url.toString()
            if (location.isNotEmpty()) location else shortUrl
        } catch (e: Exception) {
            e.printStackTrace()
            shortUrl
        }
    }
}