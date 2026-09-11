package app.protolot.build.ui.providers

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.protolot.build.ui.theme.ProtoStub

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen() {
    var llmEndpoint by remember { mutableStateOf("") }
    var llmKey by remember { mutableStateOf("") }
    var llmModel by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Providers")
                        Text(
                            "SCR-PROVIDERS · BYOK stubs",
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
                    Text("LLM provider (BYOK)", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "OpenAI-compatible endpoint. Wired in M1; fields are UI stubs in M0. No wallet / credit chrome.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ProtoStub,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = llmEndpoint,
                        onValueChange = { llmEndpoint = it },
                        label = { Text("Base URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = llmKey,
                        onValueChange = { llmKey = it },
                        label = { Text("API key (local stub)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = llmModel,
                        onValueChange = { llmModel = it },
                        label = { Text("Model") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Parts catalogs", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Through M2: DigiKey / Mouser / LCSC-class link-out stubs only. " +
                            "No vendor API keys. Live catalog APIs not required through M2.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("• DigiKey — Link-out stub", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                    Text("• Mouser — Link-out stub", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                    Text("• LCSC — Link-out stub", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                }
            }
            Text(
                "Safety refuse is on-device rules (SafetyGate), not LLM-only.",
                style = MaterialTheme.typography.labelSmall,
                color = ProtoStub,
            )
        }
    }
}
