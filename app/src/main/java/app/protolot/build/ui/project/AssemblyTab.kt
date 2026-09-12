package app.protolot.build.ui.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.protolot.build.data.ProjectPack
import app.protolot.build.data.ProjectStore
import app.protolot.build.ui.theme.ProtoSecondary
import app.protolot.build.ui.theme.ProtoStub

@Composable
internal fun AssemblyTab(pack: ProjectPack, onPackChanged: () -> Unit) {
    val total = pack.assembly.size
    val done = pack.assembly.count { it.checked }
    val progress = if (total == 0) 0f else done.toFloat() / total.toFloat()

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("Assembly · CMP-ASSEMBLY-LIST", style = MaterialTheme.typography.titleMedium)
            Text(
                "Ordered checklist — check off on the bench. Progress $done of $total.",
                style = MaterialTheme.typography.labelSmall,
                color = ProtoStub,
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
        }
        itemsIndexed(pack.assembly) { index, step ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    Modifier.padding(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Checkbox(
                        checked = step.checked,
                        onCheckedChange = {
                            ProjectStore.toggleAssemblyStep(pack.id, index)
                            onPackChanged()
                        },
                    )
                    Column(Modifier.padding(top = 10.dp, end = 8.dp)) {
                        Text(
                            "${index + 1}. ${step.title}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        if (step.body.isNotBlank()) {
                            Text(step.body, style = MaterialTheme.typography.bodyMedium, color = ProtoStub)
                        }
                        if (step.partRefs.isNotEmpty()) {
                            Text(
                                "Parts: ${step.partRefs.joinToString(", ")}",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    ProjectStore.setAssemblyReviewed(pack.id, reviewed = true)
                    onPackChanged()
                },
                enabled = total > 0 && done < total,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Mark build pack reviewed") }
            if (total > 0 && done == total) {
                Text(
                    "All steps checked — pack reviewed on device.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ProtoSecondary,
                )
            }
        }
    }
}
