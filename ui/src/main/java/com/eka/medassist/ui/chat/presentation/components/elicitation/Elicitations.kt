@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.eka.medassist.ui.chat.presentation.components.elicitation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.presentation.common.molecule.ButtonWrapper
import com.eka.medassist.ui.chat.presentation.common.molecule.ButtonWrapperType
import com.eka.medassist.ui.chat.presentation.components.SuggestionRow
import com.eka.medassist.ui.chat.presentation.models.SuggestionModel
import com.eka.conversation.matrix.MatrixComponents
import com.eka.conversation.matrix.str
import com.eka.medassist.ui.chat.presentation.models.ElicitationEntry
import com.eka.medassist.ui.chat.presentation.models.SendOptions
import dev.jeziellago.compose.markdowntext.MarkdownText
import com.eka.medassist.ui.chat.theme.DarwinTouchGreenBgLight
import com.eka.medassist.ui.chat.theme.DarwinTouchGreenDark
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral100
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral200
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral300
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimaryBgLight
import com.eka.medassist.ui.chat.theme.touchBodyBold
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import com.eka.medassist.ui.chat.theme.touchCaption1Regular
import com.eka.medassist.ui.chat.theme.touchFootnoteRegular
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Port of the widget's ToolElicitationRenderer. */
@Composable
fun ElicitationRenderer(
    messageId: String,
    toolKey: String,
    entry: ElicitationEntry,
    disabled: Boolean,
    onSend: (SendOptions) -> Unit,
) {
    val input = entry.input
    val responded = entry.disabled
    Column(modifier = Modifier.fillMaxWidth().padding(start = 40.dp, top = 8.dp, end = 8.dp)) {
        when (entry.component) {
            MatrixComponents.PILL -> Pills(
                options = input.options("options"),
                disabled = responded || disabled,
            ) { label, value ->
                onSend(SendOptions(message = value, toolCalled = true, toolKey = toolKey,
                    selectedComponent = JsonObject(mapOf("label" to JsonPrimitive(label), "value" to JsonPrimitive(value)))))
            }
            MatrixComponents.MULTI -> MultiSelect(
                options = input.options("options"),
                disabled = responded || disabled,
            ) { values -> onSend(SendOptions(message = values.joinToString(", "), toolCalled = true, toolKey = toolKey)) }
            MatrixComponents.CARD -> CardList(
                cards = (input?.get("cards") as? JsonArray)?.mapNotNull { it as? JsonObject }?.filter { it.str("card_id") != null }.orEmpty(),
                horizontal = entry.orientation == "horizontal",
                disabled = responded || disabled,
            ) { card, cta ->
                val primaryResult = (card["primary_cta"] as? JsonObject)?.get("tool_result") as? JsonObject
                onSend(SendOptions(
                    message = cta.str("text") ?: cta.str("title"), toolCalled = true, toolKey = toolKey,
                    selectedComponent = card, toolResult = primaryResult,
                ))
            }
            MatrixComponents.MEDIA_CARD -> (input?.get("media_card") as? JsonObject)?.let { MediaCard(it) }
            MatrixComponents.DOCTOR_CARD -> {
                val doctors = (input?.get("doctors") as? JsonArray)?.mapNotNull { it as? JsonObject }?.filter { it.str("doctor_id") != null }.orEmpty()
                val details = input?.get("doctor_details") as? JsonObject
                DoctorCarousel(
                    doctors = doctors,
                    details = details,
                    disabled = disabled,
                    hideShowMore = entry.hideShowMore == true,
                    onShowMore = { onSend(SendOptions(message = "show more", toolDeclined = true)) },
                ) { doctor, message, toolResult, callTool ->
                    onSend(SendOptions(toolCalled = callTool, selectedComponent = doctor, message = message, toolKey = toolKey, toolResult = toolResult))
                }
            }
        }
        entry.responseLabel?.takeIf { it.isNotBlank() && entry.component != MatrixComponents.PILL && it.startsWith("Tool call failed") }?.let {
            Text(it, style = touchCaption1Regular, color = DarwinTouchNeutral600, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

private fun JsonObject?.options(key: String): List<Pair<String, String>> {
    return when (val raw = this?.get(key)) {
        is JsonArray -> raw.mapNotNull { o -> (o as? JsonObject)?.let { (it.str("label") ?: it.str("value") ?: "") to (it.str("value") ?: it.str("label") ?: "") } }
        is JsonObject -> raw.keys.map { it to it }
        else -> emptyList()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Pills(options: List<Pair<String, String>>, disabled: Boolean, onPick: (String, String) -> Unit) {
    var clicked by remember(options) { mutableStateOf(false) }
    var visible by remember(options) { mutableIntStateOf(5) }
    val off = disabled || clicked
    val vertical = options.size > 3
    val content: @Composable () -> Unit = {
        options.take(visible).forEach { (label, value) ->
            Chip(label, enabled = !off) { clicked = true; onPick(label, value) }
        }
        if (options.size > visible) {
            Chip("Show ${minOf(5, options.size - visible)} more", enabled = !off, dashed = true) { visible += 5 }
        }
    }
    if (vertical) Column {
        // Long lists use the DocAssist suggestion rows; short ones stay as chips.
        options.take(visible).forEach { (label, value) ->
            SuggestionRow(suggestion = SuggestionModel(label = label), enabled = !off) { clicked = true; onPick(label, value) }
            Spacer(Modifier.height(8.dp))
        }
        if (options.size > visible) {
            Chip("Show ${minOf(5, options.size - visible)} more", enabled = !off, dashed = true) { visible += 5 }
        }
    }
    else FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
}

@Composable
fun Chip(text: String, enabled: Boolean, selected: Boolean = false, dashed: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) DarwinTouchPrimary else DarwinTouchPrimaryBgLight)
            .border(1.dp, if (dashed) DarwinTouchPrimary.copy(alpha = 0.5f) else DarwinTouchPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled || selected) 1f else 0.5f)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(text, style = touchFootnoteRegular, color = if (selected) DarwinTouchNeutral0 else DarwinTouchPrimary)
    }
}

/** DocAssist multi-select rows (checkbox + label) with a Confirm button; returns the selected values. */
@Composable
fun MultiSelect(options: List<Pair<String, String>>, disabled: Boolean, onConfirm: (List<String>) -> Unit) {
    val selected = remember(options) { mutableStateListOf<String>() }
    Column {
        options.forEach { (label, value) ->
            SuggestionRow(
                suggestion = SuggestionModel(label = label, selected = value in selected),
                isSelectable = true,
                enabled = !disabled,
            ) { if (value in selected) selected.remove(value) else selected.add(value) }
            Spacer(Modifier.height(8.dp))
        }
        ButtonWrapper(
            text = "Confirm",
            enabled = !disabled && selected.isNotEmpty(),
            type = ButtonWrapperType.OUTLINED,
            onClick = { onConfirm(options.map { it.second }.filter { it in selected }) },
        )
    }
}

@Composable
fun CardList(cards: List<JsonObject>, horizontal: Boolean, disabled: Boolean, onCta: (JsonObject, JsonObject) -> Unit) {
    if (cards.isEmpty()) {
        Text("No options available.", style = touchFootnoteRegular, color = DarwinTouchNeutral600); return
    }
    var anySelected by remember(cards) { mutableStateOf(false) }
    val pick: (JsonObject, JsonObject) -> Unit = { card, cta ->
        if (!anySelected) {
            anySelected = true; onCta(card, cta)
        }
    }
    if (horizontal) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(cards) { GenericCard(it, disabled || anySelected, Modifier.width(260.dp), pick) }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            cards.forEach { GenericCard(it, disabled || anySelected, Modifier.fillMaxWidth(), pick) }
        }
    }
}

@Composable
fun GenericCard(card: JsonObject, disabled: Boolean, modifier: Modifier, onCta: (JsonObject, JsonObject) -> Unit) {
    val uri = LocalUriHandler.current
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(DarwinTouchNeutral0)
            .border(1.dp, DarwinTouchNeutral200, RoundedCornerShape(12.dp)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(if (card.str("img") != null) DarwinTouchPrimaryBgLight else DarwinTouchNeutral0).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                card.str("title")?.let { Text(it, style = touchBodyBold, color = DarwinTouchNeutral1000) }
                card.str("tags")?.let { Text(it, style = touchCaption1Regular, color = DarwinTouchNeutral600) }
            }
            card.str("img")?.let {
                AsyncImage(model = it, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(40.dp).clip(CircleShape))
            }
        }
        Column(Modifier.padding(horizontal = 12.dp)) {
            card.str("description")?.let { MarkdownText(markdown = it, style = touchFootnoteRegular, color = DarwinTouchNeutral800) }
            card.str("banner_text")?.let {
                Text(it, style = touchCaption1Regular, color = DarwinTouchGreenDark,
                    modifier = Modifier.padding(top = 6.dp).clip(RoundedCornerShape(6.dp)).background(DarwinTouchGreenBgLight).padding(horizontal = 8.dp, vertical = 4.dp))
            }
            (card["items"] as? JsonArray)?.mapNotNull { (it as? JsonObject)?.str("text") }?.forEach {
                Text("• $it", style = touchCaption1Regular, color = DarwinTouchNeutral800, modifier = Modifier.padding(top = 2.dp))
            }
        }
        val primary = card["primary_cta"] as? JsonObject
        val secondary = card["secondary_cta"] as? JsonObject
        if (primary?.str("title") != null || secondary?.str("title") != null) {
            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                primary?.str("title")?.let { title ->
                    Button(
                        onClick = { primary.str("link")?.let { runCatching { uri.openUri(it) } }; onCta(card, primary) },
                        enabled = !disabled, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarwinTouchPrimary),
                    ) { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }
                secondary?.str("title")?.let { title ->
                    OutlinedButton(
                        onClick = { secondary.str("link")?.let { runCatching { uri.openUri(it) } }; onCta(card, secondary) },
                        enabled = !disabled, modifier = Modifier.weight(1f),
                    ) { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, color = DarwinTouchPrimary) }
                }
            }
        } else Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun MediaCard(card: JsonObject) {
    val uri = LocalUriHandler.current
    val link = card.str("link")
    Column(
        modifier = Modifier.widthIn(max = 280.dp).clip(RoundedCornerShape(12.dp)).background(DarwinTouchNeutral0)
            .border(1.dp, DarwinTouchNeutral200, RoundedCornerShape(12.dp)).padding(12.dp),
    ) {
        link?.let {
            AsyncImage(model = it, contentDescription = null, contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(8.dp)).background(DarwinTouchNeutral100))
        }
        card.str("title")?.let { Text(it, style = touchBodyBold, color = DarwinTouchNeutral1000, modifier = Modifier.padding(top = 8.dp)) }
        (card.str("tags_text") ?: card.str("tags"))?.let { Text(it, style = touchCaption1Regular, color = DarwinTouchNeutral600) }
        card.str("description")?.let { MarkdownText(markdown = it, style = touchFootnoteRegular, color = DarwinTouchNeutral800) }
        card.str("banner_text")?.let { Text(it, style = touchCaption1Regular, color = DarwinTouchPrimary, modifier = Modifier.padding(top = 4.dp)) }
        if (link != null && card.str("action") == "download") {
            OutlinedButton(onClick = { runCatching { uri.openUri(link) } }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Icon(painterResource(R.drawable.ic_download_regular), null, tint = DarwinTouchPrimary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Download", color = DarwinTouchPrimary)
            }
        }
    }
}

@Composable
fun DoctorCarousel(
    doctors: List<JsonObject>,
    details: JsonObject?,
    disabled: Boolean,
    hideShowMore: Boolean,
    onShowMore: () -> Unit,
    onSelect: (doctor: JsonObject, message: String?, toolResult: JsonObject?, callTool: Boolean) -> Unit,
) {
    if (doctors.isEmpty()) return
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(doctors) { doc ->
            DoctorCard(doc, details?.get(doc.str("doctor_id") ?: "") as? JsonObject, disabled, onSelect)
        }
        if (doctors.size > 1 && !hideShowMore) {
            item {
                Box(
                    modifier = Modifier.width(140.dp).height(120.dp).clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(2.dp, DarwinTouchNeutral300), RoundedCornerShape(12.dp))
                        .background(DarwinTouchPrimaryBgLight).clickable(enabled = !disabled, onClick = onShowMore),
                    contentAlignment = Alignment.Center,
                ) { Text("Show more", style = touchBodyBold, color = DarwinTouchPrimary) }
            }
        }
    }
}

/** Port of the widget DoctorCard: hospital, availability dates (3 at a time), slots, confirm. */
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun DoctorCard(
    doctor: JsonObject,
    details: JsonObject?,
    disabled: Boolean,
    onSelect: (doctor: JsonObject, message: String?, toolResult: JsonObject?, callTool: Boolean) -> Unit,
) {
    details ?: return
    val name = details.str("name").orEmpty()
    val hospitals = (details["hospitals"] as? JsonArray)?.mapNotNull { it as? JsonObject }.orEmpty()
    var hospital by remember(hospitals) { mutableStateOf(hospitals.firstOrNull()?.str("hospital_id")) }
    var showAvailability by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedSlot by remember { mutableStateOf<String?>(null) }
    var offset by remember { mutableIntStateOf(0) }
    var confirmed by remember { mutableStateOf(false) }
    val availability = (doctor["availability"] as? JsonArray)?.mapNotNull { it as? JsonObject }.orEmpty()

    fun docWith(vararg pairs: Pair<String, String?>): JsonObject {
        val m = LinkedHashMap<String, JsonElement>(doctor)
        hospital?.let { m["hospital_id"] = JsonPrimitive(it) }
        pairs.forEach { (k, v) -> if (v != null) m[k] = JsonPrimitive(v) }
        return JsonObject(m)
    }

    LaunchedEffect(showAvailability, availability.size) {
        if (!showAvailability || availability.isEmpty() || selectedDate != null) return@LaunchedEffect
        val d = availability.firstOrNull { it.str("date") == doctor.str("preferred_date") } ?: availability.first()
        selectedDate = d.str("date")
        val slots = d["slots"] as? JsonArray
        if (slots == null || slots.isEmpty()) onSelect(docWith("preferred_date" to selectedDate), null, null, true)
    }

    Column(
        modifier = Modifier.width(290.dp).clip(RoundedCornerShape(12.dp)).background(DarwinTouchNeutral0)
            .border(1.dp, DarwinTouchNeutral200, RoundedCornerShape(12.dp)).padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val pic = details.str("profile_pic")
            if (pic != null) AsyncImage(pic, null, contentScale = ContentScale.Crop, modifier = Modifier.size(44.dp).clip(CircleShape))
            else Box(Modifier.size(44.dp).clip(CircleShape).background(DarwinTouchPrimaryBgLight), contentAlignment = Alignment.Center) {
                Text(name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("").ifEmpty { "DR" }, color = DarwinTouchPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(name, style = touchBodyBold, color = DarwinTouchNeutral1000)
                Text(listOfNotNull(details.str("specialty"), details.str("experience")).joinToString(" • "),
                    style = touchCaption1Regular, color = DarwinTouchNeutral600, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        details.str("timings")?.let { Text(it, style = touchCaption1Regular, color = DarwinTouchNeutral800, modifier = Modifier.padding(top = 6.dp)) }
        details.str("languages")?.let { Text(it, style = touchCaption1Regular, color = DarwinTouchNeutral800) }
        if (hospitals.size > 1) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                hospitals.forEach { h ->
                    val id = h.str("hospital_id")
                    Chip(h.str("name").orEmpty(), enabled = !disabled, selected = id == hospital) {
                        hospital = id; selectedDate = null; selectedSlot = null; offset = 0
                        onSelect(docWith(), null, null, true)
                    }
                }
            }
        } else hospitals.firstOrNull()?.str("name")?.let {
            Text(it, style = touchCaption1Regular, color = DarwinTouchNeutral800, modifier = Modifier.padding(top = 2.dp))
        }
        details.str("info")?.let { MarkdownText(markdown = it, style = touchCaption1Regular, color = DarwinTouchNeutral600, modifier = Modifier.padding(top = 4.dp)) }

        if (!showAvailability) {
            OutlinedButton(
                onClick = {
                    showAvailability = true
                    if (availability.isEmpty()) onSelect(docWith(), null, null, true)
                },
                enabled = !disabled, modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            ) { Text("View availability", color = DarwinTouchPrimary) }
        } else {
            if (availability.isEmpty()) {
                Text("Loading availability…", style = touchCaption1Regular, color = DarwinTouchNeutral600, modifier = Modifier.padding(top = 8.dp))
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text("‹", style = touchBodyBold, color = if (offset > 0) DarwinTouchPrimary else DarwinTouchNeutral300,
                        modifier = Modifier.clickable(enabled = offset > 0) { offset = maxOf(0, offset - 3) }.padding(8.dp))
                    availability.drop(offset).take(3).forEach { day ->
                        val date = day.str("date") ?: return@forEach
                        val parsed = runCatching { java.time.LocalDate.parse(date.take(10)) }.getOrNull()
                        val label = parsed?.let { "${it.dayOfWeek.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }}\n${it.dayOfMonth} ${it.month.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }}" } ?: date
                        Box(
                            modifier = Modifier.weight(1f).padding(2.dp).clip(RoundedCornerShape(8.dp))
                                .background(if (date == selectedDate) DarwinTouchPrimary else DarwinTouchPrimaryBgLight)
                                .clickable(enabled = !disabled) {
                                    selectedDate = date; selectedSlot = null; confirmed = false
                                    val slots = day["slots"] as? JsonArray
                                    if (slots == null) onSelect(docWith("preferred_date" to date), null, null, true)
                                }.padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(label, style = touchCaption1Regular, color = if (date == selectedDate) DarwinTouchNeutral0 else DarwinTouchPrimary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                    Text("›", style = touchBodyBold, color = if (offset + 3 < availability.size) DarwinTouchPrimary else DarwinTouchNeutral300,
                        modifier = Modifier.clickable(enabled = offset + 3 < availability.size) { offset += 3 }.padding(8.dp))
                }
                val day = availability.firstOrNull { it.str("date") == selectedDate }
                val slots = (day?.get("slots") as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.content }.orEmpty()
                if (selectedDate != null && slots.isEmpty()) {
                    Text(if (day?.get("slots") == null) "Loading slots…" else "No slots available on this date",
                        style = touchCaption1Regular, color = DarwinTouchNeutral600, modifier = Modifier.padding(top = 6.dp))
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                    slots.forEach { slot -> Chip(slot, enabled = !disabled && !confirmed, selected = slot == selectedSlot) { selectedSlot = slot } }
                }
                val cta = (day?.get("cta") as? JsonObject) ?: (doctor["cta"] as? JsonObject)
                val ctaText = cta?.str("text")
                Button(
                    onClick = {
                        confirmed = true
                        val callTool = cta?.str("action")?.let { it == "tool_call" } ?: ctaText.isNullOrBlank()
                        val suffix = if (selectedDate != null && selectedSlot != null) " on $selectedDate at $selectedSlot." else ""
                        onSelect(
                            docWith("preferred_date" to selectedDate, "preferred_slot_time" to selectedSlot),
                            (ctaText ?: "I want to book an appointment with Dr. $name") + suffix,
                            cta?.get("tool_result") as? JsonObject,
                            callTool,
                        )
                    },
                    enabled = !disabled && !confirmed && (ctaText != null || selectedDate != null),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarwinTouchPrimary),
                ) { Text(cta?.str("title") ?: "Confirm") }
            }
        }
    }
}
