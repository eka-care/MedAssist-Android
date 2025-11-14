package com.eka.medassist.ui.chat.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.R
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.touchFootnoteRegular

@Composable
fun EkaChatBotScreenComponent(
    icon: Int,
    headline: String,
    headlineStyle: TextStyle,
    subText: String,
    iconSize: Dp,
    onClick: (() -> Unit)? = null,
    trailingIcon: Boolean? = false,
    trailingIconClick: (() -> Unit)? = null,
) {
    val screenWidth = LocalWindowInfo.current.containerSize.width.dp
    Row(
        modifier = Modifier
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(
                start = 16.dp,
                end = if (trailingIcon == true) 0.dp else 8.dp,
                top = 12.dp,
                bottom = 12.dp
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = DarwinTouchNeutral800,
            modifier = Modifier
                .size(iconSize)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .width(screenWidth.times(0.7f))
        ) {
            Text(
                text = headline,
                style = headlineStyle,
                color = DarwinTouchNeutral1000
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subText,
                style = touchFootnoteRegular,
                color = DarwinTouchNeutral800
            )
        }
        if (trailingIcon == true) {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { trailingIconClick?.invoke() },
                modifier = Modifier.align(Alignment.Top)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right_regular),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}


//@Composable
//fun BottomBarOtherAction(
//    selectedPatientName: String?,
//    openPatientSelection: () -> Unit,
//    openMedicalRecords: () -> Unit,
//) {
//    Row(
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        AnimatedVisibility(
//            visible = true,
//            enter = fadeIn() + slideInHorizontally(
//                initialOffsetX = { -it },
//                animationSpec = tween(
//                    durationMillis = 500,
//                    easing = LinearEasing
//                )
//            ),
//            exit = fadeOut() + slideOutHorizontally(
//                targetOffsetX = { -it },
//                animationSpec = tween(
//                    durationMillis = 500,
//                    easing = LinearEasing
//                )
//            )
//        )
//        {
//            Spacer(modifier = Modifier.width(8.dp))
//            IconButton(onClick = {
//                openMedicalRecords()
//            }) {
//                Image(
//                    painter = painterResource(id = R.drawable.ic_image_regular),
//                    contentDescription = "camera",
//                    colorFilter = ColorFilter.tint(DarwinTouchNeutral800),
//                    modifier = Modifier
//                        .size(16.dp)
//                        .clickable {
//                            val params = JSONObject()
//                            params.put("type", "records")
//                            openMedicalRecords()
//                        }
//                )
//            }
//            Spacer(modifier = Modifier.width(8.dp))
//        }
//        if (!selectedPatientName.isNullOrEmpty()) {
//            EkaBotTag(
//                tagName = selectedPatientName,
//                backgroundColor = DarwinTouchPrimaryBgLight,
//                style = touchCalloutBold
//            )
//        } else {
//            if (showPatientSelection) {
//                IconButton(
//                    onClick = {
//                        val params = JSONObject()
//                        params.put("type", "select_pt")
//                        openPatientSelection()
//                    }
//                ) {
//                    Image(
//                        painter = painterResource(id = R.drawable.ic_user_regular),
//                        contentDescription = "patient",
//                        colorFilter = ColorFilter.tint(DarwinTouchNeutral800),
//                        modifier = Modifier
//                            .size(16.dp)
//                    )
//                }
//            }
//        }
//    }
//}