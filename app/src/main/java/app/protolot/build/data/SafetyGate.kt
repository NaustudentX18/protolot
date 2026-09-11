package app.protolot.build.data

/**
 * On-device safety refuse (PRD v1.3 lock — NOT LLM-only).
 * Blocks prompts whose primary purpose is weapons or explosives.
 * Heuristic keyword gate for M0; runs locally before any generation.
 */
object SafetyGate {

    private val weaponPrimary = listOf(
        "weapon", "weapons", "firearm", "firearms", "gun", "guns", "rifle", "pistol",
        "ammo", "ammunition", "bomb", "bombs", "explosive", "explosives",
        "grenade", "landmine", "land mine", "ied", "missile", "rocket launcher",
        "how to make a bomb", "build a gun", "3d printed gun", "ghost gun",
        "pipe bomb", "molotov", "napalm", "c4 explosive", "detonator for weapon",
    )

    data class Verdict(val allowed: Boolean, val reason: String = "")

    fun evaluate(prompt: String): Verdict {
        // On-device only — never defer this gate to an LLM.
        check(ProtolotLocks.SAFETY_ON_DEVICE)
        val normalized = prompt.lowercase().trim()
        if (normalized.isEmpty()) {
            return Verdict(allowed = false, reason = "Prompt is empty.")
        }
        for (term in weaponPrimary) {
            if (normalized.contains(term)) {
                return Verdict(
                    allowed = false,
                    reason = "Protolot refuses designs whose primary purpose is weapons or explosives. " +
                        "Matched on-device safety heuristic: \"$term\". No project pack was generated.",
                )
            }
        }
        return Verdict(allowed = true)
    }
}
