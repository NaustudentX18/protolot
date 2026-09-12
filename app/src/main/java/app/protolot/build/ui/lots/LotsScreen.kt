package app.protolot.build.ui.lots

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import app.protolot.build.data.Lot
import app.protolot.build.data.LotImportResult
import app.protolot.build.data.LotImporter
import app.protolot.build.data.LotMode
import app.protolot.build.data.ProjectStore
import app.protolot.build.data.ProtolotLocks
import app.protolot.build.ui.theme.ProtoRefuse
import app.protolot.build.ui.theme.ProtoSecondary
import app.protolot.build.ui.theme.ProtoStub

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotsScreen(
    onImport: () -> Unit = {},
    importFocused: Boolean = false,
    onOpenProject: (String) -> Unit = {},
) {
    var refresh by remember { mutableStateOf(0) }
    val lots = remember(refresh) { ProjectStore.allLots() }
    var modeFilter by remember { mutableStateOf<LotMode?>(null) }
    var pasteText by remember { mutableStateOf("") }
    var importErrors by remember { mutableStateOf<List<String>>(emptyList()) }
    var importOk by remember { mutableStateOf<String?>(null) }
    var showImport by remember { mutableStateOf(importFocused) }
    var selectedLotId by remember { mutableStateOf<String?>(null) }
    var selectedMembers by remember { mutableStateOf(setOf<String>()) }
    var editField by remember { mutableStateOf("mpn") }
    var editValue by remember { mutableStateOf("") }
    var editRef by remember { mutableStateOf("") }
    var editMsg by remember { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboardManager.current

    fun runImport(asJson: Boolean) {
        importErrors = emptyList()
        importOk = null
        val result = if (asJson) LotImporter.parseJson(pasteText) else LotImporter.parseCsv(pasteText)
        when (result) {
            is LotImportResult.Success -> {
                refresh++
                selectedLotId = result.lot.id
                selectedMembers = result.lot.memberProjectIds.toSet()
                importOk = "Imported \"${result.lot.name}\" · ${result.lot.modeLabel}" +
                    if (result.warnings.isNotEmpty()) " · ${result.warnings.joinToString("; ")}" else ""
                pasteText = ""
                showImport = false
            }
            is LotImportResult.Error -> {
                importErrors = result.errors.map { e ->
                    buildString {
                        append(e.code)
                        e.row?.let { append(" · row ").append(it) }
                        append(": ").append(e.message)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (showImport) "Import lot" else "Lots")
                        Text(
                            if (showImport) "CMP-LOT-IMPORT" else "SCR-LOTS",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProtoStub,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row {
                FilterChip(selected = modeFilter == null, onClick = { modeFilter = null }, label = { Text("All") }, modifier = Modifier.padding(end = 8.dp))
                FilterChip(selected = modeFilter == LotMode.CLASSROOM, onClick = { modeFilter = LotMode.CLASSROOM }, label = { Text("Classroom") }, modifier = Modifier.padding(end = 8.dp))
                FilterChip(selected = modeFilter == LotMode.FLEET, onClick = { modeFilter = LotMode.FLEET }, label = { Text("Fleet") }, modifier = Modifier.padding(end = 8.dp))
                FilterChip(selected = modeFilter == LotMode.SINGLE, onClick = { modeFilter = LotMode.SINGLE }, label = { Text("Single") })
            }
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Canonical CSV headers (PRD v1.3)", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(ProtolotLocks.LOT_CSV_HEADER_LINE, style = MaterialTheme.typography.labelSmall)
                    Text("Template: lot-template.csv · unknown columns warn, required never silently dropped.", style = MaterialTheme.typography.bodyMedium, color = ProtoStub)
                }
            }
            Button(onClick = { showImport = !showImport; onImport() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (showImport) "Hide import" else "Import lot (CSV / JSON)")
            }
            if (showImport) {
                OutlinedTextField(value = pasteText, onValueChange = { pasteText = it; importErrors = emptyList(); importOk = null }, modifier = Modifier.fillMaxWidth().height(160.dp), label = { Text("Paste CSV or JSON lot") })
                Row {
                    OutlinedButton(onClick = {
                        pasteText = ProtolotLocks.LOT_CSV_HEADER_LINE + "\n" +
                            "Student-01,ESP32,A,ESP32-WROOM-32,1,classroom kit MCU\n" +
                            "Student-01,ESP32,A,10K-0603,4,pull-ups\n" +
                            "Student-02,ESP32,B,ESP32-WROOM-32,1,classroom kit MCU\n" +
                            "Student-02,ESP32,B,BME280,1,env sensor variant\n"
                    }) { Text("Load template CSV") }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { clipboard.getText()?.let { pasteText = it.text } }) { Text("Paste clipboard") }
                }
                Button(onClick = { runImport(false) }, modifier = Modifier.fillMaxWidth(), enabled = pasteText.isNotBlank()) { Text("Import as CSV") }
                Button(onClick = { runImport(true) }, modifier = Modifier.fillMaxWidth(), enabled = pasteText.isNotBlank()) { Text("Import as JSON") }
                if (importErrors.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("ERR-IMPORT-PARSE", style = MaterialTheme.typography.titleSmall, color = ProtoRefuse)
                            importErrors.forEach { Text(it, color = ProtoRefuse, style = MaterialTheme.typography.bodyMedium) }
                        }
                    }
                }
            }
            importOk?.let { Text(it, color = ProtoSecondary, style = MaterialTheme.typography.bodyMedium) }
            val visible = lots.filter { modeFilter == null || it.mode == modeFilter }
            if (visible.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("ERR-LOTS-EMPTY", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                        Text("No lots yet", style = MaterialTheme.typography.titleMedium)
                        Text("Import CSV/JSON to batch classroom or fleet builds.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                visible.forEach { lot ->
                    LotCard(lot = lot, selected = selectedLotId == lot.id, onClick = {
                        selectedLotId = lot.id; selectedMembers = lot.memberProjectIds.toSet(); editMsg = null
                    }, onOpenMember = onOpenProject)
                }
            }
            val activeLot = lots.find { it.id == selectedLotId }
            if (activeLot != null) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Global BOM edit · CMP-GLOBAL-BOM-EDIT", style = MaterialTheme.typography.titleMedium)
                        Text(activeLot.modeLabel, style = MaterialTheme.typography.labelSmall, color = ProtoSecondary)
                        activeLot.memberProjectIds.forEach { mid ->
                            val p = ProjectStore.get(mid)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = mid in selectedMembers, onCheckedChange = {
                                    selectedMembers = if (it) selectedMembers + mid else selectedMembers - mid
                                })
                                Text(p?.let { "${it.title} · ${it.variantLabel ?: it.id}" } ?: mid, modifier = Modifier.clickable { onOpenProject(mid) })
                            }
                        }
                        Row {
                            listOf("mpn", "qty", "notes").forEach { f ->
                                FilterChip(selected = editField == f, onClick = { editField = f }, label = { Text(f) }, modifier = Modifier.padding(end = 6.dp))
                            }
                        }
                        OutlinedTextField(value = editRef, onValueChange = { editRef = it }, label = { Text("Ref filter (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = editValue, onValueChange = { editValue = it }, label = { Text("New value") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Button(onClick = {
                            val n = ProjectStore.applyGlobalBomEdit(activeLot.id, selectedMembers.toList(), editField, editValue, editRef.ifBlank { null })
                            refresh++; editMsg = "Applied $editField to $n selected member(s)."
                        }, enabled = selectedMembers.isNotEmpty() && editValue.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Apply to selected members") }
                        editMsg?.let { Text(it, color = ProtoSecondary) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LotCard(lot: Lot, selected: Boolean, onClick: () -> Unit, onOpenMember: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(
        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
    )) {
        Column(Modifier.padding(14.dp)) {
            Text(lot.name, style = MaterialTheme.typography.titleMedium)
            Text(lot.modeLabel, style = MaterialTheme.typography.labelSmall, color = ProtoSecondary)
            Text("${lot.batchCount} members · source ${lot.sourceLabel}", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
            if (selected) {
                lot.memberProjectIds.take(5).forEach { id ->
                    val p = ProjectStore.get(id)
                    Text("→ ${p?.title ?: id}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.clickable { onOpenMember(id) })
                }
            }
        }
    }
}
