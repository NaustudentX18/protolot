package app.protolot.build.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import app.protolot.build.data.ProtolotLocks
import app.protolot.build.data.ProjectStore
import app.protolot.build.ui.theme.ProtoRefuse
import app.protolot.build.ui.theme.ProtoStub

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val blocked = ProjectStore.blockedLog()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Settings")
                        Text(
                            "SCR-SETTINGS",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProtoStub,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Safety policy · CMP-SAFETY-POLICY", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Protolot refuses to generate project packs whose primary purpose is " +
                            "weapons or explosives. The gate runs on-device (not LLM-only). " +
                            "Refusals show a hard dialog and are logged in-session — no partial weapon BOM.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Low-confidence threshold: ${ProtolotLocks.LOW_CONFIDENCE_THRESHOLD} (Override CTA)",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProtoStub,
                    )
                    if (blocked.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Blocked this session: ${blocked.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProtoRefuse,
                        )
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Export formats", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("• JSON project pack — default (offline-readable)", style = MaterialTheme.typography.bodyMedium)
                    Text("• CSV lot — headers: ${ProtolotLocks.LOT_CSV_HEADER_LINE}", style = MaterialTheme.typography.bodyMedium)
                    Text("• CAD (STEP/STL/GLB) — M4 hooks", style = MaterialTheme.typography.bodyMedium, color = ProtoStub)
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("License / about · CMP-ABOUT", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Protolot", style = MaterialTheme.typography.bodyLarge)
                    Text("applicationId: ${ProtolotLocks.APPLICATION_ID}", style = MaterialTheme.typography.bodyMedium)
                    Text("Brand: Copper Bench", style = MaterialTheme.typography.bodyMedium)
                    Text("Version: ${ProtolotLocks.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium)
                    Text("License: MIT", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Clean-room Android-first open source. Not affiliated with any closed hardware-AI or live-commerce brand.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ProtoStub,
                    )
                }
            }
        }
    }
}
