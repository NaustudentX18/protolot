package app.protolot.build.data

/**
 * Link-out stub URL templates only — no vendor API keys through M2 (CoS lock).
 */
object VendorLinks {
    fun digikey(mpn: String?): VendorLink {
        val q = (mpn ?: "").ifBlank { "hardware" }
        return VendorLink(
            providerId = "digikey",
            label = "DigiKey",
            url = "https://www.digikey.com/en/products/result?keywords=${java.net.URLEncoder.encode(q, "UTF-8")}",
        )
    }

    fun mouser(mpn: String?): VendorLink {
        val q = (mpn ?: "").ifBlank { "hardware" }
        return VendorLink(
            providerId = "mouser",
            label = "Mouser",
            url = "https://www.mouser.com/c/?q=${java.net.URLEncoder.encode(q, "UTF-8")}",
        )
    }

    fun lcsc(mpn: String?): VendorLink {
        val q = (mpn ?: "").ifBlank { "hardware" }
        return VendorLink(
            providerId = "lcsc",
            label = "LCSC",
            url = "https://www.lcsc.com/search?q=${java.net.URLEncoder.encode(q, "UTF-8")}",
        )
    }

    fun defaultsFor(mpn: String?): List<VendorLink> = listOf(digikey(mpn), mouser(mpn), lcsc(mpn))
}
