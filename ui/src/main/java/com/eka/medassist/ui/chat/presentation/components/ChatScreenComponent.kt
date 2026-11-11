package com.eka.medassist.ui.chat.presentation.components

import android.app.Application
import android.content.ActivityNotFoundException
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eka.conversation.matrix.media.MatrixFile
import com.eka.medassist.ui.chat.client.MedAssistFiles
import com.eka.medassist.ui.chat.client.MedAssistPatient
import com.eka.medassist.ui.chat.client.MedAssistRecord
import com.eka.medassist.ui.chat.client.MedAssistRecordsSource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import kotlinx.coroutines.CancellationException
import com.eka.medassist.ui.chat.presentation.screens.ConversationScreen
import com.eka.medassist.ui.chat.presentation.viewmodels.MedAssistArgs
import com.eka.medassist.ui.chat.presentation.viewmodels.MedAssistViewModel
import com.eka.medassist.ui.chat.presentation.voice.rememberCameraPermission
import com.eka.medassist.ui.chat.presentation.voice.rememberMicPermission
import com.eka.ui.theme.EkaTheme
import kotlinx.coroutines.launch
import java.io.File

/**
 * Full MedAssist chat, ready to drop into any Activity's setContent: owns the ViewModel, the
 * + menu (records, gallery, camera, files) and the mic / camera permissions.
 */
@Composable
fun MedAssistChat(
    args: MedAssistArgs,
    onClose: () -> Unit,
    /**
     * Opens the host's patient picker and reports the chosen patient through `onPicked`.
     * Shows the patient chip in the chat when set.
     */
    onPickPatient: ((onPicked: (MedAssistPatient) -> Unit) -> Unit)? = null,
    /**
     * The host's medical records: a quick list of the newest in the + sheet (one tap attaches)
     * and "See all" for the host's full records screen. null hides records in +.
     */
    records: MedAssistRecordsSource? = null,
    /** Analytics sink: ("medassist_<event>", params) - opened, sent, first token, stop, errors, voice, attachments... */
    onAnalyticsEvent: ((name: String, params: Map<String, Any?>) -> Unit)? = null,
) {
    val context = LocalContext.current
    val viewModel: MedAssistViewModel = viewModel(
        key = "medassist-${args.agentId}",
        factory = MedAssistViewModel.Factory(context.applicationContext as Application, args),
    )
    val state by viewModel.state.collectAsState()
    SideEffect { viewModel.analytics = onAnalyticsEvent }
    val withMicPermission = rememberMicPermission()
    val withCameraPermission = rememberCameraPermission()
    val scope = rememberCoroutineScope()

    // Reads / resizes picked files off the main thread, then adds what fits
    val addUris: (List<Uri>, String) -> Unit = { uris, source ->
        if (uris.isNotEmpty()) scope.launch {
            viewModel.setPreparingAttachments(true)
            val files = uris.map { MedAssistFiles.fromUri(context, it) }
            viewModel.setPreparingAttachments(false)
            viewModel.addPickedAttachments(files.filterNotNull(), unreadable = files.count { it == null }, source = source)
        }
    }
    val pickDocuments = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { addUris(it, "files") }
    val pickPhotos = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = MatrixFile.MAX_FILES_PER_MESSAGE)
    ) { addUris(it, "gallery") }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> addUris(listOfNotNull(uri), "gallery") }

    // Records: downloads (if needed) + resizes the picked records' pages, spinner on + meanwhile
    val addRecords: (List<String>) -> Unit = { ids ->
        val source = records
        if (source != null && ids.isNotEmpty()) scope.launch {
            viewModel.setPreparingAttachments(true)
            val files = try {
                source.files(ids, viewModel.attachmentSlotsLeft())
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                emptyList()
            }
            val picked = files.map { MedAssistFiles.fromFile(context, it) }
            viewModel.setPreparingAttachments(false)
            if (files.isEmpty()) {
                Toast.makeText(context, "Couldn't get the selected records. Check your connection and try again.", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.addPickedAttachments(picked.filterNotNull(), unreadable = picked.count { it == null }, source = "records")
            }
        }
    }

    // Camera writes into our FileProvider; the path survives process death via rememberSaveable
    var cameraPath by rememberSaveable { mutableStateOf<String?>(null) }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val file = cameraPath?.let(::File)
        cameraPath = null
        if (file == null) return@rememberLauncherForActivityResult
        if (!saved || !file.exists() || file.length() == 0L) {
            file.delete()
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            viewModel.setPreparingAttachments(true)
            val picked = MedAssistFiles.fromFile(context, file)
            file.delete()
            viewModel.setPreparingAttachments(false)
            viewModel.addPickedAttachments(listOfNotNull(picked), unreadable = if (picked == null) 1 else 0, source = "camera")
        }
    }
    val openCamera = {
        val dir = File(context.cacheDir, "medassist_camera").apply { mkdirs() }
        val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.medassist.fileprovider", file)
        cameraPath = file.absolutePath
        try {
            takePhoto.launch(uri)
        } catch (e: ActivityNotFoundException) {
            cameraPath = null
            file.delete()
            Toast.makeText(context, "No camera app found.", Toast.LENGTH_SHORT).show()
        }
    }

    var showAttachSheet by remember { mutableStateOf(false) }
    // Newest records for the sheet's quick list; null while loading
    var recentRecords by remember { mutableStateOf<List<MedAssistRecord>?>(null) }
    LaunchedEffect(showAttachSheet, state.patient) {
        val source = records
        if (!showAttachSheet || source == null) return@LaunchedEffect
        recentRecords = null
        recentRecords = try {
            source.recent(state.patient, RECENT_RECORDS)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyList()
        }
    }

    EkaTheme {
        ConversationScreen(
            viewModel = viewModel,
            onBackClick = onClose,
            onPickFiles = { if (viewModel.checkAttachmentRoom()) showAttachSheet = true },
            onPickPatient = onPickPatient,
            askMicrophonePermission = withMicPermission,
        )
        if (showAttachSheet) {
            val slots = viewModel.attachmentSlotsLeft()
            AttachSheet(
                patientName = state.patient?.name,
                slotsLeft = slots,
                onRecords = records?.let { source ->
                    {
                        showAttachSheet = false
                        source.openPicker(state.patient, slots) { ids -> addRecords(ids) }
                    }
                },
                recentRecords = recentRecords,
                onRecordClick = { record ->
                    showAttachSheet = false
                    addRecords(listOf(record.id))
                },
                onGallery = {
                    showAttachSheet = false
                    val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    // The multi picker needs room for at least 2
                    if (slots >= 2) pickPhotos.launch(request) else pickPhoto.launch(request)
                },
                onCamera = {
                    showAttachSheet = false
                    withCameraPermission { openCamera() }
                },
                onFiles = {
                    showAttachSheet = false
                    pickDocuments.launch(arrayOf("image/png", "image/jpeg", "application/pdf"))
                },
                onDismiss = { showAttachSheet = false },
            )
        }
    }
}

/** Records shown in the + sheet's quick list. */
private const val RECENT_RECORDS = 6
