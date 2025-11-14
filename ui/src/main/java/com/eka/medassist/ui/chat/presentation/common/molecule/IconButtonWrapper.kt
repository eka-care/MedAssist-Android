package com.eka.medassist.ui.chat.presentation.common.molecule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.chat.presentation.common.atom.IconWrapper
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral200
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral400
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral50
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimary
import com.eka.medassist.ui.chat.theme.DarwinTouchPrimaryBgLight

enum class IconButtonType {
    FILLED,
    STANDARD,
    OUTLINED,
    TONAL,
    CUSTOM,
}

class IconButtonColorsOverride(
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    val disabledContentColor: Color? = null,
    val disabledContainerColor: Color? = null
)

@Composable
fun IconButtonWrapper(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    type: IconButtonType = IconButtonType.STANDARD,
    contentDescription: String = "",
    onClick: () -> Unit,
    colors: IconButtonColorsOverride? = null,
    icon: Int,
    iconSize: Dp = 14.dp,
    borderColor: Color = DarwinTouchNeutral200
) {
    val colorsDefaults = when (type) {
        IconButtonType.FILLED -> IconButtonDefaults.filledIconButtonColors(
            containerColor = DarwinTouchPrimary,
            contentColor = DarwinTouchNeutral0,
            disabledContentColor = DarwinTouchNeutral400,
            disabledContainerColor = DarwinTouchNeutral50
        )

        IconButtonType.STANDARD, IconButtonType.CUSTOM -> IconButtonDefaults.iconButtonColors(
            containerColor = Color.Transparent,
            contentColor = DarwinTouchNeutral800,
            disabledContentColor = DarwinTouchNeutral400,
            disabledContainerColor = Color.Transparent
        )

        IconButtonType.TONAL -> IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = DarwinTouchPrimaryBgLight,
            contentColor = DarwinTouchNeutral1000,
            disabledContentColor = DarwinTouchNeutral400,
            disabledContainerColor = DarwinTouchNeutral50
        )

        IconButtonType.OUTLINED -> IconButtonDefaults.outlinedIconButtonColors(
            containerColor = Color.Transparent,
            contentColor = DarwinTouchNeutral800,
            disabledContentColor = DarwinTouchNeutral400,
            disabledContainerColor = Color.Transparent
        )
    }

    val colorsOverride = colorsDefaults.copy(
        containerColor = if (colors?.containerColor != null) colors.containerColor else colorsDefaults.containerColor,
        contentColor = if (colors?.contentColor != null) colors.contentColor else colorsDefaults.contentColor,
        disabledContentColor = if (colors?.disabledContentColor != null) colors.disabledContentColor else colorsDefaults.disabledContentColor,
        disabledContainerColor = if (colors?.disabledContainerColor != null) colors.disabledContainerColor else colorsDefaults.disabledContainerColor
    )

    val tintColor =
        if (enabled) colorsOverride.contentColor else colorsOverride.disabledContentColor

    when (type) {
        IconButtonType.FILLED -> FilledIconButton(
            enabled = enabled,
            onClick = onClick,
            modifier = modifier
                .size(40.dp),
            colors = colorsOverride,
        ) {
            IconWrapper(
                modifier = modifier
                    .size(iconSize),
                icon = icon,
                contentDescription = contentDescription,
                tint = tintColor
            )
        }

        IconButtonType.STANDARD -> IconButton(
            enabled = enabled,
            onClick = onClick,
            modifier = modifier
                .size(40.dp),
            colors = colorsOverride,
        ) {
            IconWrapper(
                modifier = modifier
                    .size(iconSize),
                icon = icon,
                contentDescription = contentDescription,
                tint = tintColor
            )
        }

        IconButtonType.OUTLINED -> OutlinedIconButton(
            enabled = enabled,
            onClick = onClick,
            modifier = modifier
                .size(40.dp),
            border = BorderStroke(1.dp, borderColor),
            colors = colorsOverride
        ) {
            IconWrapper(
                modifier = modifier
                    .size(iconSize),
                icon = icon,
                contentDescription = contentDescription,
                tint = tintColor,
            )
        }

        IconButtonType.TONAL -> FilledTonalIconButton(
            enabled = enabled,
            onClick = onClick,
            modifier = modifier
                .size(40.dp),
            colors = colorsOverride,
        ) {
            IconWrapper(
                modifier = modifier
                    .size(iconSize),
                icon = icon,
                contentDescription = contentDescription,
                tint = tintColor
            )
        }

        IconButtonType.CUSTOM -> IconWrapper(
            modifier = modifier
                .size(iconSize)
                .clickable(
                    role = Role.Button,
                    enabled = enabled,
                    interactionSource = null,
                    indication = ripple(
                        bounded = false,
                        radius = iconSize
                    ),
                    onClick = onClick
                ),
            icon = icon,
            contentDescription = contentDescription,
            tint = tintColor,
        )
    }
}