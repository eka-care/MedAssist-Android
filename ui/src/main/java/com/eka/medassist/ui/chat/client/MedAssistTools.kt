package com.eka.medassist.ui.chat.client

import com.eka.conversation.matrix.ClientToolDeclaration
import kotlinx.serialization.json.JsonObject

/**
 * The host app's client tools: functions the agent can call during a reply (same protocol as
 * the web SDK), e.g. get_patient_context. The SDK declares [tools] when a chat starts, runs
 * [call] when the agent asks, and sends the result back. [patient] is the chat's current
 * patient (the chip), so tools never need to trust a patient id from the agent.
 * Throw [com.eka.conversation.matrix.ClientToolException] for a failure the agent should see.
 */
interface MedAssistToolSource {
    val tools: List<ClientToolDeclaration>
    suspend fun call(name: String, arguments: JsonObject, patient: MedAssistPatient?): JsonObject
}
