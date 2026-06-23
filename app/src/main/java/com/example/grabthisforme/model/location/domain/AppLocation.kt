package com.example.grabthisforme.model.location.domain

data class AppLocation(
    val latitude: Double,
    val longitude: Double,
    val country: String = "",
    val province: String = "",
    val city: String = "",
    val district: String = "",
    val address: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val locationLabel: String
        get() = if (isDomesticCountry()) {
            listOf(province, city, district)
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(separator = " ")
                .ifBlank { address.ifBlank { "当前位置" } }
        } else {
            country.ifBlank { address.ifBlank { "当前位置" } }
        }

    val displayText: String
        get() = locationLabel

    val provinceDisplayText: String
        get() = province.ifBlank { country }

    private fun isDomesticCountry(): Boolean {
        if (country.isBlank()) return true
        return country.contains("中国") || country.equals("China", ignoreCase = true)
    }
}
