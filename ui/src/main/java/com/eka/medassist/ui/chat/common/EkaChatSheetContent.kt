package com.eka.chat.common

sealed class EkaChatSheetContent {
    object Voice2RxInitialBottomSheet : EkaChatSheetContent()
    object Voice2RxPatientBottomSheet : EkaChatSheetContent()
    object Voice2RxMedicalBottomSheet : EkaChatSheetContent()
    object Voice2RxPatientDetailBottomSheet : EkaChatSheetContent()
    object Voice2RxOnboardingBottomSheet : EkaChatSheetContent()
    object Voice2RxErrorBottomSheet : EkaChatSheetContent()
    object Voice2RxRecordingModeSelectionBottomSheet : EkaChatSheetContent()
    object Voice2RxLanguageSelectionBottomSheet : EkaChatSheetContent()
    object Voice2RxOutputFormatSelectionBottomSheet : EkaChatSheetContent()
}