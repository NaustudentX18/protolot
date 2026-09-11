package app.protolot.build.ui.makerhub

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
fun MakerHubScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Maker Hub")
                        Text(
                            "SCR-MAKER-HUB · under More only",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProtoInfo,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ProtoInfo.copy(alpha = 0.15f)),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Deferred until Spec confirms M3 milestone",
                        style = MaterialTheme.typography.titleMedium,
                        color = ProtoInfo,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Orders, labels, and earnings lite are M3. Stub only — not an M0–M2 ship-gate. " +
                            "Hosted under More (PRD v1.3).",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text("No wallet / credit chrome.", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                }
            }
        }
    }
}
