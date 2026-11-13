package com.eka.medassist.ui.chat.common

import com.eka.conversation.matrix.MatrixComponents
import com.eka.conversation.matrix.str
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Port of the widget's doctorUtils merge helpers: doctor_card inputs arrive incrementally
 * (details first, then availability per date, then slots) and must be merged, not replaced.
 */
object DoctorMerge {

    fun mergeInput(prev: JsonObject?, next: JsonObject?, component: String?): JsonObject? {
        if (prev == null) return next
        if (next == null) return prev
        if (component != MatrixComponents.DOCTOR_CARD) return next
        val prevDocs = prev["doctors"] as? JsonArray ?: return prev
        val nextDocs = next["doctors"] as? JsonArray ?: return prev
        val format = prev.str("format") ?: next.str("format")
        val details = LinkedHashMap<String, JsonElement>()
        (prev["doctor_details"] as? JsonObject)?.let { details.putAll(it) }
        (next["doctor_details"] as? JsonObject)?.let { details.putAll(it) }
        val out = LinkedHashMap<String, JsonElement>(next)
        out["doctors"] = JsonArray(mergeDoctors(prevDocs.objects(), nextDocs.objects(), format))
        out["doctor_details"] = JsonObject(details)
        return JsonObject(out)
    }

    private fun mergeDoctors(prev: List<JsonObject>, next: List<JsonObject>, format: String?): List<JsonObject> {
        if (prev.isEmpty()) return next
        if (next.isEmpty()) return prev
        val merged = prev.toMutableList()
        for (incoming in next) {
            val id = incoming.str("doctor_id")
            val idx = if (id != null) merged.indexOfFirst { it.str("doctor_id") == id } else -1
            if (idx == -1) {
                merged += incoming
                continue
            }
            val existing = merged[idx]
            val start = existing.str("start_date") ?: incoming.str("start_date") ?: existing.str("preferred_date") ?: incoming.str("preferred_date")
            val end = existing.str("end_date") ?: incoming.str("end_date")
            val combined = LinkedHashMap<String, JsonElement>(existing)
            incoming.forEach { (k, v) -> if (v !is JsonNull) combined[k] = v }
            combined["availability"] = JsonArray(
                mergeAvailability(
                    (existing["availability"] as? JsonArray)?.objects().orEmpty(),
                    (incoming["availability"] as? JsonArray)?.objects().orEmpty(),
                    start, end, format,
                )
            )
            merged[idx] = JsonObject(combined)
        }
        return merged
    }

    private fun mergeAvailability(
        prev: List<JsonObject>,
        next: List<JsonObject>,
        startDate: String?,
        endDate: String?,
        format: String?,
    ): List<JsonObject> {
        if (prev.isEmpty() && next.isEmpty()) {
            val base = startDate?.let { runCatching { LocalDate.parse(it.take(10)) }.getOrNull() } ?: LocalDate.now()
            val days = if (startDate != null && endDate != null) daysBetween(startDate, endDate) else if (format == "default") 31 else 3
            return (0 until days).map { i ->
                JsonObject(mapOf("date" to JsonPrimitive(base.plusDays(i.toLong()).toString()), "slots" to JsonArray(emptyList())))
            }
        }
        if (prev.isEmpty()) return next
        if (next.isEmpty()) return prev
        val merged = prev.toMutableList()
        for (incoming in next) {
            val date = incoming.str("date")
            val idx = if (date != null) merged.indexOfFirst { it.str("date") == date } else -1
            if (idx == -1) {
                merged += incoming
            } else {
                val existing = merged[idx]
                val combined = LinkedHashMap<String, JsonElement>(existing)
                incoming.forEach { (k, v) -> if (v !is JsonNull) combined[k] = v }
                combined["slots"] = incoming["slots"]?.takeUnless { it is JsonNull } ?: existing["slots"] ?: JsonArray(emptyList())
                merged[idx] = JsonObject(combined)
            }
        }
        return merged
    }

    private fun daysBetween(start: String, end: String): Int = try {
        val d = ChronoUnit.DAYS.between(LocalDate.parse(start.take(10)), LocalDate.parse(end.take(10))).toInt()
        if (d >= 0) d + 1 else 3
    } catch (_: Exception) {
        3
    }

    private fun JsonArray.objects(): List<JsonObject> = mapNotNull { it as? JsonObject }
}
