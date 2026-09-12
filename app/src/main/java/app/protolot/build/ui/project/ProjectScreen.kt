package app.protolot.build.ui.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.protolot.build.data.BomLineStub
import app.protolot.build.data.ProtolotLocks
import app.protolot.build.data.ProjectPack
import app.protolot.build.data.ProjectStore
import app.protolot.build.ui.theme.ProtoStub
import app.protolot.build.ui.theme.ProtoTertiary

private val tabs = listOf("Overview", "Wiring", "BOM", "Assembly", "CAD")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectScreen(
    projectId: String,
    initialTab: String,
    onBack: () -> Unit,
) {
    val pack = remember(projectId) { ProjectStore.get(projectId) }
    var selected by remember {
        mutableIntStateOf(
            when (initialTab.lowercase()) {
                "wiring" -> 1
                "bom" -> 2
                "assembly" -> 3
                "cad" -> 4
                else -> 0
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(pack?.title ?: "Missing project")
                        Text(
                            "SCR-PROJECT · stub",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProtoStub,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (pack == null) {
            Column(Modifier.padding(padding).padding(16.dp)) {
                Text("Project not found.", style = MaterialTheme.typography.bodyLarge)
                Text("Return home and generate a stub pack.", color = ProtoStub)
            }
            return@Scaffold
        }

        Column(Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selected) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = selected == index,
                        onClick = { selected = index },
                        text = { Text(label) },
                    )
                }
            }
            when (selected) {
                0 -> OverviewTab(pack)
                1 -> StubTextTab("Wiring · CMP-WIRING-VIEW", pack.wiringStub)
                2 -> BomTab(pack)
                3 -> AssemblyTab(pack)
                4 -> StubTextTab("CAD hooks", pack.cadHooksStub)
            }
        }
    }
}

@Composable
private fun OverviewTab(pack: ProjectPack) {
    LazyColumn(
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
                    Text("Overview", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(pack.overview, style = MaterialTheme.typography.bodyMedium)
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
                    Text("Firmware notes · CMP-FIRMWARE-NOTES", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(pack.firmwareNotesStub, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun StubTextTab(title: String, body: String) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "M0 stub — real viewer in later milestones",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProtoStub,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(body, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun BomTab(pack: ProjectPack) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text("BOM · CMP-BOM-TABLE", style = MaterialTheme.typography.titleMedium)
            // CMP-BOM-ESTIMATE-DISCLAIMER
            Text(
                "Prices & vendor links are estimates; verify before buy. Override available when confidence < ${ProtolotLocks.LOW_CONFIDENCE_THRESHOLD}.",
                style = MaterialTheme.typography.labelSmall,
                color = ProtoStub,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Estimate only · CMP-BOM-EST-TOTAL (stub)",
                style = MaterialTheme.typography.labelSmall,
                color = ProtoStub,
            )
        }
        items(pack.bomStub) { line -> BomRow(line) }
    }
}

@Composable
private fun BomRow(line: BomLineStub) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${line.ref}  ×${line.qty}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text(line.estUnitPriceLabel, style = MaterialTheme.typography.labelSmall, color = ProtoStub)
            }
            line.mpn?.let {
                Text("MPN: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Text(line.notes, style = MaterialTheme.typography.bodyMedium)
            if (line.vendorLinkHint.isNotBlank()) {
                Text(line.vendorLinkHint, style = MaterialTheme.typography.labelSmall, color = ProtoStub)
            }
            // v1.3: confidence < 0.6 → low-confidence + Override CTA
            if (line.isLowConfidence) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                "Low confidence ${(line.confidence!! * 100).toInt()}% (< ${(ProtolotLocks.LOW_CONFIDENCE_THRESHOLD * 100).toInt()}%)",
                            )
                        },
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { /* CMP-PART-OVERRIDE stub — M2 */ }) {
                        Text("Override")
                    }
                }
                Text(
                    "ERR-LOW-CONF · CMP-PART-OVERRIDE (M2 full flow)",
                    style = MaterialTheme.typography.labelSmall,
                    color = ProtoTertiary,
                )
            } else if (line.confidence != null) {
                Text(
                    "Confidence ${(line.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = ProtoStub,
                )
            }
        }
    }
}

@Composable
private fun AssemblyTab(pack: ProjectPack) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text("Assembly · CMP-ASSEMBLY-LIST", style = MaterialTheme.typography.titleMedium)
            Text(
                "Checklist interaction lands in M2.",
                style = MaterialTheme.typography.labelSmall,
                color = ProtoStub,
            )
            Spacer(Modifier.height(8.dp))
        }
        items(pack.assemblyStub.mapIndexed { i, s -> (i + 1) to s }) { (n, step) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Text(
                    "$n. $step",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
