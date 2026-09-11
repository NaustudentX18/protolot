package app.protolot.build.ui.lots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import app.protolot.build.ui.theme.ProtoStub

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotsScreen(
    onImport: () -> Unit = {},
    importFocused: Boolean = false,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (importFocused) "Import lot" else "Lots")
                        Text(
                            if (importFocused) "CMP-LOT-IMPORT · stub" else "SCR-LOTS",
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
                    Text("Batch classroom / fleet variants", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Import a CSV or JSON lot to create multiple project variants and edit a global BOM. Real import + global edit ship in M1.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("CMP-LOT-MODE · Classroom / Fleet (M1)", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                    Text("CMP-GLOBAL-BOM-EDIT (M1)", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Canonical CSV headers (PRD v1.3)", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        ProtolotLocks.LOT_CSV_HEADER_LINE,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Exact headers: ${ProtolotLocks.LOT_CSV_HEADERS.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ProtoStub,
                    )
                }
            }
            Button(onClick = onImport, modifier = Modifier.fillMaxWidth()) {
                Text("Import CSV (stub)")
            }
            Button(onClick = onImport, modifier = Modifier.fillMaxWidth()) {
                Text("Import JSON (stub)")
            }
            Text(
                "ERR-LOTS-EMPTY / ERR-IMPORT-PARSE land with M1 import.",
                style = MaterialTheme.typography.labelSmall,
                color = ProtoStub,
            )
        }
    }
}
