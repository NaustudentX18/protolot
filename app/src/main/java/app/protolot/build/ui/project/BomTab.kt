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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import app.protolot.build.data.ProjectStore
import app.protolot.build.data.ProtolotLocks
import app.protolot.build.ui.theme.ProtoSecondary
import app.protolot.build.ui.theme.ProtoStub
import app.protolot.build.ui.theme.ProtoTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BomTab(pack: ProjectPack, onPackChanged: () -> Unit) {
    val context = LocalContext.current
    val lowCount = pack.bom.count { it.isLowConfidence }
    val estTotalCents = pack.bom.sumOf { (it.estUnitPriceCents ?: 0) * it.qty }
    var overrideTarget by remember { mutableStateOf<BomLine?>(null) }
    var showEstSnack by remember { mutableStateOf(false) }

    if (overrideTarget != null) {
        PartOverrideSheet(
            line = overrideTarget!!,
            onDismiss = { overrideTarget = null },
            onApply = { mpn, notes, qty, extraUrl ->
                ProjectStore.overrideBomLine(
                    projectId = pack.id,
                    ref = overrideTarget!!.ref,
                    newMpn = mpn,
                    newNotes = notes,
                    newQty = qty,
                    extraVendorUrl = extraUrl,
                )
                overrideTarget = null
                onPackChanged()
            },
        )
    }

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
            if (showEstSnack) {
                Text(
                    "ERR-EST-STALE · Link & price are estimates — confirm on vendor site.",
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
        items(pack.bom, key = { it.ref }) { line ->
            BomRow(
                line = line,
                onOpenUrl = { url ->
                    showEstSnack = true
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                },
                onOverride = { overrideTarget = line },
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        if (estTotalCents > 0) "Estimate only · ~\$" + "%.2f".format(estTotalCents / 100.0)
                        else "Estimate only · Est. unavailable for some lines",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "Subtotal is an estimate — confirm on vendor sites. Bundle to kit from Kits tab.",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProtoStub,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            ProjectStore.createKitFromProject(pack.id)
                            onPackChanged()
                        },
                        enabled = pack.bom.isNotEmpty(),
                    ) { Text("Bundle to kit · CMP-KIT-FROM-BOM") }
                }
            }
        }
    }
}

@Composable
private fun BomRow(
    line: BomLine,
    onOpenUrl: (String) -> Unit,
    onOverride: () -> Unit,
) {
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
                    Text("Configure link templates in Providers.", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                }
            }
            if (line.isLowConfidence) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = onOverride,
                        label = {
                            Text(
                                "Low confidence ${(line.confidence!! * 100).toInt()}% (< ${(ProtolotLocks.LOW_CONFIDENCE_THRESHOLD * 100).toInt()}%)",
                            )
                        },
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onOverride) { Text("Override") }
                }
                Text("ERR-LOW-CONF · CMP-PART-OVERRIDE", style = MaterialTheme.typography.labelSmall, color = ProtoTertiary)
            } else if (line.confidence != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Confidence ${(line.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProtoSecondary,
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onOverride) { Text("Override") }
                }
            } else {
                TextButton(onClick = onOverride) { Text("Override part") }
            }
            if (line.overridden) {
                Text(
                    "Overridden locally · confidence bumped for display",
                    style = MaterialTheme.typography.labelSmall,
                    color = ProtoStub,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PartOverrideSheet(
    line: BomLine,
    onDismiss: () -> Unit,
    onApply: (mpn: String, notes: String, qty: Int?, extraUrl: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var mpn by remember { mutableStateOf(line.mpn.orEmpty()) }
    var notes by remember { mutableStateOf(line.notes) }
    var qtyText by remember { mutableStateOf(line.qty.toString()) }
    var extraUrl by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            Text("Override part · CMP-PART-OVERRIDE", style = MaterialTheme.typography.titleLarge)
            Text(
                "Ref ${line.ref} · swaps MPN/notes/qty, refreshes DigiKey/Mouser/LCSC link-outs, marks overridden.",
                style = MaterialTheme.typography.labelSmall,
                color = ProtoStub,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = mpn,
                onValueChange = { mpn = it },
                label = { Text("MPN") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = qtyText,
                onValueChange = { qtyText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Qty") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = extraUrl,
                onValueChange = { extraUrl = it },
                label = { Text("Optional vendor URL (https…)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Prices & links stay estimates after override — never verified checkout.",
                style = MaterialTheme.typography.labelSmall,
                color = ProtoTertiary,
            )
            Spacer(Modifier.height(16.dp))
            Row {
                Button(
                    onClick = {
                        onApply(mpn, notes, qtyText.toIntOrNull(), extraUrl)
                    },
                ) { Text("Apply override") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    }
}
