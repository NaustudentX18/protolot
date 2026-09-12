package app.protolot.build.data

/**
 * M1 project pack — structured wiring + BOM + exportable JSON.
 * Shapes aligned to UX HANDOFF §5.
 */
data class ProjectPack(
    val id: String,
    val title: String,
    val prompt: String,
    val boardClass: String?,
    val overview: String,
    val wiring: WiringGraph,
    val bom: List<BomLine>,
    val assembly: List<AssemblyStep>,
    val cadHooks: String,
    val firmwareNotes: String,
    val createdAtMs: Long,
    val lotId: String? = null,
    val variantLabel: String? = null,
    val generationSource: String = "stub", // stub | llm
) {
    /** Backward-compat aliases used by older stub UI. */
    val wiringStub: String get() = wiring.toReadable()
    val bomStub: List<BomLine> get() = bom
    val assemblyStub: List<String> get() = assembly.map { it.title }
    val cadHooksStub: String get() = cadHooks
    val firmwareNotesStub: String get() = firmwareNotes
}

data class WiringGraph(
    val nets: List<WiringNet>,
) {
    fun toReadable(): String = buildString {
        appendLine("Net list (${nets.size} nets):")
        nets.forEach { net ->
            appendLine("• ${net.name}")
            net.connections.forEach { c ->
                appendLine("    ${c.fromComponent}.${c.fromPin} → ${c.toComponent}.${c.toPin}${c.notes?.let { " ($it)" } ?: ""}")
            }
        }
    }
}

data class WiringNet(
    val name: String,
    val connections: List<WiringConnection>,
)

data class WiringConnection(
    val fromComponent: String,
    val fromPin: String,
    val toComponent: String,
    val toPin: String,
    val notes: String? = null,
)

data class BomLine(
    val ref: String,
    val mpn: String? = null,
    val qty: Int,
    val notes: String,
    val estUnitPriceLabel: String = "Est. unavailable",
    val estUnitPriceCents: Int? = null,
    val confidence: Float? = null,
    val vendorLinks: List<VendorLink> = emptyList(),
    val overridden: Boolean = false,
) {
    val isLowConfidence: Boolean
        get() = confidence != null && confidence < ProtolotLocks.LOW_CONFIDENCE_THRESHOLD

    /** M0 alias */
    val vendorLinkHint: String
        get() = if (vendorLinks.isEmpty()) {
            "Search link (estimate)"
        } else {
            vendorLinks.joinToString(" · ") { "${it.label} (estimate)" }
        }
}

/** Alias kept for any leftover imports */
typealias BomLineStub = BomLine

data class VendorLink(
    val providerId: String,
    val label: String,
    val url: String,
)

data class AssemblyStep(
    val title: String,
    val body: String = "",
    val partRefs: List<String> = emptyList(),
    val checked: Boolean = false,
)

data class BlockedGeneration(
    val prompt: String,
    val reason: String,
    val blockedAtMs: Long,
)

enum class LotMode { SINGLE, CLASSROOM, FLEET }

data class Lot(
    val id: String,
    val name: String,
    val mode: LotMode,
    val memberProjectIds: List<String>,
    val createdAtMs: Long,
    val sourceLabel: String = "import",
) {
    val batchCount: Int get() = memberProjectIds.size
    val modeLabel: String
        get() = when (mode) {
            LotMode.SINGLE -> "Single"
            LotMode.CLASSROOM -> "Classroom · ${batchCount} student variants"
            LotMode.FLEET -> "Fleet · ${batchCount} device units"
        }
}

data class LotImportRow(
    val name: String,
    val board: String,
    val variant: String,
    val mpn: String,
    val qty: Int,
    val notes: String,
    val rowNumber: Int,
)

sealed class LotImportResult {
    data class Success(val lot: Lot, val createdIds: List<String>, val warnings: List<String> = emptyList()) : LotImportResult()
    data class Error(val errors: List<ImportError>) : LotImportResult()
}

data class ImportError(
    val code: String,
    val message: String,
    val row: Int? = null,
)

/** Kit shell (M2) — commerce depth deferred to M3. */
data class Kit(
    val id: String,
    val name: String,
    val sourceProjectId: String,
    val sourceProjectTitle: String,
    val partCount: Int,
    val bomSnapshot: List<BomLine>,
    val createdAtMs: Long,
) {
    val rolledQty: Int get() = bomSnapshot.sumOf { it.qty }
}
