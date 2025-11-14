package com.eka.medassist.ui.chat.presentation.common.molecule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.chat.presentation.common.atom.IconWrapper
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral100
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral200
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral400
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimaryBgLight
import com.eka.medassist.ui.chat.theme.touchCalloutBold

enum class ButtonWrapperType {
    FILLED,
    OUTLINED,
    TEXT,
    ELEVATED,
    TONAL,
    EDITSIZE
}

class ButtonWrapperColorsOverride(
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    val disabledContentColor: Color? = null,
    val disabledContainerColor: Color? = null
)

@Composable
fun ButtonWrapper(
    modifier: Modifier = Modifier,
    type: ButtonWrapperType = ButtonWrapperType.FILLED,
    text: String,
    showLoader: Boolean = false,
    onClick: () -> Unit,
    enabled: Boolean = true,
    colors: ButtonWrapperColorsOverride? = null,
    iconSize: Dp = 14.dp,
    icon: Int? = null,
    borderColor: Color = DarwinTouchNeutral200,
    shape: Shape = ButtonDefaults.shape,
    textStyle: TextStyle = touchCalloutBold,
    contentPaddingValues: PaddingValues? = null
) {
    val colorsDefaults = when (type) {
        ButtonWrapperType.FILLED -> ButtonDefaults.buttonColors(
            containerColor = DarwinTouchPrimary,
            contentColor = DarwinTouchNeutral0,
            disabledContentColor = DarwinTouchNeutral400,
            disabledContainerColor = DarwinTouchNeutral100
        )

        ButtonWrapperType.OUTLINED -> ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = DarwinTouchPrimary,
            disabledContentColor = DarwinTouchNeutral400,
            disabledContainerColor = Color.Transparent
        )

        ButtonWrapperType.TEXT -> ButtonDefaults.textButtonColors(
            containerColor = Color.Transparent,
            contentColor = DarwinTouchPrimary,
            disabledContentColor = DarwinTouchNeutral400,
            disabledContainerColor = Color.Transparent
        )

        ButtonWrapperType.ELEVATED -> ButtonDefaults.elevatedButtonColors(
            containerColor = Color.Transparent,
            contentColor = DarwinTouchPrimary,
            disabledContentColor = DarwinTouchNeutral400,
            disabledContainerColor = DarwinTouchNeutral100
        )

        ButtonWrapperType.TONAL -> ButtonDefaults.filledTonalButtonColors(
            containerColor = DarwinTouchPrimaryBgLight,
            contentColor = DarwinTouchNeutral800,
            disabledContentColor = DarwinTouchNeutral400,
            disabledContainerColor = DarwinTouchNeutral100
        )

        ButtonWrapperType.EDITSIZE -> ButtonDefaults.buttonColors(
            containerColor = DarwinTouchPrimary,
            contentColor = DarwinTouchNeutral0,
            disabledContentColor = DarwinTouchNeutral400,
            disabledContainerColor = DarwinTouchNeutral100
        )
    }

    val colorsOverride = colorsDefaults.copy(
        containerColor = if (colors?.containerColor != null) colors.containerColor else colorsDefaults.containerColor,
        contentColor = if (colors?.contentColor != null) colors.contentColor else colorsDefaults.contentColor,
        disabledContentColor = if (colors?.disabledContentColor != null) colors.disabledContentColor else colorsDefaults.disabledContentColor,
        disabledContainerColor = if (colors?.disabledContainerColor != null) colors.disabledContainerColor else colorsDefaults.disabledContainerColor
    )

    val iconColor =
        if (enabled) colorsOverride.contentColor else colorsOverride.disabledContentColor

    when (type) {
        ButtonWrapperType.FILLED -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = colorsOverride,
            shape = shape,
            contentPadding = PaddingValues(0.dp),
        ) {
            ButtonContent(
                showLoader = showLoader,
                icon = icon,
                text = text,
                iconSize = iconSize,
                textStyle = textStyle,
                iconColor = iconColor
            )
        }

        ButtonWrapperType.OUTLINED -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = colorsOverride,
            shape = shape,
            contentPadding = PaddingValues(0.dp),
            border = BorderStroke(1.dp, borderColor),
        ) {
            ButtonContent(
                showLoader = showLoader,
                icon = icon,
                text = text,
                textStyle = textStyle,
                iconSize = iconSize,
                iconColor = iconColor
            )

        }

        ButtonWrapperType.TEXT -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = colorsOverride,
            contentPadding = PaddingValues(0.dp),
        ) {
            ButtonContent(
                showLoader = showLoader,
                icon = icon,
                text = text,
                iconSize = iconSize,
                textStyle = textStyle,
                iconColor = iconColor
            )
        }

        ButtonWrapperType.ELEVATED -> ElevatedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = colorsOverride,
            contentPadding = PaddingValues(0.dp),
        ) {
            ButtonContent(
                showLoader = showLoader,
                icon = icon,
                text = text,
                iconSize = iconSize,
                textStyle = textStyle,
                iconColor = iconColor
            )
        }

        ButtonWrapperType.TONAL -> FilledTonalButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = colorsOverride,
            contentPadding = PaddingValues(0.dp),
        ) {
            ButtonContent(
                showLoader = showLoader,
                icon = icon,
                text = text,
                iconSize = iconSize,
                iconColor = iconColor,
                textStyle = textStyle,
            )
        }

        ButtonWrapperType.EDITSIZE -> FilledTonalButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = colorsOverride,
            contentPadding = contentPaddingValues ?: PaddingValues(0.dp),
        ) {
            ButtonContent(
                showLoader = showLoader,
                icon = icon,
                text = text,
                iconSize = iconSize,
                iconColor = iconColor,
                textStyle = textStyle,
                paddingContentValues = contentPaddingValues
            )
        }
    }
}

@Composable
private fun ButtonContent(
    showLoader: Boolean = false,
    icon: Int? = null,
    text: String,
    iconSize: Dp,
    iconColor: Color,
    textStyle: TextStyle = touchCalloutBold,
    paddingContentValues: PaddingValues? = null
) {
    Row(
        modifier = Modifier
            .padding(
                paddingContentValues ?: PaddingValues(
                    start = if (icon == null) 16.dp else 12.dp,
                    top = 10.dp,
                    end = 16.dp,
                    bottom = 10.dp
                )
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showLoader) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp)
            )
        }
        if (icon != null) {
            IconWrapper(
                icon = icon,
                contentDescription = "",
                modifier = Modifier.size(iconSize),
                tint = iconColor
            )
        }
        Text(
            text = text,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
