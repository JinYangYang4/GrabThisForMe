package com.example.grabthisforme.model.location.domain

data class AppLocation(
    val latitude: Double,
    val longitude: Double,
    val city: String = "",
    val district: String = "",
    val address: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val displayText: String
        get() = listOf(city,district)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(separator = " ")
            .ifBlank { address.ifBlank { "当前位置" } }
}
