package app.protolot.build.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Offline-readable JSON project pack export / reopen (AC-Project-Pack).
 */
object ProjectPackCodec {
    const val SCHEMA_VERSION = 1

    fun toJson(pack: ProjectPack): String {
        val root = JSONObject()
        root.put("schemaVersion", SCHEMA_VERSION)
        root.put("id", pack.id)
        root.put("title", pack.title)
        root.put("prompt", pack.prompt)
        root.put("boardClass", pack.boardClass ?: JSONObject.NULL)
        root.put("overview", pack.overview)
        root.put("firmwareNotes", pack.firmwareNotes)
        root.put("cadHooks", pack.cadHooks)
        root.put("createdAtMs", pack.createdAtMs)
        root.put("lotId", pack.lotId ?: JSONObject.NULL)
        root.put("variantLabel", pack.variantLabel ?: JSONObject.NULL)
        root.put("generationSource", pack.generationSource)

        val nets = JSONArray()
        pack.wiring.nets.forEach { net ->
            val n = JSONObject()
            n.put("name", net.name)
            val conns = JSONArray()
            net.connections.forEach { c ->
                conns.put(
                    JSONObject()
                        .put("fromComponent", c.fromComponent)
                        .put("fromPin", c.fromPin)
                        .put("toComponent", c.toComponent)
                        .put("toPin", c.toPin)
                        .put("notes", c.notes ?: JSONObject.NULL),
                )
            }
            n.put("connections", conns)
            nets.put(n)
        }
        root.put("wiring", JSONObject().put("nets", nets))

        val bom = JSONArray()
        pack.bom.forEach { line ->
            val links = JSONArray()
            line.vendorLinks.forEach { v ->
                links.put(
                    JSONObject()
                        .put("providerId", v.providerId)
                        .put("label", v.label)
                        .put("url", v.url),
                )
            }
            bom.put(
                JSONObject()
                    .put("ref", line.ref)
                    .put("mpn", line.mpn ?: JSONObject.NULL)
                    .put("qty", line.qty)
                    .put("notes", line.notes)
                    .put("estUnitPriceLabel", line.estUnitPriceLabel)
                    .put("estUnitPriceCents", line.estUnitPriceCents ?: JSONObject.NULL)
                    .put("confidence", line.confidence?.toDouble() ?: JSONObject.NULL)
                    .put("overridden", line.overridden)
                    .put("vendorLinks", links),
            )
        }
        root.put("bom", bom)

        val asm = JSONArray()
        pack.assembly.forEach { step ->
            asm.put(
                JSONObject()
                    .put("title", step.title)
                    .put("body", step.body)
                    .put("partRefs", JSONArray(step.partRefs))
                    .put("checked", step.checked),
            )
        }
        root.put("assembly", asm)
        return root.toString(2)
    }

    fun fromJson(text: String): ProjectPack {
        val root = JSONObject(text)
        val wiringObj = root.optJSONObject("wiring") ?: JSONObject()
        val netsArr = wiringObj.optJSONArray("nets") ?: JSONArray()
        val nets = mutableListOf<WiringNet>()
        for (i in 0 until netsArr.length()) {
            val n = netsArr.getJSONObject(i)
            val connsArr = n.optJSONArray("connections") ?: JSONArray()
            val conns = mutableListOf<WiringConnection>()
            for (j in 0 until connsArr.length()) {
                val c = connsArr.getJSONObject(j)
                conns += WiringConnection(
                    fromComponent = c.getString("fromComponent"),
                    fromPin = c.getString("fromPin"),
                    toComponent = c.getString("toComponent"),
                    toPin = c.getString("toPin"),
                    notes = c.optString("notes", null)?.takeIf { it.isNotBlank() && it != "null" },
                )
            }
            nets += WiringNet(n.getString("name"), conns)
        }

        val bomArr = root.optJSONArray("bom") ?: JSONArray()
        val bom = mutableListOf<BomLine>()
        for (i in 0 until bomArr.length()) {
            val b = bomArr.getJSONObject(i)
            val linksArr = b.optJSONArray("vendorLinks") ?: JSONArray()
            val links = mutableListOf<VendorLink>()
            for (j in 0 until linksArr.length()) {
                val v = linksArr.getJSONObject(j)
                links += VendorLink(
                    providerId = v.getString("providerId"),
                    label = v.getString("label"),
                    url = v.getString("url"),
                )
            }
            val conf = if (b.isNull("confidence")) null else b.optDouble("confidence").toFloat()
            val cents = if (b.isNull("estUnitPriceCents")) null else b.optInt("estUnitPriceCents")
            bom += BomLine(
                ref = b.getString("ref"),
                mpn = b.optString("mpn", null)?.takeIf { it.isNotBlank() && it != "null" },
                qty = b.getInt("qty"),
                notes = b.optString("notes", ""),
                estUnitPriceLabel = b.optString("estUnitPriceLabel", "Est. unavailable"),
                estUnitPriceCents = cents,
                confidence = conf,
                vendorLinks = links,
                overridden = b.optBoolean("overridden", false),
            )
        }

        val asmArr = root.optJSONArray("assembly") ?: JSONArray()
        val asm = mutableListOf<AssemblyStep>()
        for (i in 0 until asmArr.length()) {
            val a = asmArr.getJSONObject(i)
            val refsArr = a.optJSONArray("partRefs") ?: JSONArray()
            val refs = (0 until refsArr.length()).map { refsArr.getString(it) }
            asm += AssemblyStep(
                title = a.getString("title"),
                body = a.optString("body", ""),
                partRefs = refs,
                checked = a.optBoolean("checked", false),
            )
        }

        return ProjectPack(
            id = root.getString("id"),
            title = root.getString("title"),
            prompt = root.optString("prompt", ""),
            boardClass = root.optString("boardClass", null)?.takeIf { it.isNotBlank() && it != "null" },
            overview = root.optString("overview", ""),
            wiring = WiringGraph(nets),
            bom = bom,
            assembly = asm,
            cadHooks = root.optString("cadHooks", "CAD hooks — M4"),
            firmwareNotes = root.optString("firmwareNotes", ""),
            createdAtMs = root.optLong("createdAtMs", System.currentTimeMillis()),
            lotId = root.optString("lotId", null)?.takeIf { it.isNotBlank() && it != "null" },
            variantLabel = root.optString("variantLabel", null)?.takeIf { it.isNotBlank() && it != "null" },
            generationSource = root.optString("generationSource", "import"),
        )
    }
}
