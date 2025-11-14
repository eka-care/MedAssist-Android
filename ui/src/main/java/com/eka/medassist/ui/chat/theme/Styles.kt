package com.eka.medassist.ui.chat.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.eka.medassist.ui.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val fontName = GoogleFont("Inter")

val ekaFontFamily = FontFamily(
    Font(googleFont = fontName, fontProvider = provider)
)

val touchTitle1Regular = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Normal,
    fontSize = 28.sp,
    lineHeight = 36.sp,
)

val touchTitle1Bold = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 28.sp,
    lineHeight = 36.sp,
)

val touchTitle3Bold = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 20.sp,
    lineHeight = 28.sp,
)

val touchTitle3Regular = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Normal,
    fontSize = 20.sp,
    lineHeight = 28.sp,
)

val touchTitle2Regular = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Normal,
    fontSize = 24.sp,
    lineHeight = 32.sp,
)

val touchLargeTitleRegular = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Normal,
    fontSize = 34.sp,
    lineHeight = 44.sp,
)

val touchBody2Bold = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 13.sp,
    lineHeight = 20.sp,
)

val touchBody2Medium = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Medium,
    fontStyle = FontStyle.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
)

val touchCaption1Regular = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
)

val touchCaption2SemiBold = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 11.sp,
    lineHeight = 16.sp,
)

val touchCaption2ExtraBold = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.ExtraBold,
    fontStyle = FontStyle.Normal,
    fontSize = 11.sp,
    lineHeight = 16.sp,
)

val touchCaption1Bold = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
)

val touchCaption2Bold = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 10.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.1.sp,
)

val touchCalloutRegular = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
)

val touchCalloutBold = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
)

val touchBodyRegular = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)

val touchBody3Regular = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
)

val touchBodyBold = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)

val touchLabelRegular = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
)

val touchLabelBold = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
)

val touchSubheadlineRegular = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Normal,
    fontSize = 15.sp,
    lineHeight = 24.sp,
)

val touchSubheadlineBold = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 15.sp,
    lineHeight = 24.sp,
)

val touchFootnoteRegular = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Normal,
    fontSize = 13.sp,
    lineHeight = 20.sp,
)

val touchFootnoteBold = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 13.sp,
    lineHeight = 20.sp,
)

val touchHeadlineBold = TextStyle.Default.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 16.sp,
    lineHeight = 20.sp,
)