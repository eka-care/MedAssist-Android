package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.eka.medassist.ui.chat.client.MedAssistRecord
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral100
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimaryBgLight
import com.eka.medassist.ui.chat.theme.touchBodyRegular
import com.eka.medassist.ui.chat.theme.touchFootnoteRegular
import com.eka.medassist.ui.chat.theme.touchHeadlineBold

/** What + offers: records (when the host has them), gallery, camera and files. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachSheet(
    /** Records of this patient; null = all records. Ignored when [onRecords] is null. */
    patientName: String?,
    slotsLeft: Int,
    onRecords: (() -> Unit)?,
    /** Newest records for one-tap attach; null while loading, empty when there are none. */
    recentRecords: List<MedAssistRecord>? = null,
    onRecordClick: (MedAssistRecord) -> Unit = {},
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    onFiles: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = DarwinTouchNeutral0,
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 12.dp)) {
            Text("Add to chat", style = touchHeadlineBold, color = DarwinTouchNeutral1000, modifier = Modifier.padding(horizontal = 20.dp))
            Text(
                "Up to $slotsLeft more file${if (slotsLeft == 1) "" else "s"} · images or PDF, 3 MB each",
                style = touchFootnoteRegular,
                color = DarwinTouchNeutral600,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 8.dp),
            )
            onRecords?.let { seeAll ->
                RecentRecords(
                    title = if (patientName != null) "$patientName's records" else "Recent records",
                    records = recentRecords,
                    onRecordClick = onRecordClick,
                    onSeeAll = seeAll,
                )
            }
            AttachOption(R.drawable.ic_image_regular, "Gallery", "Choose photos", onGallery)
            AttachOption(R.drawable.ic_camera_solid, "Camera", "Take a photo", onCamera)
            AttachOption(R.drawable.ic_file_regular, "Files", "PDF or image from your phone", onFiles)
        }
    }
}

@Composable
private fun AttachOption(icon: Int, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(DarwinTouchPrimaryBgLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = DarwinTouchPrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, style = touchBodyRegular, color = DarwinTouchNeutral1000)
            Text(subtitle, style = touchFootnoteRegular, color = DarwinTouchNeutral600)
        }
    }
}

/** Newest records in a row: one tap attaches; "See all" opens the full records screen. */
@Composable
private fun RecentRecords(
    title: String,
    records: List<MedAssistRecord>?,
    onRecordClick: (MedAssistRecord) -> Unit,
    onSeeAll: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = touchBodyRegular, color = DarwinTouchNeutral1000, modifier = Modifier.weight(1f))
        TextButton(onClick = onSeeAll) { Text("See all", style = touchFootnoteRegular, color = DarwinTouchPrimary) }
    }
    when {
        records == null -> Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = DarwinTouchPrimary)
        }
        records.isEmpty() -> Text(
            "No records yet",
            style = touchFootnoteRegular,
            color = DarwinTouchNeutral600,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        else -> LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            items(records, key = { it.id }) { record ->
                Column(modifier = Modifier.width(88.dp).clickable { onRecordClick(record) }) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarwinTouchPrimaryBgLight),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(painterResource(R.drawable.ic_file_medical_regular), null, tint = DarwinTouchPrimary, modifier = Modifier.size(24.dp))
                        record.thumbnail?.let {
                            AsyncImage(
                                model = it,
                                contentDescription = record.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                    Text(record.title, style = touchFootnoteRegular, color = DarwinTouchNeutral1000, maxLines = 1,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                    record.subtitle?.let {
                        Text(it, style = touchFootnoteRegular, color = DarwinTouchNeutral600, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
    HorizontalDivider(color = DarwinTouchNeutral100, modifier = Modifier.padding(vertical = 4.dp))
}
