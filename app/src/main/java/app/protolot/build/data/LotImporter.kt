package app.protolot.build.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Lot CSV/JSON import — AC-Lots.
 * Canonical headers (exact): name,board,variant,mpn,qty,notes
 */
object LotImporter {

    fun parseCsv(text: String): LotImportResult {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return LotImportResult.Error(
                listOf(ImportError("EMPTY", "CSV is empty. Download the template with headers: ${ProtolotLocks.LOT_CSV_HEADER_LINE}")),
            )
        }
        val header = splitCsvLine(lines.first()).map { it.trim().lowercase() }
        val expected = ProtolotLocks.LOT_CSV_HEADERS
        val errors = mutableListOf<ImportError>()
        val missing = expected.filter { it !in header }
        if (missing.isNotEmpty()) {
            errors += ImportError(
                "BAD_COLUMNS",
                "Missing required columns: ${missing.joinToString(", ")}. Expected exactly: ${ProtolotLocks.LOT_CSV_HEADER_LINE}",
            )
        }
        val unknown = header.filter { it !in expected && it.isNotBlank() }
        val warnings = mutableListOf<String>()
        if (unknown.isNotEmpty()) {
            warnings += "Unknown columns ignored (not dropped required): ${unknown.joinToString(", ")}"
        }
        if (errors.isNotEmpty()) return LotImportResult.Error(errors)

        val idx = expected.associateWith { header.indexOf(it) }
        val rows = mutableListOf<LotImportRow>()
        for (i in 1 until lines.size) {
            val cols = splitCsvLine(lines[i])
            fun col(name: String): String {
                val at = idx[name] ?: return ""
                return cols.getOrNull(at)?.trim().orEmpty()
            }
            val name = col("name")
            val board = col("board")
            val variant = col("variant")
            val mpn = col("mpn")
            val qtyRaw = col("qty")
            val notes = col("notes")
            val rowNum = i + 1
            if (name.isBlank()) {
                errors += ImportError("MISSING_NAME", "Row $rowNum: name is required", rowNum)
                continue
            }
            val qty = qtyRaw.toIntOrNull()
            if (qty == null || qty < 0) {
                errors += ImportError("BAD_QTY", "Row $rowNum: qty must be a non-negative integer (got \"$qtyRaw\")", rowNum)
                continue
            }
            if (mpn.isBlank()) {
                errors += ImportError("MISSING_SKU", "Row $rowNum: mpn/SKU is missing — add an MPN or use a placeholder", rowNum)
                continue
            }
            rows += LotImportRow(name, board, variant, mpn, qty, notes, rowNum)
        }
        if (errors.isNotEmpty()) return LotImportResult.Error(errors)
        if (rows.isEmpty()) {
            return LotImportResult.Error(
                listOf(ImportError("NO_ROWS", "No data rows after header. Add at least one lot member.")),
            )
        }
        return materialize(rows, warnings, sourceLabel = "csv")
    }

    fun parseJson(text: String): LotImportResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return LotImportResult.Error(listOf(ImportError("EMPTY", "JSON is empty.")))
        }
        return try {
            val array = when {
                trimmed.startsWith("[") -> JSONArray(trimmed)
                trimmed.startsWith("{") -> {
                    val obj = JSONObject(trimmed)
                    when {
                        obj.has("rows") -> obj.getJSONArray("rows")
                        obj.has("members") -> obj.getJSONArray("members")
                        else -> JSONArray().put(obj)
                    }
                }
                else -> throw IllegalArgumentException("JSON must be an array or object")
            }
            val errors = mutableListOf<ImportError>()
            val rows = mutableListOf<LotImportRow>()
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                val rowNum = i + 1
                fun req(key: String): String = o.optString(key, "").trim()
                val name = req("name")
                val board = req("board")
                val variant = req("variant")
                val mpn = req("mpn")
                val notes = req("notes")
                val qty = if (o.has("qty")) o.optInt("qty", -1) else -1
                if (name.isBlank()) {
                    errors += ImportError("MISSING_NAME", "Item $rowNum: name is required", rowNum)
                    continue
                }
                if (qty < 0) {
                    errors += ImportError("BAD_QTY", "Item $rowNum: qty must be a non-negative integer", rowNum)
                    continue
                }
                if (mpn.isBlank()) {
                    errors += ImportError("MISSING_SKU", "Item $rowNum: mpn/SKU is missing", rowNum)
                    continue
                }
                rows += LotImportRow(name, board, variant, mpn, qty, notes, rowNum)
            }
            if (errors.isNotEmpty()) return LotImportResult.Error(errors)
            if (rows.isEmpty()) {
                return LotImportResult.Error(listOf(ImportError("NO_ROWS", "JSON array had no usable members.")))
            }
            materialize(rows, emptyList(), sourceLabel = "json")
        } catch (e: Exception) {
            LotImportResult.Error(
                listOf(ImportError("PARSE", "Could not parse JSON: ${e.message ?: "unknown error"}")),
            )
        }
    }

    private fun materialize(
        rows: List<LotImportRow>,
        warnings: List<String>,
        sourceLabel: String,
    ): LotImportResult {
        val mode = inferMode(rows)
        val lotName = deriveLotName(rows, mode)
        val created = ProjectStore.createLotFromRows(lotName, mode, rows, sourceLabel)
        return LotImportResult.Success(created.first, created.second, warnings)
    }

    private fun inferMode(rows: List<LotImportRow>): LotMode {
        val names = rows.map { it.name }.distinct()
        return when {
            names.size <= 1 && rows.size <= 2 -> LotMode.SINGLE
            rows.any { it.name.contains("fleet", true) || it.variant.contains("unit", true) } -> LotMode.FLEET
            names.size >= 2 -> LotMode.CLASSROOM
            else -> LotMode.CLASSROOM
        }
    }

    private fun deriveLotName(rows: List<LotImportRow>, mode: LotMode): String {
        val base = rows.firstOrNull()?.name?.substringBefore("-")?.ifBlank { null }
            ?: rows.firstOrNull()?.board?.ifBlank { null }
            ?: "Lot"
        return when (mode) {
            LotMode.CLASSROOM -> "$base classroom"
            LotMode.FLEET -> "$base fleet"
            LotMode.SINGLE -> base
        }
    }

    fun splitCsvLine(line: String): List<String> {
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"'); i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    out += sb.toString(); sb.clear()
                }
                else -> sb.append(c)
            }
            i++
        }
        out += sb.toString()
        return out
    }
}
