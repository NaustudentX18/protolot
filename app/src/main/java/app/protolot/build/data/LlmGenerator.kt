package app.protolot.build.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * OpenAI-compatible BYOK generation (AC-Providers).
 * Unconfigured → stub generation OK. Failures surface retry + Check Providers.
 */
object LlmGenerator {
    private val executor = Executors.newSingleThreadExecutor()

    sealed class GenResult {
        data class Ok(val pack: ProjectPack, val source: String) : GenResult()
        data class Fail(val message: String, val checkProviders: Boolean) : GenResult()
    }

    fun generateSync(prompt: String, boardClass: String?): GenResult {
        val cfg = ProviderStore.llm
        if (!cfg.isConfigured) {
            val pack = ProjectStore.createFromPrompt(prompt, boardClass, generationSource = "stub")
            return GenResult.Ok(pack, "stub")
        }
        return try {
            val body = callChatCompletions(cfg, prompt, boardClass)
            val parsed = tryParsePackJson(body, prompt, boardClass)
            if (parsed != null) {
                ProjectStore.upsert(parsed)
                GenResult.Ok(parsed, "llm")
            } else {
                val pack = ProjectStore.createFromPrompt(
                    prompt,
                    boardClass,
                    generationSource = "stub-after-llm",
                )
                GenResult.Ok(pack, "stub-after-llm")
            }
        } catch (e: Exception) {
            GenResult.Fail(
                message = "Pack generation failed: ${e.message ?: "network/provider error"}",
                checkProviders = true,
            )
        }
    }

    fun testConnectionSync(): Pair<Boolean, String> {
        val cfg = ProviderStore.llm
        if (!cfg.isConfigured) return false to "Configure base URL, API key, and model first."
        return try {
            val url = URL("${cfg.baseUrl}/models")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer ${cfg.apiKey}")
                connectTimeout = 12_000
                readTimeout = 12_000
            }
            val code = conn.responseCode
            if (code in 200..299) {
                true to "Connection OK (HTTP $code)."
            } else {
                val chat = callChatCompletions(cfg, "ping", null, maxTokens = 8)
                if (chat.isNotBlank()) true to "Chat endpoint OK."
                else false to "HTTP $code from /models"
            }
        } catch (e: Exception) {
            try {
                callChatCompletions(cfg, "ping", null, maxTokens = 8)
                true to "Chat endpoint OK."
            } catch (e2: Exception) {
                false to (e2.message ?: e.message ?: "Connection failed")
            }
        }
    }

    private fun callChatCompletions(
        cfg: ProviderStore.LlmConfig,
        prompt: String,
        boardClass: String?,
        maxTokens: Int = 1200,
    ): String {
        val url = URL("${cfg.baseUrl}/chat/completions")
        val system = """
            You are Protolot pack generator. Reply with ONLY a JSON object matching:
            {"title":"...","overview":"...","wiring":{"nets":[{"name":"...","connections":[{"fromComponent":"...","fromPin":"...","toComponent":"...","toPin":"...","notes":null}]}]},
            "bom":[{"ref":"U1","mpn":"...","qty":1,"notes":"...","confidence":0.8}],
            "assembly":[{"title":"...","body":"..."}],"firmwareNotes":"..."}
            No markdown fences. Hardware build packs only.
        """.trimIndent()
        val user = buildString {
            append("Prompt: "); append(prompt)
            if (!boardClass.isNullOrBlank()) append("\nBoard class: ").append(boardClass)
        }
        val payload = JSONObject()
            .put("model", cfg.model)
            .put(
                "messages",
                JSONArray()
                    .put(JSONObject().put("role", "system").put("content", system))
                    .put(JSONObject().put("role", "user").put("content", user)),
            )
            .put("temperature", 0.2)
            .put("max_tokens", maxTokens)

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Authorization", "Bearer ${cfg.apiKey}")
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 20_000
            readTimeout = 60_000
        }
        OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = BufferedReader(InputStreamReader(stream)).use { it.readText() }
        if (code !in 200..299) {
            throw IllegalStateException("Provider HTTP $code: ${text.take(200)}")
        }
        val root = JSONObject(text)
        val choices = root.optJSONArray("choices") ?: throw IllegalStateException("No choices in response")
        val msg = choices.getJSONObject(0).getJSONObject("message").getString("content")
        return msg.trim()
    }

    private fun tryParsePackJson(content: String, prompt: String, boardClass: String?): ProjectPack? {
        val jsonStart = content.indexOf('{')
        val jsonEnd = content.lastIndexOf('}')
        if (jsonStart < 0 || jsonEnd <= jsonStart) return null
        return try {
            val obj = JSONObject(content.substring(jsonStart, jsonEnd + 1))
            val title = obj.optString("title", "").ifBlank { prompt.take(42) }
            val overview = obj.optString("overview", "Generated pack")
            val wiringObj = obj.optJSONObject("wiring")
            val nets = mutableListOf<WiringNet>()
            val netsArr = wiringObj?.optJSONArray("nets")
            if (netsArr != null) {
                for (i in 0 until netsArr.length()) {
                    val n = netsArr.getJSONObject(i)
                    val connsArr = n.optJSONArray("connections") ?: JSONArray()
                    val conns = mutableListOf<WiringConnection>()
                    for (j in 0 until connsArr.length()) {
                        val c = connsArr.getJSONObject(j)
                        conns += WiringConnection(
                            c.optString("fromComponent", "?"),
                            c.optString("fromPin", "?"),
                            c.optString("toComponent", "?"),
                            c.optString("toPin", "?"),
                            c.optString("notes", null)?.takeIf { it.isNotBlank() },
                        )
                    }
                    nets += WiringNet(n.optString("name", "NET$i"), conns)
                }
            }
            if (nets.isEmpty()) nets += StubPackFactory.defaultWiring(boardClass).nets

            val bomArr = obj.optJSONArray("bom") ?: JSONArray()
            val bom = mutableListOf<BomLine>()
            for (i in 0 until bomArr.length()) {
                val b = bomArr.getJSONObject(i)
                val mpn = b.optString("mpn", null)?.takeIf { it.isNotBlank() }
                val conf = if (b.has("confidence")) b.optDouble("confidence").toFloat() else 0.7f
                bom += BomLine(
                    ref = b.optString("ref", "R$i"),
                    mpn = mpn,
                    qty = b.optInt("qty", 1),
                    notes = b.optString("notes", ""),
                    estUnitPriceLabel = "Estimate only",
                    confidence = conf,
                    vendorLinks = VendorLinks.defaultsFor(mpn),
                )
            }
            if (bom.isEmpty()) bom += StubPackFactory.defaultBom(boardClass)

            val asmArr = obj.optJSONArray("assembly") ?: JSONArray()
            val asm = mutableListOf<AssemblyStep>()
            for (i in 0 until asmArr.length()) {
                val a = asmArr.getJSONObject(i)
                asm += AssemblyStep(a.optString("title", "Step ${i + 1}"), a.optString("body", ""))
            }
            if (asm.isEmpty()) asm += StubPackFactory.defaultAssembly()

            ProjectPack(
                id = java.util.UUID.randomUUID().toString().take(8),
                title = title.take(80),
                prompt = prompt.trim(),
                boardClass = boardClass,
                overview = overview,
                wiring = WiringGraph(nets),
                bom = bom,
                assembly = asm,
                cadHooks = StubPackFactory.CAD_HOOKS,
                firmwareNotes = obj.optString("firmwareNotes", StubPackFactory.defaultFirmware(boardClass)),
                createdAtMs = System.currentTimeMillis(),
                generationSource = "llm",
            )
        } catch (_: Exception) {
            null
        }
    }
}
