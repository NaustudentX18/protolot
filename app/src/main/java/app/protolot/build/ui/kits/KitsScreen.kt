package app.protolot.build.ui.kits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.protolot.build.data.Kit
import app.protolot.build.data.ProjectStore
import app.protolot.build.ui.theme.ProtoSecondary
import app.protolot.build.ui.theme.ProtoStub
import app.protolot.build.ui.theme.ProtoTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitsScreen() {
    var revision by remember { mutableIntStateOf(0) }
    val kits = remember(revision) { ProjectStore.allKits() }
    val projects = remember(revision) { ProjectStore.all() }
    var previewKitId by remember { mutableStateOf<String?>(null) }
    val preview = previewKitId?.let { id -> kits.find { it.id == id } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kits")
                        Text(
                            "SCR-KITS · shell (commerce M3)",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProtoStub,
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Kit bundling shell · CMP-KIT-LIST", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Bundle a project BOM into a kit preview. Pricing, stock CSV, BIN, and live drops are M3 — not ship-gate.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Create from project BOM · CMP-KIT-FROM-BOM", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        if (projects.isEmpty()) {
                            Text(
                                "ERR-KITS-EMPTY · Generate a project pack first, then bundle its BOM here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ProtoTertiary,
                            )
                        } else {
                            val latest = projects.first()
                            Text(
                                "Latest project: ${latest.title} · ${latest.bom.size} BOM lines",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val kit = ProjectStore.createKitFromProject(latest.id)
                                    if (kit != null) previewKitId = kit.id
                                    revision += 1
                                },
                                enabled = latest.bom.isNotEmpty(),
                            ) { Text("Create kit from latest BOM") }
                        }
                    }
                }
            }

            if (kits.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("No kits yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "ERR-KITS-EMPTY · Bundle a BOM into a kit from a project or use Create above.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ProtoStub,
                            )
                        }
                    }
                }
            } else {
                items(kits, key = { it.id }) { kit ->
                    KitCard(
                        kit = kit,
                        selected = previewKitId == kit.id,
                        onSelect = { previewKitId = kit.id },
                    )
                }
            }

            if (preview != null) {
                item {
                    BundlePreview(preview)
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Commerce / live drop", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Kit commerce & live drops — M3. Pricing, stock, BIN, and auction are deferred. Live/Hub remain under More only.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ProtoStub,
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = {}, enabled = false) {
                            Text("List kit on Live Build — Coming in M3")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KitCard(kit: Kit, selected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        onClick = onSelect,
    ) {
        Column(Modifier.padding(16.dp)) {
            Row {
                Text(kit.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                if (selected) {
                    Text("Preview", style = MaterialTheme.typography.labelSmall, color = ProtoSecondary)
                }
            }
            Text(
                "Source: ${kit.sourceProjectTitle} · ${kit.partCount} lines · ${kit.rolledQty} pcs rolled",
                style = MaterialTheme.typography.bodyMedium,
                color = ProtoStub,
            )
        }
    }
}

@Composable
private fun BundlePreview(kit: Kit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Bundle preview · CMP-KIT-BUNDLE", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            kit.bomSnapshot.forEach { line ->
                Text(
                    "${line.ref} ×${line.qty} · ${line.mpn ?: "(no MPN)"} — ${line.notes}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Estimates only — kit pricing/stock CSV lands in M3.",
                style = MaterialTheme.typography.labelSmall,
                color = ProtoTertiary,
            )
        }
    }
}
