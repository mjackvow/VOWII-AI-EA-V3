package com.example.data.config

import com.example.BuildConfig

/**
 * Institutional API Keys and Data Provider Endpoints
 * Included directly in code for deployment in private repository environments
 * with automatic fallback checking against BuildConfig and environment variables.
 */
object ApiConfig {

    // 1. Google Gemini Multimodal Vision & Reasoning API Key
    const val DEFAULT_GEMINI_API_KEY = "AIzaSyDc4wvuslYy3gkVSIgWmQrc6V1VgmLIwPs"
    val GEMINI_API_KEY: String
        get() = resolveKey("GEMINI_API_KEY", DEFAULT_GEMINI_API_KEY)

    // 2. Finnhub / Finnbub Real-Time Forex, Quotes & Rates
    const val DEFAULT_FINNHUB_KEY = "d9ghg4hr01qq65369ap0d9ghg4hr01qq65369apg"
    val FINNHUB_KEY: String
        get() = resolveKey("FINNHUB_API_KEY", DEFAULT_FINNHUB_KEY, "FINNBUB_API_KEY", "FINNHUB_SECRET", "FINNBUB_SECRET")

    // 3. UnoRouter Free Models (Space Bunny Alpha & Nemotron-3 Ultra 550B)
    const val DEFAULT_UNOROUTER_API_KEY = "sk-zNCsHajAvDBAs3Q6kLtkrTUu3nHsPc16mp4VKqvidJaSQcjP"
    val UNOROUTER_API_KEY: String
        get() = resolveKey("UNOROUTER_API_KEY", DEFAULT_UNOROUTER_API_KEY)

    // 4. TwelveData Real-Time Quotes & Forex
    const val DEFAULT_TWELVE_DATA_KEY = "26c4993fde2a49afb8c6f900401e576c"
    val TWELVE_DATA_KEY: String
        get() = resolveKey("TWELVE_DATA_API_KEY", DEFAULT_TWELVE_DATA_KEY)

    // 5. OpenRouter DeepSeek R1
    const val DEFAULT_OPENROUTER_API_KEY = "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"
    val OPENROUTER_API_KEY: String
        get() = resolveKey("OPENROUTER_API_KEY", DEFAULT_OPENROUTER_API_KEY)

    // 6. BazaarLink GPT-4o Mini
    const val DEFAULT_BAZAARLINK_API_KEY = "sk-bl-qpnWt1Bovo_8v2fCy6ih6H4_7DgroHvDR3da71bA8KnQUrtC"
    val BAZAARLINK_API_KEY: String
        get() = resolveKey("BAZAARLINK_API_KEY", DEFAULT_BAZAARLINK_API_KEY)

    // 7. NVIDIA NIM Nemotron-4 340B
    const val DEFAULT_NVIDIA_NEMOTRON_KEY = "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"
    val NVIDIA_NEMOTRON_KEY: String
        get() = resolveKey("NVIDIA_NEMOTRON_API_KEY", DEFAULT_NVIDIA_NEMOTRON_KEY)

    // 8. NVIDIA NIM DeepSeek R1 671B
    const val DEFAULT_NVIDIA_DEEPSEEK_KEY = "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"
    val NVIDIA_DEEPSEEK_KEY: String
        get() = resolveKey("NVIDIA_DEEPSEEK_API_KEY", DEFAULT_NVIDIA_DEEPSEEK_KEY)

    // 9. Cohere Command R+
    const val DEFAULT_COHERE_API_KEY = "9gUeeanTn9ZLuVfaH4GSwjoE6VEDODkh2Gflrr5a"
    val COHERE_API_KEY: String
        get() = resolveKey("COHERE_API_KEY", DEFAULT_COHERE_API_KEY)

    // 10. Alpaca Paper Broker & Market Data
    const val ALPACA_KEY = "PKMSXG3C7CFFXG4B32LUJBGJNI"
    const val ALPACA_SECRET = "D9eagbXULrCALm3Ku3saDpJUTodiFQJfLF1nK73pimNX"
    const val ALPACA_PAPER_BASE_URL = "https://paper-api.alpaca.markets/v2"
    const val ALPACA_DATA_BASE_URL = "https://data.alpaca.markets/v2"

    // 11. Auxiliary Financial Feeds
    const val ITICK_API_KEY = "ae78bfbf587a421592cf517432d323dacdbd9e4b04864e70bdde440ea7ebb081"
    const val MASSIVE_API_KEY = "wf3CgbpxSJXcLXcYxFUwmADxrJAAmSED"

    private fun resolveKey(primaryKeyName: String, fallbackDefault: String, vararg aliasNames: String): String {
        return try {
            val names = listOf(primaryKeyName) + aliasNames
            val bFields = BuildConfig::class.java.fields

            for (name in names) {
                val fieldVal = bFields.firstOrNull { it.name == name }?.get(null) as? String
                if (!fieldVal.isNullOrBlank() && !fieldVal.contains("MY_") && fieldVal != name) {
                    return fieldVal
                }
            }

            for (name in names) {
                val envVal = System.getenv(name)
                if (!envVal.isNullOrBlank() && !envVal.contains("MY_") && envVal != name) {
                    return envVal
                }
            }

            fallbackDefault
        } catch (_: Throwable) {
            fallbackDefault
        }
    }
}
