package app.protolot.build.ui.project

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.protolot.build.data.ProjectPack
import app.protolot.build.data.WiringNet
import app.protolot.build.ui.theme.ProtoSecondary
import app.protolot.build.ui.theme.ProtoStub
import app.protolot.build.ui.theme.ProtoTertiary

@Composable
internal fun OverviewTab(pack: ProjectPack) {
    val doneAsm = pack.assembly.count { it.checked }
    val totalAsm = pack.assembly.size
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                AssistChip(onClick = {}, label = { Text("Wiring ✓") })
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = {}, label = { Text("BOM ✓") })
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = {}, label = { Text("${pack.bom.size} parts") })
                Spacer(Modifier.width(8.dp))
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            if (totalAsm == 0) "Assembly —"
                            else "Assembly $doneAsm/$totalAsm",
                        )
                    },
                )
                if (pack.lotId != null) {
                    Spacer(Modifier.width(8.dp))
                    AssistChip(onClick = {}, label = { Text("Lot ${pack.variantLabel ?: pack.lotId}") })
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Firmware notes · CMP-FIRMWARE-NOTES", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(pack.firmwareNotes, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
internal fun WiringTab(pack: ProjectPack) {
    var selectedNet by remember { mutableStateOf<WiringNet?>(null) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("Wiring · CMP-WIRING-VIEW", style = MaterialTheme.typography.titleMedium)
            Text("Structured net list — pin → net → pin", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                pack.wiring.nets.forEach { net ->
                    FilterChip(
                        selected = selectedNet == net,
                        onClick = { selectedNet = if (selectedNet == net) null else net },
                        label = { Text(net.name) },
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
            }
        }
        val nets = selectedNet?.let { listOf(it) } ?: pack.wiring.nets
        items(nets) { net ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(net.name, style = MaterialTheme.typography.titleMedium, color = ProtoSecondary)
                    Spacer(Modifier.height(6.dp))
                    net.connections.forEach { c ->
                        Text(
                            "${c.fromComponent}.${c.fromPin}  →  ${c.toComponent}.${c.toPin}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        c.notes?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = ProtoStub) }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
        if (pack.wiring.nets.isEmpty()) {
            item { Text("No nets — regenerate pack.", color = ProtoTertiary) }
        }
    }
}

@Composable
internal fun CadTab(pack: ProjectPack) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("CAD hooks", style = MaterialTheme.typography.titleMedium)
                    Text("Coming in M4", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                    Spacer(Modifier.height(8.dp))
                    Text(pack.cadHooks, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = {}, enabled = false) { Text("Export STEP — Coming in M4") }
                    OutlinedButton(onClick = {}, enabled = false) { Text("Export STL / GLB — Coming in M4") }
                    OutlinedButton(onClick = {}, enabled = false) { Text("KiCad netlist — Coming in M4") }
                }
            }
        }
    }
}
