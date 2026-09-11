package app.protolot.build.data

/**
 * Local stub project pack (M0). Real wiring/BOM/assembly land in M1–M2.
 * Shapes aligned to UX HANDOFF §5 (Eng owns final models).
 */
data class ProjectPack(
    val id: String,
    val title: String,
    val prompt: String,
    val boardClass: String?,
    val overview: String,
    val wiringStub: String,
    val bomStub: List<BomLineStub>,
    val assemblyStub: List<String>,
    val cadHooksStub: String,
    val firmwareNotesStub: String,
    val createdAtMs: Long,
)

data class BomLineStub(
    val ref: String,
    val mpn: String? = null,
    val qty: Int,
    val notes: String,
    val estUnitPriceLabel: String = "Est. unavailable",
    val confidence: Float? = null, // M2; null OK in M0/M1
    val vendorLinkHint: String = "", // link-out stub only through M2
    val overridden: Boolean = false,
) {
    val isLowConfidence: Boolean
        get() = confidence != null && confidence < ProtolotLocks.LOW_CONFIDENCE_THRESHOLD
}

data class BlockedGeneration(
    val prompt: String,
    val reason: String,
    val blockedAtMs: Long,
)
