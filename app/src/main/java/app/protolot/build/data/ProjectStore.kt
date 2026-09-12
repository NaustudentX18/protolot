package app.protolot.build.data

import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

/**
 * In-memory store for M1 — projects, lots, blocked log, imported packs.
 */
object ProjectStore {
    private val projects = CopyOnWriteArrayList<ProjectPack>()
    private val lots = CopyOnWriteArrayList<Lot>()
    private val blocked = CopyOnWriteArrayList<BlockedGeneration>()
    private val listeners = CopyOnWriteArrayList<() -> Unit>()

    fun addListener(l: () -> Unit) { listeners += l }
    fun removeListener(l: () -> Unit) { listeners -= l }
    private fun notifyChanged() { listeners.forEach { runCatching { it() } } }

    fun all(): List<ProjectPack> = projects.toList().sortedByDescending { it.createdAtMs }
    fun get(id: String): ProjectPack? = projects.find { it.id == id }
    fun allLots(): List<Lot> = lots.toList().sortedByDescending { it.createdAtMs }
    fun getLot(id: String): Lot? = lots.find { it.id == id }
    fun blockedLog(): List<BlockedGeneration> = blocked.toList().sortedByDescending { it.blockedAtMs }

    fun recordBlocked(prompt: String, reason: String) {
        blocked.add(
            BlockedGeneration(prompt = prompt, reason = reason, blockedAtMs = System.currentTimeMillis()),
        )
        notifyChanged()
    }

    fun upsert(pack: ProjectPack) {
        val idx = projects.indexOfFirst { it.id == pack.id }
        if (idx >= 0) projects[idx] = pack else projects.add(0, pack)
        notifyChanged()
    }

    fun createFromPrompt(
        prompt: String,
        boardClass: String? = null,
        generationSource: String = "stub",
    ): ProjectPack {
        val id = UUID.randomUUID().toString().take(8)
        val title = deriveTitle(prompt)
        val pack = ProjectPack(
            id = id,
            title = title,
            prompt = prompt.trim(),
            boardClass = boardClass,
            overview = StubPackFactory.overview(title, prompt, boardClass, generationSource),
            wiring = StubPackFactory.defaultWiring(boardClass),
            bom = StubPackFactory.defaultBom(boardClass),
            assembly = StubPackFactory.defaultAssembly(),
            cadHooks = StubPackFactory.CAD_HOOKS,
            firmwareNotes = StubPackFactory.defaultFirmware(boardClass),
            createdAtMs = System.currentTimeMillis(),
            generationSource = generationSource,
        )
        projects.add(0, pack)
        notifyChanged()
        return pack
    }

    fun importPackJson(json: String): ProjectPack {
        val pack = ProjectPackCodec.fromJson(json)
        val final = if (projects.any { it.id == pack.id }) {
            pack.copy(id = UUID.randomUUID().toString().take(8), generationSource = "import")
        } else {
            pack.copy(generationSource = pack.generationSource.ifBlank { "import" })
        }
        projects.add(0, final)
        notifyChanged()
        return final
    }

    fun exportPackJson(id: String): String? {
        val pack = get(id) ?: return null
        return ProjectPackCodec.toJson(pack)
    }

    fun createLotFromRows(
        lotName: String,
        mode: LotMode,
        rows: List<LotImportRow>,
        sourceLabel: String,
    ): Pair<Lot, List<String>> {
        val grouped = rows.groupBy { Triple(it.name, it.board, it.variant) }
        val memberIds = mutableListOf<String>()
        val lotId = UUID.randomUUID().toString().take(8)

        grouped.forEach { (key, groupRows) ->
            val (name, board, variant) = key
            val existing = projects.find {
                it.lotId == null && it.title == name && it.variantLabel == variant && it.boardClass == board.ifBlank { null }
            }
            val bomLines = groupRows.mapIndexed { i, r ->
                BomLine(
                    ref = "L${i + 1}",
                    mpn = r.mpn,
                    qty = r.qty,
                    notes = r.notes.ifBlank { "From lot import ($variant)" },
                    estUnitPriceLabel = "Est. unavailable",
                    confidence = 0.65f,
                    vendorLinks = VendorLinks.defaultsFor(r.mpn),
                )
            }
            val pack = if (existing != null) {
                val updated = existing.copy(
                    bom = mergeBom(existing.bom, bomLines),
                    boardClass = board.ifBlank { existing.boardClass },
                    variantLabel = variant.ifBlank { existing.variantLabel },
                    lotId = lotId,
                    overview = existing.overview + "\n\nUpdated from lot import ($sourceLabel).",
                )
                upsert(updated)
                updated
            } else {
                val id = UUID.randomUUID().toString().take(8)
                val title = name.ifBlank { "Variant $variant" }
                val boardClass = board.ifBlank { null }
                val newPack = ProjectPack(
                    id = id,
                    title = title,
                    prompt = "Lot import: $title / $variant",
                    boardClass = boardClass,
                    overview = StubPackFactory.overview(title, "Lot member $variant", boardClass, "lot-$sourceLabel"),
                    wiring = StubPackFactory.defaultWiring(boardClass),
                    bom = if (bomLines.isNotEmpty()) bomLines else StubPackFactory.defaultBom(boardClass),
                    assembly = StubPackFactory.defaultAssembly(),
                    cadHooks = StubPackFactory.CAD_HOOKS,
                    firmwareNotes = StubPackFactory.defaultFirmware(boardClass),
                    createdAtMs = System.currentTimeMillis(),
                    lotId = lotId,
                    variantLabel = variant.ifBlank { null },
                    generationSource = "lot-$sourceLabel",
                )
                projects.add(0, newPack)
                newPack
            }
            memberIds += pack.id
        }

        val lot = Lot(
            id = lotId,
            name = lotName,
            mode = mode,
            memberProjectIds = memberIds,
            createdAtMs = System.currentTimeMillis(),
            sourceLabel = sourceLabel,
        )
        lots.add(0, lot)
        notifyChanged()
        return lot to memberIds
    }

    fun applyGlobalBomEdit(
        lotId: String,
        memberIds: List<String>,
        field: String,
        value: String,
        refFilter: String? = null,
        qtyMultiplier: Double? = null,
    ): Int {
        var changed = 0
        memberIds.forEach { id ->
            val pack = get(id) ?: return@forEach
            val newBom = pack.bom.map { line ->
                if (refFilter != null && !line.ref.equals(refFilter, ignoreCase = true)) return@map line
                when (field.lowercase()) {
                    "mpn" -> line.copy(mpn = value, overridden = true, vendorLinks = VendorLinks.defaultsFor(value))
                    "notes" -> line.copy(notes = value, overridden = true)
                    "qty" -> {
                        val q = qtyMultiplier?.let { (line.qty * it).toInt().coerceAtLeast(0) }
                            ?: value.toIntOrNull()
                        if (q == null) line else line.copy(qty = q, overridden = true)
                    }
                    else -> line
                }
            }
            if (newBom != pack.bom) {
                upsert(pack.copy(bom = newBom))
                changed++
            }
        }
        notifyChanged()
        return changed
    }

    private fun mergeBom(existing: List<BomLine>, incoming: List<BomLine>): List<BomLine> {
        val byMpn = existing.associateBy { it.mpn?.lowercase() }.toMutableMap()
        val out = existing.toMutableList()
        incoming.forEach { line ->
            val key = line.mpn?.lowercase()
            if (key != null && byMpn.containsKey(key)) {
                val idx = out.indexOfFirst { it.mpn?.lowercase() == key }
                if (idx >= 0) out[idx] = out[idx].copy(qty = line.qty, notes = line.notes.ifBlank { out[idx].notes })
            } else {
                out += line
                if (key != null) byMpn[key] = line
            }
        }
        return out
    }

    private fun deriveTitle(prompt: String): String {
        val cleaned = prompt.trim().replace('\n', ' ')
        if (cleaned.length <= 42) return cleaned.ifEmpty { "Untitled project" }
        return cleaned.take(39).trimEnd() + "…"
    }
}
