package app.protolot.build.ui.project

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.protolot.build.data.BomLine
import app.protolot.build.data.ProjectPack
import app.protolot.build.data.ProtolotLocks
import app.protolot.build.data.WiringNet
import app.protolot.build.ui.theme.ProtoSecondary
import app.protolot.build.ui.theme.ProtoStub
import app.protolot.build.ui.theme.ProtoTertiary

@Composable
internal fun OverviewTab(pack: ProjectPack) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                AssistChip(onClick = {}, label = { Text("Wiring ✓") })
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = {}, label = { Text("BOM ✓") })
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = {}, label = { Text("${pack.bom.size} parts") })
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
            Text("Structured net list — pin → net → pin (M1)", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
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
internal fun BomTab(pack: ProjectPack) {
    val context = LocalContext.current
    val lowCount = pack.bom.count { it.isLowConfidence }
    val estTotalCents = pack.bom.sumOf { (it.estUnitPriceCents ?: 0) * it.qty }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("BOM · CMP-BOM-TABLE", style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Text(
                    "Prices and vendor links are estimates — verify before you buy. " +
                        "Wrong part, stale price, or bad link can happen; use Override. " +
                        "Never treated as verified checkout.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = ProtoTertiary,
                )
            }
            if (lowCount > 0) {
                Text(
                    "ERR-LOW-CONF · $lowCount part(s) need review (confidence < ${ProtolotLocks.LOW_CONFIDENCE_THRESHOLD})",
                    style = MaterialTheme.typography.labelSmall,
                    color = ProtoTertiary,
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Text("Ref", Modifier.width(64.dp), style = MaterialTheme.typography.labelSmall)
                Text("Qty", Modifier.width(40.dp), style = MaterialTheme.typography.labelSmall)
                Text("Notes / MPN", Modifier.width(160.dp), style = MaterialTheme.typography.labelSmall)
                Text("Vendor", Modifier.width(120.dp), style = MaterialTheme.typography.labelSmall)
            }
        }
        items(pack.bom) { line ->
            BomRow(line) { url -> runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        if (estTotalCents > 0) "Estimate only · ~\$" + (estTotalCents / 100.0)
                        else "Estimate only · Est. unavailable for some lines",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text("Subtotal is an estimate — confirm on vendor sites.", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                }
            }
        }
    }
}

@Composable
private fun BomRow(line: BomLine, onOpenUrl: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${line.ref}  ×${line.qty}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text(line.estUnitPriceLabel, style = MaterialTheme.typography.labelSmall, color = ProtoStub)
            }
            line.mpn?.let { Text("MPN: $it", style = MaterialTheme.typography.bodyMedium) }
            Text(line.notes, style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                line.vendorLinks.forEach { v ->
                    OutlinedButton(onClick = { onOpenUrl(v.url) }, modifier = Modifier.padding(end = 6.dp)) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.height(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${v.label} · Search link (estimate)")
                    }
                }
                if (line.vendorLinks.isEmpty()) {
                    Text("Link-out stubs via Providers.", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                }
            }
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
                    OutlinedButton(onClick = { }) { Text("Override") }
                }
                Text("ERR-LOW-CONF · CMP-PART-OVERRIDE (M1 stub; full M2)", style = MaterialTheme.typography.labelSmall, color = ProtoTertiary)
            } else if (line.confidence != null) {
                Text("Confidence ${(line.confidence * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = ProtoSecondary)
            }
            if (line.overridden) {
                Text("Overridden locally", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
            }
        }
    }
}

@Composable
internal fun AssemblyTab(pack: ProjectPack) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("Assembly · CMP-ASSEMBLY-LIST", style = MaterialTheme.typography.titleMedium)
            Text("Ordered steps in M1; checklist check-off in M2.", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
            Spacer(Modifier.height(8.dp))
        }
        items(pack.assembly.mapIndexed { i, s -> (i + 1) to s }) { (n, step) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("$n. ${step.title}", style = MaterialTheme.typography.bodyLarge)
                    if (step.body.isNotBlank()) {
                        Text(step.body, style = MaterialTheme.typography.bodyMedium, color = ProtoStub)
                    }
                    if (step.partRefs.isNotEmpty()) {
                        Text("Parts: ${step.partRefs.joinToString(", ")}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
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
