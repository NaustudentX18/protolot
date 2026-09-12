package app.protolot.build.ui.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import app.protolot.build.data.ProjectPackCodec
import app.protolot.build.data.ProjectStore
import app.protolot.build.ui.theme.ProtoStub
import kotlinx.coroutines.launch

private val tabs = listOf("Overview", "Wiring", "BOM", "Assembly", "CAD")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectScreen(projectId: String, initialTab: String, onBack: () -> Unit) {
    var revision by remember { mutableIntStateOf(0) }
    val pack = remember(projectId, revision) { ProjectStore.get(projectId) }
    var selected by remember {
        mutableIntStateOf(
            when (initialTab.lowercase()) {
                "wiring" -> 1; "bom" -> 2; "assembly" -> 3; "cad" -> 4; else -> 0
            },
        )
    }
    var menuOpen by remember { mutableStateOf(false) }
    var showExport by remember { mutableStateOf(false) }
    var exportText by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    if (showExport && exportText != null) {
        AlertDialog(
            onDismissRequest = { showExport = false },
            title = { Text("Export JSON project pack") },
            text = {
                Column {
                    Text(
                        "Offline-readable pack · schema v${ProjectPackCodec.SCHEMA_VERSION}. Reopen via Home → Import.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        exportText!!.take(600) + if (exportText!!.length > 600) "\n…" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProtoStub,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(exportText!!))
                    showExport = false
                    scope.launch { snackbar.showSnackbar("JSON copied — offline-readable") }
                }) { Text("Copy JSON") }
            },
            dismissButton = { TextButton(onClick = { showExport = false }) { Text("Close") } },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(pack?.title ?: "Missing project")
                        Text(
                            "SCR-PROJECT · ${pack?.generationSource ?: "—"}",
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
                actions = {
                    IconButton(onClick = {
                        val json = ProjectStore.exportPackJson(projectId)
                        if (json != null) { exportText = json; showExport = true }
                        else scope.launch { snackbar.showSnackbar("Export failed — pack missing") }
                    }) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "Export JSON")
                    }
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Export JSON project pack") },
                            onClick = {
                                menuOpen = false
                                val json = ProjectStore.exportPackJson(projectId)
                                if (json != null) { exportText = json; showExport = true }
                            },
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (pack == null) {
            Column(Modifier.padding(padding).padding(16.dp)) {
                Text("Project not found.", style = MaterialTheme.typography.bodyLarge)
                Text("Return home and generate a pack, or import JSON.", color = ProtoStub)
            }
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selected) {
                tabs.forEachIndexed { index, label ->
                    Tab(selected = selected == index, onClick = { selected = index }, text = { Text(label) })
                }
            }
            when (selected) {
                0 -> OverviewTab(pack)
                1 -> WiringTab(pack)
                2 -> BomTab(pack)
                3 -> AssemblyTab(pack)
                4 -> CadTab(pack)
            }
        }
    }
}
