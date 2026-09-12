package app.protolot.build.data

/**
 * Structured stub packs for M1 when LLM is unconfigured or as fallback.
 * Wiring nets are non-empty (M1 gate).
 */
object StubPackFactory {
    const val CAD_HOOKS =
        "CAD exports land in M4. Project pack carries hooks only.\n" +
            "• Export STEP — Coming in M4\n" +
            "• Export STL / GLB — Coming in M4\n" +
            "• KiCad netlist — Coming in M4"

    fun defaultWiring(boardClass: String?): WiringGraph {
        val mcu = when (boardClass?.lowercase()) {
            "arduino" -> "Arduino"
            "pi" -> "RaspberryPi"
            else -> "ESP32"
        }
        return WiringGraph(
            nets = listOf(
                WiringNet(
                    name = "NET_3V3",
                    connections = listOf(
                        WiringConnection(mcu, "3V3", "REG1", "VOUT", "power rail"),
                        WiringConnection("REG1", "VOUT", "U2", "VDD", "sensor supply"),
                    ),
                ),
                WiringNet(
                    name = "NET_GND",
                    connections = listOf(
                        WiringConnection(mcu, "GND", "REG1", "GND", "star ground"),
                        WiringConnection(mcu, "GND", "U2", "GND", null),
                        WiringConnection(mcu, "GND", "J1", "GND", "header return"),
                    ),
                ),
                WiringNet(
                    name = "NET_I2C_SDA",
                    connections = listOf(
                        WiringConnection(mcu, "GPIO21", "U2", "SDA", "I2C data"),
                        WiringConnection(mcu, "GPIO21", "R1", "1", "4.7k pull-up to 3V3"),
                    ),
                ),
                WiringNet(
                    name = "NET_I2C_SCL",
                    connections = listOf(
                        WiringConnection(mcu, "GPIO22", "U2", "SCL", "I2C clock"),
                        WiringConnection(mcu, "GPIO22", "R2", "1", "4.7k pull-up to 3V3"),
                    ),
                ),
                WiringNet(
                    name = "NET_BTN",
                    connections = listOf(
                        WiringConnection(mcu, "GPIO0", "SW1", "A", "active-low button"),
                        WiringConnection("SW1", "B", mcu, "GND", null),
                    ),
                ),
            ),
        )
    }

    fun defaultBom(boardClass: String?): List<BomLine> {
        val mcuMpn = when (boardClass?.lowercase()) {
            "arduino" -> "A000066"
            "pi" -> "RPI4-MODB-4GB"
            else -> "ESP32-WROOM-32"
        }
        return listOf(
            BomLine(
                ref = "U1",
                mpn = mcuMpn,
                qty = 1,
                notes = "MCU / board",
                estUnitPriceLabel = "Est. \$4.50",
                estUnitPriceCents = 450,
                confidence = 0.82f,
                vendorLinks = VendorLinks.defaultsFor(mcuMpn),
            ),
            BomLine(
                ref = "U2",
                mpn = "BME280",
                qty = 1,
                notes = "Env sensor (I2C)",
                estUnitPriceLabel = "Est. \$3.20",
                estUnitPriceCents = 320,
                confidence = 0.74f,
                vendorLinks = VendorLinks.defaultsFor("BME280"),
            ),
            BomLine(
                ref = "R1–R2",
                mpn = "RC0603FR-074K7L",
                qty = 2,
                notes = "4.7k I2C pull-ups",
                estUnitPriceLabel = "Est. \$0.02",
                estUnitPriceCents = 2,
                confidence = 0.45f,
                vendorLinks = VendorLinks.defaultsFor("RC0603FR-074K7L"),
            ),
            BomLine(
                ref = "C1",
                mpn = "CL10B104KB8NNNC",
                qty = 1,
                notes = "100nF decoupling",
                estUnitPriceLabel = "Est. \$0.01",
                estUnitPriceCents = 1,
                confidence = 0.71f,
                vendorLinks = VendorLinks.defaultsFor("CL10B104KB8NNNC"),
            ),
            BomLine(
                ref = "SW1",
                mpn = null,
                qty = 1,
                notes = "Tactile button",
                estUnitPriceLabel = "Est. unavailable",
                confidence = 0.55f,
                vendorLinks = VendorLinks.defaultsFor("tactile switch"),
            ),
            BomLine(
                ref = "J1",
                mpn = null,
                qty = 1,
                notes = "2.54mm header",
                estUnitPriceLabel = "Est. unavailable",
                confidence = 0.68f,
                vendorLinks = VendorLinks.defaultsFor("pin header 2.54"),
            ),
        )
    }

    fun defaultAssembly(): List<AssemblyStep> = listOf(
        AssemblyStep("Gather parts from BOM", "Verify MPNs and qty against the estimate table.", listOf("U1", "U2")),
        AssemblyStep("Place MCU and decoupling", "Seat U1; add C1 close to VDD.", listOf("U1", "C1")),
        AssemblyStep("Wire power and ground", "Follow NET_3V3 and NET_GND before signal nets."),
        AssemblyStep("Connect I2C peripherals", "SDA/SCL + pull-ups R1/R2.", listOf("U2", "R1–R2")),
        AssemblyStep("Smoke-test power rail", "Check 3V3 before plugging sensors."),
    )

    fun defaultFirmware(boardClass: String?): String {
        val target = boardClass ?: "ESP32-class"
        return "Firmware notes (light):\n" +
            "• Target board: $target\n" +
            "• Bring-up: blink + I2C scan on SDA/SCL nets\n" +
            "• Deeper firmware assist is out of M2."
    }

    fun overview(title: String, prompt: String, boardClass: String?, source: String): String {
        val board = boardClass?.let { "Board class: $it\n" } ?: ""
        return "Project pack: $title\n" +
            board +
            "Generation: $source\n\n" +
            "Original prompt:\n\"${prompt.trim()}\"\n\n" +
            "Review Wiring + BOM (override low-confidence parts), walk Assembly checklist, optionally import a classroom lot, then Export JSON for offline use."
    }
}
