package com.example.grabthisforme.model.XAxisValueFormatter

class XAxisValueFormatter(
    private val labels: List<String>
) : com.github.mikephil.charting.formatter.ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return labels[value.toInt() % labels.size]
    }
}
