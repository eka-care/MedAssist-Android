package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.client.LocalMedAssistLabels
import com.eka.medassist.ui.chat.client.MedAssistPatient
import com.eka.medassist.ui.chat.presentation.common.molecule.IconButtonType
import com.eka.medassist.ui.chat.presentation.common.molecule.IconButtonWrapper
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimaryBgLight
import com.eka.medassist.ui.chat.theme.touchCalloutRegular
import com.eka.medassist.ui.chat.theme.touchFootnoteBold

/**
 * Patient the chat is about, under the header: "RK  Ravi Kumar · 42 M  ✕", or "+ Attach patient".
 * Tapping the chip changes the patient; ✕ removes it.
 */
@Composable
fun PatientChipRow(
    patient: MedAssistPatient?,
    enabled: Boolean,
    onPick: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (patient == null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DarwinTouchPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable(enabled = enabled, onClick = onPick)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus_regular),
                    contentDescription = null,
                    tint = DarwinTouchPrimary,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(LocalMedAssistLabels.current.attachPatient, style = touchCalloutRegular, color = DarwinTouchPrimary)
            }
            return@Row
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DarwinTouchPrimaryBgLight)
                .clickable(enabled = enabled, onClick = onPick)
                .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(DarwinTouchPrimary),
                contentAlignment = Alignment.Center,
            ) {
                Text(patient.initials, style = touchFootnoteBold.copy(fontSize = 10.sp), color = DarwinTouchNeutral0)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = patient.label,
                style = touchCalloutRegular,
                color = DarwinTouchPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false).padding(end = 2.dp),
            )
            Spacer(Modifier.width(8.dp))
            // A size on the wrapper also sizes the glyph, so the cross drew at 32dp. Size the icon.
            IconButtonWrapper(
                type = IconButtonType.CUSTOM,
                enabled = enabled,
                onClick = onRemove,
                icon = R.drawable.ic_xmark_regular,
                iconSize = 12.dp,
                contentDescription = LocalMedAssistLabels.current.removePatient,
            )
        }
    }
}

/**
 * The attached patient inside the composer's bottom row: "RK  Ravi Kumar · 42 M  ✕",
 * or a dashed "Attach patient". Tap to change the patient, ✕ to remove it.
 * With [onPick] / [onRemove] null it only shows who the chat is about (no tap, no ✕).
 */
@Composable
fun ComposerPatientChip(
    patient: MedAssistPatient?,
    enabled: Boolean,
    onPick: (() -> Unit)?,
    onRemove: (() -> Unit)?,
) {
    val shape = RoundedCornerShape(15.dp)
    if (patient == null) {
        Row(
            modifier = Modifier
                .height(30.dp)
                .clip(shape)
                .drawBehind {
                    drawRoundRect(
                        color = DarwinTouchPrimary.copy(alpha = 0.45f),
                        cornerRadius = CornerRadius(15.dp.toPx()),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())),
                        ),
                    )
                }
                .then(if (onPick != null) Modifier.clickable(enabled = enabled, onClick = onPick) else Modifier)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_user_regular),
                contentDescription = null,
                tint = DarwinTouchPrimary,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(LocalMedAssistLabels.current.attachPatient, style = touchCalloutRegular.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp), color = DarwinTouchPrimary, maxLines = 1)
        }
        return
    }
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(shape)
            .background(DarwinTouchPrimaryBgLight)
            .then(if (onPick != null) Modifier.clickable(enabled = enabled, onClick = onPick) else Modifier)
            .padding(start = 4.dp, end = if (onRemove != null) 2.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(DarwinTouchPrimary),
            contentAlignment = Alignment.Center,
        ) {
            Text(patient.initials, style = touchFootnoteBold.copy(fontSize = 9.sp), color = DarwinTouchNeutral0)
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = patient.label,
            style = touchCalloutRegular.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp),
            color = DarwinTouchPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (onRemove != null) {
            IconButtonWrapper(
                type = IconButtonType.CUSTOM,
                enabled = enabled,
                onClick = onRemove,
                icon = R.drawable.ic_xmark_regular,
                iconSize = 10.dp,
                contentDescription = LocalMedAssistLabels.current.removePatient,
            )
        }
    }
}
