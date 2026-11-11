package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.background
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
            onRecords?.let {
                AttachOption(
                    icon = R.drawable.ic_file_medical_regular,
                    title = if (patientName != null) "$patientName's records" else "Records",
                    subtitle = if (patientName != null) "Reports and prescriptions on file" else "All records in your clinic",
                    onClick = it,
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
