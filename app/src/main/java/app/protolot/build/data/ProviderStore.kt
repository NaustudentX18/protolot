package app.protolot.build.data

/**
 * In-memory BYOK LLM provider config (M1).
 * Persisted prefs can land later; graceful degrade when unkeyed.
 */
object ProviderStore {
    data class LlmConfig(
        val baseUrl: String = "",
        val apiKey: String = "",
        val model: String = "",
    ) {
        val isConfigured: Boolean
            get() = baseUrl.isNotBlank() && apiKey.isNotBlank() && model.isNotBlank()
    }

    @Volatile
    var llm: LlmConfig = LlmConfig()
        private set

    fun saveLlm(baseUrl: String, apiKey: String, model: String) {
        llm = LlmConfig(
            baseUrl = baseUrl.trim().trimEnd('/'),
            apiKey = apiKey.trim(),
            model = model.trim(),
        )
    }

    fun clearLlm() {
        llm = LlmConfig()
    }
}
