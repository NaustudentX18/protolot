package app.protolot.build.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import app.protolot.build.data.LlmGenerator
import app.protolot.build.data.ProjectStore
import app.protolot.build.data.ProviderStore
import app.protolot.build.data.SafetyGate
import app.protolot.build.ui.theme.ProtoRefuse
import app.protolot.build.ui.theme.ProtoStub
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val boardHints = listOf("ESP32", "Arduino", "Pi", "Other")
private val templates = listOf("Blink + button", "Env sensor node", "Line-follower shell", "Soil moisture logger")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenProject: (String) -> Unit,
    onOpenProviders: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    var prompt by remember { mutableStateOf("") }
    var board by remember { mutableStateOf<String?>(null) }
    var refuseMessage by remember { mutableStateOf<String?>(null) }
    var showRefuseDialog by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var refresh by remember { mutableStateOf(0) }
    var generating by remember { mutableStateOf(false) }
    var genError by remember { mutableStateOf<String?>(null) }
    var showImport by remember { mutableStateOf(false) }
    var importJson by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }
    val projects = remember(refresh) { ProjectStore.all() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val providerHint = if (ProviderStore.llm.isConfigured) "LLM BYOK" else "stub (unconfigured)"

    fun doGenerate() {
        val verdict = SafetyGate.evaluate(prompt)
        if (!verdict.allowed) {
            ProjectStore.recordBlocked(prompt, verdict.reason)
            refuseMessage = verdict.reason
            showRefuseDialog = true
            return
        }
        genError = null
        generating = true
        val p = prompt
        val b = board
        scope.launch {
            val result = withContext(Dispatchers.IO) { LlmGenerator.generateSync(p, b) }
            generating = false
            when (result) {
                is LlmGenerator.GenResult.Ok -> {
                    refresh++
                    prompt = ""
                    onOpenProject(result.pack.id)
                }
                is LlmGenerator.GenResult.Fail -> { genError = result.message }
            }
        }
    }

    if (showRefuseDialog && refuseMessage != null) {
        AlertDialog(
            onDismissRequest = { showRefuseDialog = false },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = ProtoRefuse) },
            title = { Text("Generation refused") },
            text = {
                Column {
                    Text(refuseMessage!!)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "On-device safety gate — no project created, no partial BOM.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showRefuseDialog = false }) { Text("OK") } },
            dismissButton = {
                TextButton(onClick = { showRefuseDialog = false; onOpenSettings() }) { Text("Safety policy") }
            },
        )
    }

    if (showImport) {
        AlertDialog(
            onDismissRequest = { showImport = false },
            title = { Text("Reopen JSON project pack") },
            text = {
                Column {
                    Text("Paste an exported Protolot JSON pack (offline-readable).", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJson,
                        onValueChange = { importJson = it; importError = null },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        label = { Text("JSON") },
                    )
                    importError?.let { Text(it, color = ProtoRefuse, style = MaterialTheme.typography.labelSmall) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    try {
                        val pack = ProjectStore.importPackJson(importJson)
                        showImport = false
                        importJson = ""
                        refresh++
                        onOpenProject(pack.id)
                    } catch (e: Exception) {
                        importError = "Could not read pack: ${e.message}"
                    }
                }) { Text("Open") }
            },
            dismissButton = {
                TextButton(onClick = { clipboard.getText()?.let { importJson = it.text } }) { Text("Paste") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Protolot", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Copper Bench · Ideas · $providerHint",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text("Import JSON pack") }, onClick = { menuOpen = false; showImport = true })
                        DropdownMenuItem(text = { Text("Providers") }, onClick = { menuOpen = false; onOpenProviders() })
                        DropdownMenuItem(text = { Text("Settings") }, onClick = { menuOpen = false; onOpenSettings() })
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("CMP-PROMPT-COMPOSE", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it; refuseMessage = null; genError = null },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    placeholder = { Text("Describe what you want to build.") },
                    enabled = !generating,
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    boardHints.forEach { hint ->
                        FilterChip(
                            selected = board == hint,
                            onClick = { board = if (board == hint) null else hint },
                            label = { Text(hint) },
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { doGenerate() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = prompt.isNotBlank() && !generating,
                ) {
                    if (generating) {
                        CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Generating…")
                    } else {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generate pack")
                    }
                }
                TextButton(onClick = onOpenSettings) {
                    Text("We refuse weapons & explosives as primary purpose.")
                }
                genError?.let { err ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("ERR-GEN-FAIL", style = MaterialTheme.typography.labelSmall, color = ProtoRefuse)
                            Text(err, style = MaterialTheme.typography.bodyMedium)
                            Row {
                                Button(onClick = { doGenerate() }) { Text("Retry") }
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(onClick = onOpenProviders) { Text("Check Providers") }
                            }
                        }
                    }
                }
            }
            item {
                Text("Templates", style = MaterialTheme.typography.titleMedium)
                Text("CMP-TEMPLATE-ROW", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    templates.forEach { tmpl ->
                        Card(
                            modifier = Modifier.padding(end = 8.dp).width(160.dp)
                                .clickable { prompt = tmpl; refuseMessage = null; genError = null },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Text(tmpl, Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Recent projects", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { showImport = true }) { Text("Import JSON") }
                }
                Text("CMP-RECENT-LIST", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
            }
            if (projects.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("No projects yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Enter a prompt above to create a build pack, or import a JSON pack offline.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(projects) { pack ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenProject(pack.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(pack.title, style = MaterialTheme.typography.titleMedium)
                            val boardLabel = pack.boardClass ?: "—"
                            Text(
                                "id ${pack.id} · $boardLabel · ${pack.generationSource} · ${pack.bom.size} BOM",
                                style = MaterialTheme.typography.labelSmall,
                                color = ProtoStub,
                            )
                        }
                    }
                }
            }
        }
    }
}
