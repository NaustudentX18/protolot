package app.protolot.build.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.protolot.build.ui.theme.ProtoInfo
import app.protolot.build.ui.theme.ProtoStub

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onProviders: () -> Unit,
    onSettings: () -> Unit,
    onLive: () -> Unit,
    onMakerHub: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("More")
                        Text(
                            "Providers · Settings · M3 stubs",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProtoStub,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            MoreRow("Providers", "SCR-PROVIDERS · BYOK + link-out stubs", onProviders)
            MoreRow("Settings", "SCR-SETTINGS · safety · MIT", onSettings)
            MoreRow(
                "Live",
                "SCR-LIVE · Deferred until Spec confirms M3 milestone",
                onLive,
                accent = true,
            )
            MoreRow(
                "Maker Hub",
                "SCR-MAKER-HUB · Deferred until Spec confirms M3 milestone",
                onMakerHub,
                accent = true,
            )
        }
    }
}

@Composable
private fun MoreRow(title: String, subtitle: String, onClick: () -> Unit, accent: Boolean = false) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = if (accent) ProtoInfo else MaterialTheme.colorScheme.onSurface,
            )
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = ProtoStub)
        }
    }
}
