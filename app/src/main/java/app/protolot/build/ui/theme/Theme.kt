package app.protolot.build.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/** Copper Bench dark ColorScheme — Material 3 seed mapping. */
private val CopperBenchScheme = darkColorScheme(
    primary = ProtoPrimary,
    onPrimary = ProtoOnPrimary,
    primaryContainer = ProtoPrimaryContainer,
    onPrimaryContainer = ProtoPrimary,
    secondary = ProtoSecondary,
    onSecondary = ProtoOnSecondary,
    tertiary = ProtoTertiary,
    onTertiary = ProtoOnTertiary,
    background = ProtoBackground,
    onBackground = ProtoOnSurface,
    surface = ProtoSurface,
    onSurface = ProtoOnSurface,
    surfaceVariant = ProtoSurfaceVariant,
    onSurfaceVariant = ProtoOnSurfaceVariant,
    outline = ProtoOutline,
    error = ProtoError,
    onError = ProtoOnError,
)

@Composable
fun ProtolotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CopperBenchScheme,
        typography = ProtolotTypography,
        content = content,
    )
}
