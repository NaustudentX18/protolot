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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.protolot.build.data.ProjectStore
import app.protolot.build.data.SafetyGate
import app.protolot.build.ui.theme.ProtoRefuse
import app.protolot.build.ui.theme.ProtoStub

private val boardHints = listOf("ESP32", "Arduino", "Pi", "Other")
private val templates = listOf(
    "Blink + button",
    "Env sensor node",
    "Line-follower shell",
    "Soil moisture logger",
)

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
    val projects = remember(refresh) { ProjectStore.all() }

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
            confirmButton = {
                TextButton(onClick = { showRefuseDialog = false }) { Text("OK") }
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
                            "Copper Bench · Ideas",
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
                        DropdownMenuItem(
                            text = { Text("Providers") },
                            onClick = { menuOpen = false; onOpenProviders() },
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = { menuOpen = false; onOpenSettings() },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                // CMP-PROMPT-COMPOSE
                Text("CMP-PROMPT-COMPOSE", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it; refuseMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    placeholder = { Text("Describe what you want to build.") },
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
                    onClick = {
                        val verdict = SafetyGate.evaluate(prompt)
                        if (!verdict.allowed) {
                            ProjectStore.recordBlocked(prompt, verdict.reason)
                            refuseMessage = verdict.reason
                            showRefuseDialog = true
                            return@Button
                        }
                        refuseMessage = null
                        val pack = ProjectStore.createFromPrompt(prompt, board)
                        refresh++
                        prompt = ""
                        onOpenProject(pack.id)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = prompt.isNotBlank(),
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Generate pack")
                }
                // CMP-SAFETY-INLINE
                TextButton(onClick = onOpenSettings) {
                    Text("We refuse weapons & explosives as primary purpose.")
                }
            }

            item {
                Text("Templates", style = MaterialTheme.typography.titleMedium)
                Text("CMP-TEMPLATE-ROW", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    templates.forEach { tmpl ->
                        Card(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .width(160.dp)
                                .clickable { prompt = tmpl; refuseMessage = null },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        ) {
                            Text(tmpl, Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item {
                Text("Recent projects", style = MaterialTheme.typography.titleMedium)
                Text("CMP-RECENT-LIST", style = MaterialTheme.typography.labelSmall, color = ProtoStub)
            }
            if (projects.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("No projects yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Enter a prompt above to create a local stub pack and open Overview.",
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
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(pack.title, style = MaterialTheme.typography.titleMedium)
                            val boardLabel = pack.boardClass ?: "—"
                            Text(
                                "id ${pack.id} · $boardLabel · stub",
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
