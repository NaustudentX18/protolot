package app.protolot.build.data

/**
 * Spec PRD v1.3 locks (CoS).
 */
object ProtolotLocks {
    /** Confidence below this shows low-confidence chip + Override CTA. */
    const val LOW_CONFIDENCE_THRESHOLD = 0.6f

    /** After human override, local display confidence is bumped to this. */
    const val OVERRIDE_CONFIDENCE_BUMP = 0.85f

    /** Canonical lot CSV headers — exact order. */
    val LOT_CSV_HEADERS = listOf("name", "board", "variant", "mpn", "qty", "notes")

    const val LOT_CSV_HEADER_LINE = "name,board,variant,mpn,qty,notes"

    /**
     * Safety refuse is on-device rules (SafetyGate), not LLM-only.
     * Live/Hub stubs live under More only — not primary bottom nav.
     */
    const val SAFETY_ON_DEVICE = true

    const val VERSION_NAME = "0.3.0-m2"
    const val APPLICATION_ID = "app.protolot.build"
}
