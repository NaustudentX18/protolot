package app.protolot.build.ui.live

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
fun LiveScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Live")
                        Text(
                            "SCR-LIVE · under More only",
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
                        "BIN + simple auction + flash for kits/parts land after the M2 gate. " +
                            "This screen is an early stub only and is not required to ship M0–M2. " +
                            "Hosted under More — not a primary bottom-nav slot (PRD v1.3).",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("CMP-BIN — stub", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                    Text("CMP-AUCTION — stub", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                }
            }
        }
    }
}
