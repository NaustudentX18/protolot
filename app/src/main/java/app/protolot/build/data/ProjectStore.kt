package app.protolot.build.data

import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

/**
 * In-memory local stub store for M0. Persisted storage arrives later.
 */
object ProjectStore {
    private val projects = CopyOnWriteArrayList<ProjectPack>()
    private val blocked = CopyOnWriteArrayList<BlockedGeneration>()

    fun all(): List<ProjectPack> = projects.toList().sortedByDescending { it.createdAtMs }

    fun get(id: String): ProjectPack? = projects.find { it.id == id }

    fun blockedLog(): List<BlockedGeneration> = blocked.toList().sortedByDescending { it.blockedAtMs }

    fun recordBlocked(prompt: String, reason: String) {
        blocked.add(
            BlockedGeneration(
                prompt = prompt,
                reason = reason,
                blockedAtMs = System.currentTimeMillis(),
            ),
        )
    }

    fun createFromPrompt(prompt: String, boardClass: String? = null): ProjectPack {
        val id = UUID.randomUUID().toString().take(8)
        val title = deriveTitle(prompt)
        val pack = ProjectPack(
            id = id,
            title = title,
            prompt = prompt.trim(),
            boardClass = boardClass,
            overview = buildOverview(title, prompt, boardClass),
            wiringStub = "Stub net list (M0):\n" +
                "• MCU ↔ sensor bus (I2C)\n" +
                "• Power rail 3V3 → MCU, peripherals\n" +
                "• GND star near regulator\n" +
                "(Real wiring viewer arrives in M1.)",
            bomStub = listOf(
                BomLineStub(
                    ref = "U1",
                    mpn = "ESP32-WROOM-32",
                    qty = 1,
                    notes = "MCU — stub placeholder",
                    estUnitPriceLabel = "Estimate only",
                    confidence = 0.82f,
                    vendorLinkHint = "Search link (estimate) — DigiKey/Mouser/LCSC",
                ),
                BomLineStub(
                    ref = "R1–R4",
                    mpn = null,
                    qty = 4,
                    notes = "10k pull-ups — stub",
                    estUnitPriceLabel = "Est. unavailable",
                    confidence = 0.45f, // below 0.6 → low-confidence + Override CTA (M2 UI)
                    vendorLinkHint = "Search link (estimate)",
                ),
                BomLineStub(
                    ref = "C1",
                    qty = 1,
                    notes = "100nF decoupling — stub",
                    confidence = 0.71f,
                    vendorLinkHint = "link-out stub",
                ),
                BomLineStub(
                    ref = "J1",
                    qty = 1,
                    notes = "Header / connector — stub",
                    confidence = 0.55f,
                    vendorLinkHint = "link-out stub",
                ),
            ),
            assemblyStub = listOf(
                "Gather parts from BOM stub",
                "Place MCU and decoupling (stub step)",
                "Wire power and ground (stub step)",
                "Connect peripherals per wiring stub",
                "Smoke-test power rail (stub step)",
            ),
            cadHooksStub = "CAD hooks (M0 placeholder):\n" +
                "• STEP / STL / GLB export paths — M4\n" +
                "• KiCad netlist export — M4\n" +
                "No CAD files generated in M0.",
            firmwareNotesStub = "Firmware notes (light stub):\n" +
                "• Target: Arduino-compatible / bare-metal TBD\n" +
                "• Bring-up: blink + I2C scan\n" +
                "(Deeper firmware assist is out of M0.)",
            createdAtMs = System.currentTimeMillis(),
        )
        projects.add(0, pack)
        return pack
    }

    private fun deriveTitle(prompt: String): String {
        val cleaned = prompt.trim().replace('\n', ' ')
        if (cleaned.length <= 42) return cleaned.ifEmpty { "Untitled project" }
        return cleaned.take(39).trimEnd() + "…"
    }

    private fun buildOverview(title: String, prompt: String, boardClass: String?): String {
        val board = boardClass?.let { "Board class: $it\n" } ?: ""
        return "Stub project pack for: $title\n" +
            board +
            "\nOriginal prompt:\n\"${prompt.trim()}\"\n\n" +
            "This is M0 fake/generated stub content so navigation and " +
            "the prompt → Overview flow can be exercised. Real LLM generation " +
            "and structured packs arrive in M1."
    }
}
