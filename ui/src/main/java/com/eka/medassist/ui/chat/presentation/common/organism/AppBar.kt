package com.eka.medassist.ui.chat.presentation.common.organism

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral0
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral1000
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral200
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral600
import com.eka.medassist.ui.chat.theme.DarwinTouchNeutral800
import com.eka.medassist.ui.chat.theme.touchFootnoteRegular
import com.eka.medassist.ui.chat.theme.touchLargeTitleRegular
import com.eka.medassist.ui.chat.theme.touchTitle1Regular
import com.eka.medassist.ui.chat.theme.touchTitle3Bold

enum class AppBarType {
    CENTER_ALIGNED,
    SMALL,
    MEDIUM,
    LARGE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    type: AppBarType = AppBarType.SMALL,
    title: String,
    subTitle: String? = null,
    actions: @Composable() (RowScope.() -> Unit) = {},
    navigationIcon: @Composable () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    containerColor: Color = DarwinTouchNeutral0,
    titleColor: Color = DarwinTouchNeutral1000,
    iconColor: Color = DarwinTouchNeutral800,
    borderColor: Color = DarwinTouchNeutral200
) {
    when (type) {
        AppBarType.CENTER_ALIGNED -> CenterAlignedTopAppBar(
            modifier = modifier.drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = size.height + strokeWidth / 2
                drawLine(
                    color = borderColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        style = touchTitle3Bold,
                        color = DarwinTouchNeutral1000,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subTitle.isNullOrEmpty()) {
                        Text(
                            text = subTitle,
                            style = touchFootnoteRegular,
                            color = DarwinTouchNeutral600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = containerColor,
                titleContentColor = titleColor,
                navigationIconContentColor = iconColor,
                actionIconContentColor = iconColor,
                scrolledContainerColor = Color.Unspecified
            ),
            scrollBehavior = scrollBehavior,
            navigationIcon = navigationIcon
        )

        AppBarType.SMALL -> TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = touchTitle3Bold,
                        color = DarwinTouchNeutral1000,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subTitle.isNullOrEmpty()) {
                        Text(
                            text = subTitle,
                            style = touchFootnoteRegular,
                            color = DarwinTouchNeutral600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            modifier = modifier.drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = size.height + strokeWidth / 2
                drawLine(
                    color = borderColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            },
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                titleContentColor = titleColor,
                navigationIconContentColor = iconColor,
                actionIconContentColor = iconColor,
                scrolledContainerColor = Color.Unspecified
            ),
            scrollBehavior = scrollBehavior
        )

        AppBarType.MEDIUM -> MediumTopAppBar(
            modifier = modifier.drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = size.height + strokeWidth / 2
                drawLine(
                    color = borderColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            },
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        style = touchTitle1Regular,
                        color = DarwinTouchNeutral1000,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subTitle.isNullOrEmpty()) {
                        Text(
                            text = subTitle,
                            style = touchFootnoteRegular,
                            color = DarwinTouchNeutral600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = containerColor,
                titleContentColor = titleColor,
                navigationIconContentColor = iconColor,
                actionIconContentColor = iconColor,
                scrolledContainerColor = Color.Unspecified
            ),
            scrollBehavior = scrollBehavior,
            navigationIcon = navigationIcon
        )

        AppBarType.LARGE -> LargeTopAppBar(
            modifier = modifier.drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = size.height + strokeWidth / 2
                drawLine(
                    color = borderColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            },
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        style = touchLargeTitleRegular,
                        color = DarwinTouchNeutral1000,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subTitle.isNullOrEmpty()) {
                        Text(
                            text = subTitle,
                            style = touchFootnoteRegular,
                            color = DarwinTouchNeutral600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = containerColor,
                titleContentColor = titleColor,
                navigationIconContentColor = iconColor,
                actionIconContentColor = iconColor,
                scrolledContainerColor = Color.Unspecified
            ),
            scrollBehavior = scrollBehavior,
            navigationIcon = navigationIcon
        )
    }
}