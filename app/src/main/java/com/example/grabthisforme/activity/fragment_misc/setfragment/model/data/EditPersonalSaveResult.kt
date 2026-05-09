package com.example.grabthisforme.activity.fragment_misc.setfragment.model.data

data class EditPersonalSaveResult(
    val success: Boolean,
    val message: String,
    val eventId: Long = System.currentTimeMillis()
)