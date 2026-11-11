package com.eka.medassist.ui.chat

import com.eka.medassist.ui.chat.client.MedAssistDoctor
import com.eka.medassist.ui.chat.client.MedAssistPatient
import org.junit.Assert.assertEquals
import org.junit.Test

class PeopleLabelsTest {

    @Test
    fun patientChipLabel() {
        assertEquals("Ravi Kumar · 42 M", MedAssistPatient("Ravi Kumar", 42, "M").label)
        assertEquals("Ravi Kumar", MedAssistPatient("Ravi Kumar").label)
        assertEquals("Ravi Kumar · F", MedAssistPatient("Ravi Kumar", sex = "F").label)
    }

    @Test
    fun patientInitials() {
        assertEquals("RK", MedAssistPatient("ravi kumar sharma").initials)
        assertEquals("A", MedAssistPatient("  asha ").initials)
    }

    @Test
    fun doctorGreetingName() {
        assertEquals("Dr. Imtiyaz", MedAssistDoctor("Imtiyaz").displayName)
        assertEquals("Dr. Imtiyaz", MedAssistDoctor("Imtiyaz", "Dr.").displayName)
        assertEquals("Prof. Rao", MedAssistDoctor("Rao", "Prof").displayName)
    }
}
