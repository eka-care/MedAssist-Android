package com.eka.medassist.ui.chat.client

import java.io.File

/** One record shown in the + sheet's quick list. */
data class MedAssistRecord(
    val id: String,
    /** e.g. "Lab report". */
    val title: String,
    /** e.g. "12 Nov 2026". */
    val subtitle: String? = null,
    /** Local path or URL of a preview image. */
    val thumbnail: String? = null,
)

/**
 * The host's medical records, for "attach a record" in the chat: a quick list of the newest
 * ones right in the + sheet (one tap attaches) and "See all" for the host's full records screen.
 */
interface MedAssistRecordsSource {
    /** Newest first: the attached patient's records, or all clinic records when [patient] is null. */
    suspend fun recent(patient: MedAssistPatient?, limit: Int): List<MedAssistRecord>

    /** Local images / PDFs of these records (download them if needed). */
    suspend fun files(recordIds: List<String>, maxFiles: Int): List<File>

    /** Opens the full records screen to pick from; report the picked record ids. */
    fun openPicker(patient: MedAssistPatient?, maxFiles: Int, onPicked: (recordIds: List<String>) -> Unit)
}
