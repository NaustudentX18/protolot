package app.protolot.build.navigation

/**
 * Nav routes from UX HANDOFF §3.
 * home, project/{id}, lots, kits, providers, settings, live, maker_hub
 */
object ProtolotDestinations {
    const val HOME = "home"
    const val PROJECT = "project/{projectId}?tab={tab}"
    const val LOTS = "lots"
    const val LOTS_IMPORT = "lots/import"
    const val KITS = "kits"
    const val PROVIDERS = "providers"
    const val SETTINGS = "settings"
    const val LIVE = "live"
    const val MAKER_HUB = "maker_hub"
    const val MORE = "more"

    fun project(projectId: String, tab: String = "overview"): String =
        "project/$projectId?tab=$tab"
}
