package app.protolot.build.data

/**
 * Spec PRD v1.3 locks (CoS).
 * M0 embeds these constants; full confidence/lot UI lands M1–M2.
 */
object ProtolotLocks {
    /** Confidence below this shows low-confidence chip + Override CTA (M2). */
    const val LOW_CONFIDENCE_THRESHOLD = 0.6f

    /** Canonical lot CSV headers — exact order (M1 import). */
    val LOT_CSV_HEADERS = listOf("name", "board", "variant", "mpn", "qty", "notes")

    const val LOT_CSV_HEADER_LINE = "name,board,variant,mpn,qty,notes"

    /**
     * Safety refuse is on-device rules (SafetyGate), not LLM-only.
     * Live/Hub stubs live under More only — not primary bottom nav.
     */
    const val SAFETY_ON_DEVICE = true
}
