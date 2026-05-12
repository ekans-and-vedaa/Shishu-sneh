package com.shishusneh

object VaccineTranslations {
    fun getTranslatedName(name: String, isHindi: Boolean): String {
        return if (isHindi) {
            when (name) {
                "BCG" -> "बीसीजी (BCG)"
                "OPV-0 & Hep-B" -> "ओपीवी-0 और हेप-बी"
                "OPV-1 & Penta-1" -> "ओपीवी-1 और पेंटा-1"
                "OPV-2 & Penta-2" -> "ओपीवी-2 और पेंटा-2"
                "OPV-3 & Penta-3" -> "ओपीवी-3 और पेंटा-3"
                "Measles/MR 1" -> "खसरा/एमआर 1"
                "Vitamin A-1" -> "विटामिन ए-1"
                "DPT Booster 1" -> "डीपीटी बूस्टर 1"
                "OPV Booster" -> "ओपीवी बूस्टर"
                else -> name
            }
        } else name
    }

    fun getTranslatedDescription(name: String, isHindi: Boolean): String {
        return if (isHindi) {
            when (name) {
                "BCG" -> "तपेदिक (TB) से बचाव के लिए जन्म के समय दिया जाता है।"
                "OPV-0 & Hep-B" -> "पोलियो और हेपेटाइटिस बी से बचाव के लिए जन्म के समय।"
                "OPV-1 & Penta-1" -> "पोलियो, काली खांसी, टिटनेस, हेपेटाइटिस बी और हिब से बचाव।"
                "OPV-2 & Penta-2" -> "पोलियो, काली खांसी, टिटनेस, हेपेटाइटिस बी और हिब से बचाव।"
                "OPV-3 & Penta-3" -> "पोलियो, काली खांसी, टिटनेस, हेपेटाइटिस बी और हिब से बचाव।"
                "Measles/MR 1" -> "खसरा और रूबेला से बचाव के लिए 9 महीने पर दिया जाता है।"
                "Vitamin A-1" -> "आंखों की रोशनी और रोग प्रतिरोधक क्षमता बढ़ाने के लिए।"
                "DPT Booster 1" -> "डिप्थीरिया, काली खांसी और टिटनेस से बचाव के लिए बूस्टर डोज़।"
                "OPV Booster" -> "पोलियो से दीर्घकालिक सुरक्षा सुनिश्चित करने के लिए बूस्टर डोज़।"
                else -> ""
            }
        } else {
            when (name) {
                "BCG" -> "Given at birth to protect against Tuberculosis (TB)."
                "OPV-0 & Hep-B" -> "Given at birth for protection against Polio and Hepatitis B."
                "OPV-1 & Penta-1" -> "Protects against Polio, DPT, HepB, and HiB (6 Weeks)."
                "OPV-2 & Penta-2" -> "Protects against Polio, DPT, HepB, and HiB (10 Weeks)."
                "OPV-3 & Penta-3" -> "Protects against Polio, DPT, HepB, and HiB (14 Weeks)."
                "Measles/MR 1" -> "Protects against Measles and Rubella (9 Months)."
                "Vitamin A-1" -> "Essential for vision and boosting overall immunity."
                "DPT Booster 1" -> "Booster dose against Diphtheria, Pertussis, and Tetanus."
                "OPV Booster" -> "Booster dose to ensure long-term immunity against Polio."
                else -> ""
            }
        }
    }

    fun getTranslatedTimeframe(timeframe: String, isHindi: Boolean): String {
        if (!isHindi) return timeframe
        return when (timeframe) {
            "At Birth" -> "जन्म के समय"
            "6 Weeks" -> "6 सप्ताह"
            "10 Weeks" -> "10 सप्ताह"
            "14 Weeks" -> "14 सप्ताह"
            "9 Months" -> "9 महीने"
            "16-24 Months" -> "16-24 महीने"
            "5-6 Years" -> "5-6 साल"
            else -> timeframe
        }
    }
}
