package com.shishusneh

data class GrowthPoint(val month: Int, val weight: Float, val height: Float)

object GrowthData {
    val boyAverages = listOf(
        GrowthPoint(0, 3.3f, 49.9f),
        GrowthPoint(1, 4.5f, 54.7f),
        GrowthPoint(2, 5.6f, 58.4f),
        GrowthPoint(3, 6.4f, 61.4f),
        GrowthPoint(4, 7.0f, 63.9f),
        GrowthPoint(5, 7.5f, 65.9f),
        GrowthPoint(6, 7.9f, 67.6f),
        GrowthPoint(7, 8.3f, 69.2f),
        GrowthPoint(8, 8.6f, 70.6f),
        GrowthPoint(9, 8.9f, 72.0f),
        GrowthPoint(10, 9.2f, 73.3f),
        GrowthPoint(11, 9.4f, 74.5f),
        GrowthPoint(12, 9.6f, 75.7f)
    )

    val girlAverages = listOf(
        GrowthPoint(0, 3.2f, 49.1f),
        GrowthPoint(1, 4.2f, 53.7f),
        GrowthPoint(2, 5.1f, 57.1f),
        GrowthPoint(3, 5.8f, 59.8f),
        GrowthPoint(4, 6.4f, 62.1f),
        GrowthPoint(5, 6.9f, 64.0f),
        GrowthPoint(6, 7.3f, 65.7f),
        GrowthPoint(7, 7.6f, 67.3f),
        GrowthPoint(8, 7.9f, 68.7f),
        GrowthPoint(9, 8.2f, 70.1f),
        GrowthPoint(10, 8.5f, 71.5f),
        GrowthPoint(11, 8.7f, 72.8f),
        GrowthPoint(12, 8.9f, 74.0f)
    )

    fun getAverageForMonth(month: Int, gender: String): GrowthPoint {
        val list = if (gender.lowercase().contains("girl") || gender.contains("स्त्री") || gender.contains("लड़की")) girlAverages else boyAverages
        return list.getOrNull(month) ?: list.last()
    }

    fun analyze(currentWeight: Float, currentHeight: Float, average: GrowthPoint, isHindi: Boolean): AnalysisResult {
        val weightStatus = when {
            currentWeight < average.weight * 0.85f -> if (isHindi) "कम वजन" else "Underweight"
            currentWeight > average.weight * 1.15f -> if (isHindi) "अधिक वजन" else "Overweight"
            else -> if (isHindi) "स्वस्थ" else "Healthy"
        }

        val heightStatus = when {
            currentHeight < average.height * 0.95f -> if (isHindi) "कम ऊंचाई" else "Short"
            currentHeight > average.height * 1.05f -> if (isHindi) "अच्छी ऊंचाई" else "Tall"
            else -> if (isHindi) "सामान्य" else "Normal"
        }

        val suggestion = when (weightStatus) {
            "Underweight", "कम वजन" -> if (isHindi) 
                "बच्चे के आहार में कैलोरी बढ़ाएं। यदि बच्चा 6 महीने से छोटा है, तो स्तनपान बढ़ाएं। डॉक्टर से सलाह लें।" 
                else "Increase calorie intake. If the baby is under 6 months, increase breastfeeding. Consult a pediatrician."
            "Overweight", "अधिक वजन" -> if (isHindi) 
                "सक्रिय खेल को प्रोत्साहित करें। मीठे पेय और स्नैक्स से बचें। डॉक्टर से परामर्श करें।" 
                else "Encourage active play. Avoid sugary drinks and snacks. Consult a pediatrician."
            else -> if (isHindi) 
                "आपका बच्चा स्वस्थ विकास कर रहा है! पौष्टिक आहार और नियमित जांच जारी रखें।" 
                else "Your baby is growing healthily! Continue nutritious feeding and regular checkups."
        }

        return AnalysisResult(weightStatus, heightStatus, suggestion)
    }
}

data class AnalysisResult(val weightStatus: String, val heightStatus: String, val suggestion: String)
