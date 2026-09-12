package app.protolot.build.ui.providers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.protolot.build.data.LlmGenerator
import app.protolot.build.data.ProviderStore
import app.protolot.build.ui.theme.ProtoRefuse
import app.protolot.build.ui.theme.ProtoSecondary
import app.protolot.build.ui.theme.ProtoStub
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen() {
    val initial = ProviderStore.llm
    var llmEndpoint by remember { mutableStateOf(initial.baseUrl) }
    var llmKey by remember { mutableStateOf(initial.apiKey) }
    var llmModel by remember { mutableStateOf(initial.model) }
    var status by remember { mutableStateOf<String?>(null) }
    var statusOk by remember { mutableStateOf<Boolean?>(null) }
    var testing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Providers")
                        Text(
                            "SCR-PROVIDERS · BYOK",
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
                    Text("LLM provider (BYOK) · CMP-LLM-PROVIDER", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "OpenAI-compatible endpoint. Keys stay on-device. Unkeyed → stub generation still works.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ProtoStub,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = llmEndpoint,
                        onValueChange = { llmEndpoint = it },
                        label = { Text("Base URL (e.g. https://api.openai.com/v1)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = llmKey,
                        onValueChange = { llmKey = it },
                        label = { Text("API key") },
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
                    Spacer(Modifier.height(12.dp))
                    Row {
                        Button(onClick = {
                            ProviderStore.saveLlm(llmEndpoint, llmKey, llmModel)
                            status = if (ProviderStore.llm.isConfigured) {
                                "Saved. Generation will use BYOK."
                            } else {
                                "Saved incomplete config — stub generation remains active."
                            }
                            statusOk = ProviderStore.llm.isConfigured
                        }) { Text("Save") }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                testing = true
                                ProviderStore.saveLlm(llmEndpoint, llmKey, llmModel)
                                scope.launch {
                                    val (ok, msg) = withContext(Dispatchers.IO) {
                                        LlmGenerator.testConnectionSync()
                                    }
                                    testing = false
                                    statusOk = ok
                                    status = msg
                                }
                            },
                            enabled = !testing,
                        ) { Text(if (testing) "Testing…" else "Test connection") }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = {
                            ProviderStore.clearLlm()
                            llmEndpoint = ""; llmKey = ""; llmModel = ""
                            status = "Cleared — stub generation."
                            statusOk = null
                        }) { Text("Clear") }
                    }
                    status?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when (statusOk) {
                                true -> ProtoSecondary
                                false -> ProtoRefuse
                                null -> ProtoStub
                            },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "CMP-PROVIDER-STATUS · ${if (ProviderStore.llm.isConfigured) "Keyed" else "Unkeyed / stub"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProtoStub,
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
                    Text("Parts catalogs · CMP-PARTS-PROVIDER", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Through M2: DigiKey / Mouser / LCSC-class link-out stubs only. " +
                            "No vendor API keys. Live catalog APIs not required through M2.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("• DigiKey — Link-out stub · digikey.com search URL", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                    Text("• Mouser — Link-out stub · mouser.com search URL", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                    Text("• LCSC — Link-out stub · lcsc.com search URL", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "BOM rows open ≥2 external Search link (estimate) buttons (DigiKey + Mouser; LCSC also). No vendor API keys.",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProtoStub,
                    )
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
