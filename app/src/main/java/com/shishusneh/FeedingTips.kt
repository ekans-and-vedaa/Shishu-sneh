package com.shishusneh

import java.util.Calendar

object FeedingTips {

    /**
     * Returns a daily tip for the baby based on age and language.
     */
    fun getBabyTip(months: Int, isHindi: Boolean): String {
        val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val tipIndex = (dayOfMonth % 10)

        return if (isHindi) {
            getHindiBabyTips(months)[tipIndex]
        } else {
            getEnglishBabyTips(months)[tipIndex]
        }
    }

    /**
     * Returns a daily tip for the mother based on the baby's age and language.
     */
    fun getMotherTip(months: Int, isHindi: Boolean): String {
        val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val tipIndex = (dayOfMonth % 10)

        return if (isHindi) {
            getHindiMotherTips(months)[tipIndex]
        } else {
            getEnglishMotherTips(months)[tipIndex]
        }
    }

    // Deprecated but kept for compatibility during transition
    fun getTipForAge(months: Int, isHindi: Boolean): String = getBabyTip(months, isHindi)

    private fun getEnglishBabyTips(months: Int): List<String> {
        return when {
            months < 6 -> listOf(
                "Mother's milk is the complete meal. No water or honey is needed until 6 months.",
                "Breast milk contains antibodies that protect your baby from common childhood illnesses.",
                "Feed your baby on demand, usually 8 to 12 times in a 24-hour period.",
                "Ensure a good latch to prevent nipple soreness and ensure baby gets enough milk.",
                "The more the baby nurses, the more milk your body will produce.",
                "If baby is fussy, try skin-to-skin contact to soothe them and encourage feeding.",
                "Breast milk is 80% water; your baby does not need extra water even in hot weather.",
                "Exclusive breastfeeding helps you bond deeply with your little one.",
                "Rest when the baby rests; a well-rested mother produces milk more easily.",
                "Avoid using bottles or pacifiers early on to prevent nipple confusion."
            )
            months == 6 -> listOf(
                "Time to start 'Annaprashan'! Begin with well-mashed dalia or khichdi.",
                "Start with just 2-3 spoons of mashed food twice a day.",
                "Mashed banana is an excellent first food for energy.",
                "Introduce one food at a time to check for any allergies.",
                "The food should be thick enough to stay on a spoon, not runny like water.",
                "Continue breastfeeding along with starting solid foods.",
                "Boil and mash vegetables like carrots or potatoes until they are very soft.",
                "Do not add salt or sugar to the baby's food until they are 1 year old.",
                "Always wash your hands and the baby's bowl thoroughly before feeding.",
                "Be patient; it might take 10 tries for a baby to accept a new taste!"
            )
            months in 7..8 -> listOf(
                "Introduce different tastes. Give mashed vegetables and fruits 3 times a day.",
                "Add a teaspoon of ghee or oil to the baby's khichdi for healthy fats.",
                "Finely mashed dal and rice provide great protein for growth.",
                "Encourage the baby to touch and feel the texture of the food.",
                "Offer small sips of clean, boiled water from a cup after meals.",
                "Try thick ragi porridge (nachni) for iron and calcium.",
                "Avoid fruit juices; give whole mashed fruits like papaya or chikoo instead.",
                "Maintain a consistent feeding schedule to develop good habits.",
                "Ensure the baby is sitting upright while eating to prevent choking.",
                "Breast milk is still a very important source of nutrition."
            )
            months in 9..11 -> listOf(
                "Baby can now try finely chopped family foods. Encourage picking up small bits.",
                "Give small pieces of soft roti soaked in dal or milk.",
                "Encourage 'finger foods' like small pieces of boiled potato or soft fruit.",
                "Offer 3 to 4 small meals a day plus healthy snacks.",
                "Baby might try to hold the spoon now—let them try, even if it's messy!",
                "Incorporate finely shredded pieces of cooked egg or soft paneer.",
                "Make sure food is soft enough to be mashed by the baby's gums.",
                "Avoid small, hard foods like whole nuts or grapes which are choking hazards.",
                "Include a variety of colors in their bowl (green spinach, yellow dal, orange pumpkin).",
                "Mealtime is social time; let the baby sit with the family during dinner."
            )
            else -> listOf(
                "The baby can now eat almost everything the family eats, but with less spice.",
                "Ensure 3 main meals and 2 healthy snacks like fruit or curd daily.",
                "Cow's milk can now be introduced in a cup.",
                "Encourage the child to feed themselves to build independence.",
                "Keep a balance of grains, pulses, vegetables, and milk products.",
                "Limit sugary biscuits or snacks; stick to natural home-cooked food.",
                "Make sure the child drinks enough water throughout the day.",
                "Continue offering a variety of textures to avoid picky eating later.",
                "Avoid forced feeding; let the child decide when they are full.",
                "Keep mealtimes happy and free from screens or distractions."
            )
        }
    }

    private fun getHindiBabyTips(months: Int): List<String> {
        return when {
            months < 6 -> listOf(
                "6 महीने तक केवल माँ का दूध ही पूर्ण आहार है। पानी या शहद की जरूरत नहीं है।",
                "माँ के दूध में एंटीबॉडी होते हैं जो आपके बच्चे को बीमारियों से बचाते हैं।",
                "बच्चे को उसकी मांग के अनुसार दूध पिलाएं, आमतौर पर 24 घंटे में 8 से 12 बार।",
                "दूध पिलाते समय बच्चे की पकड़ (latch) सही रखें ताकि उसे पर्याप्त दूध मिले।",
                "बच्चा जितना अधिक स्तनपान करेगा, आपका शरीर उतना ही अधिक दूध बनाएगा।",
                "यदि बच्चा चिड़चिड़ा है, तो उसे शांत करने के लिए अपनी छाती से सटाकर रखें।",
                "माँ के दूध में 80% पानी होता है; गर्मी में भी अलग से पानी की जरूरत नहीं है।",
                "केवल स्तनपान कराने से आपका अपने बच्चे के साथ गहरा लगाव बनता है।",
                "जब बच्चा सोए तब आप भी आराम करें; आराम करने से दूध अधिक बनता है।",
                "शुरुआत में बोतल का उपयोग न करें, इससे बच्चा स्तनपान छोड़ सकता है।"
            )
            months == 6 -> listOf(
                "अब 'अन्नप्राशन' का समय है! अच्छी तरह मसली हुई दलिया या खिचड़ी से शुरू करें।",
                "शुरुआत में दिन में दो बार सिर्फ 2-3 चम्मच मसला हुआ खाना दें।",
                "मसला हुआ केला ऊर्जा के लिए एक बेहतरीन पहला ठोस आहार है।",
                "किसी भी एलर्जी की जांच के लिए एक बार में एक ही नया भोजन खिलाएं।",
                "खाना इतना गाढ़ा होना चाहिए कि चम्मच पर टिका रहे, पानी जैसा पतला नहीं।",
                "ठोस आहार शुरू करने के साथ-साथ स्तनपान जारी रखें।",
                "गाजर या आलू जैसी सब्जियों को नरम होने तक उबालें और फिर अच्छी तरह मसल लें।",
                "1 साल का होने तक बच्चे के खाने में नमक या चीनी बिल्कुल न डालें।",
                "खिलाने से पहले हमेशा अपने हाथ और बच्चे के बर्तन अच्छी तरह धोएं।",
                "धैर्य रखें; बच्चे को नया स्वाद अपनाने में कई बार समय लगता है।"
            )
            months in 7..8 -> listOf(
                "नए स्वाद पेश करें। दिन में 3 बार मसली हुई सब्जियां और फल दें।",
                "स्वस्थ वसा के लिए बच्चे की खिचड़ी में एक चम्मच घी या तेल मिलाएं।",
                "अच्छी तरह मसली हुई दाल और चावल विकास के लिए प्रोटीन प्रदान करते हैं।",
                "बच्चे को खाने की बनावट (texture) को छूने और महसूस करने दें।",
                "भोजन के बाद कप से साफ, उबले हुए पानी की छोटी घूंट पिलाएं।",
                "आयरन और कैल्शियम के लिए गाढ़ा रागी का दलिया (नाचनी) खिलाएं।",
                "फलों के रस से बचें; इसके बजाय पपीता या चीकू जैसे मसले हुए फल दें।",
                "अच्छी आदतें डालने के लिए खिलाने का एक निश्चित समय तय करें।",
                "खाना खिलाते समय सुनिश्चित करें कि बच्चा सीधा बैठा हो।",
                "माँ का दूध अभी भी पोषण का एक बहुत ही महत्वपूर्ण स्रोत है।"
            )
            months in 9..11 -> listOf(
                "बच्चा अब बारीक कटा हुआ पारिवारिक भोजन खा सकता है। उसे छोटे टुकड़े उठाने दें।",
                "दाल या दूध में भिगोई हुई नरम रोटी के छोटे टुकड़े दें।",
                "उबले हुए आलू या नरम फल के टुकड़ों जैसे 'फिंगर फूड्स' खिलाएं।",
                "दिन में 3 से 4 बार भोजन और साथ में स्वस्थ नाश्ता दें।",
                "बच्चा अब चम्मच पकड़ने की कोशिश कर सकता है—उसे कोशिश करने दें।",
                "बारीक कटे हुए उबले अंडे या नरम पनीर को खाने में शामिल करें।",
                "सुनिश्चित करें कि भोजन इतना नरम हो जिसे बच्चा मसूड़ों से चबा सके।",
                "साबुत मेवे या अंगूर जैसे सख्त खाने से बचें, ये गले में फंस सकते हैं।",
                "बच्चे की थाली में अलग-अलग रंगों की सब्जियां (पालक, कद्दू, दाल) शामिल करें।",
                "भोजन का समय सामाजिक होता है; बच्चे को परिवार के साथ बैठने दें।"
            )
            else -> listOf(
                "बच्चा अब परिवार का लगभग सब कुछ खा सकता है, लेकिन मिर्च-मसाले कम रखें।",
                "दिन में 3 मुख्य भोजन और 2 बार स्वस्थ नाश्ता (फल या दही) सुनिश्चित करें।",
                "अब कप में गाय का दूध देना शुरू किया जा सकता है।",
                "बच्चे को खुद से खाने के लिए प्रोत्साहित करें ताकि वह आत्मनिर्भर बने।",
                "अनाज, दालों, सब्जियों और दूध उत्पादों का संतुलन बनाए रखें।",
                "मीठे बिस्कुट या बाहरी स्नैक्स कम दें; घर का बना ताजा खाना ही दें।",
                "सुनिश्चित करें कि बच्चा दिन भर में पर्याप्त पानी पीता रहे।",
                "खाने में विविधता रखें ताकि आगे चलकर बच्चा खाने में नखरे न करे।",
                "जबरदस्ती न खिलाएं; बच्चे को तय करने दें कि उसका पेट भर गया है।",
                "भोजन के समय को खुशनुमा रखें और मोबाइल या टीवी से दूर रहें।"
            )
        }
    }

    private fun getEnglishMotherTips(months: Int): List<String> {
        return listOf(
            "Drink plenty of water. Staying hydrated is crucial for maintaining your milk supply.",
            "Eat iron-rich foods like spinach, lentils, and beans to regain your strength.",
            "Include calcium-rich foods like milk, curd, or ragi in your diet daily.",
            "Try to have small, frequent meals to keep your energy levels steady throughout the day.",
            "Add healthy fats like ghee or nuts to your meals for better nutrition and recovery.",
            "Oats and fenugreek (methi) are traditionally known to help increase milk production.",
            "Avoid caffeine and spicy foods if you notice they make your baby fussy.",
            "Eat seasonal fruits and vegetables to get a wide range of vitamins and minerals.",
            "Protein is essential for your recovery; include dal, eggs, or paneer in your meals.",
            "Rest is as important as nutrition. Try to nap whenever your baby sleeps."
        )
    }

    private fun getHindiMotherTips(months: Int): List<String> {
        return listOf(
            "खूब पानी पिएं। दूध की आपूर्ति बनाए रखने के लिए हाइड्रेटेड रहना बहुत महत्वपूर्ण है।",
            "अपनी शक्ति वापस पाने के लिए पालक, दाल और बीन्स जैसे आयरन से भरपूर खाद्य पदार्थ खाएं।",
            "अपने आहार में रोजाना दूध, दही या रागी जैसे कैल्शियम से भरपूर खाद्य पदार्थ शामिल करें।",
            "दिन भर अपनी ऊर्जा के स्तर को बनाए रखने के लिए थोड़ा-थोड़ा और बार-बार भोजन करें।",
            "बेहतर पोषण और रिकवरी के लिए अपने भोजन में घी या मेवा जैसे स्वस्थ वसा शामिल करें।",
            "ओट्स और मेथी पारंपरिक रूप से दूध उत्पादन बढ़ाने में मदद करने के लिए जाने जाते हैं।",
            "यदि आप देखती हैं कि कैफीन और मसालेदार भोजन से आपका बच्चा चिड़चिड़ा हो जाता है, तो उनसे बचें।",
            "विभिन्न प्रकार के विटामिन और खनिज प्राप्त करने के लिए मौसमी फल और सब्जियां खाएं।",
            "रिकवरी के लिए प्रोटीन आवश्यक है; अपने भोजन में दाल, अंडे या पनीर शामिल करें।",
            "आराम उतना ही महत्वपूर्ण है जितना पोषण। जब भी आपका बच्चा सोए, झपकी लेने की कोशिश करें।"
        )
    }
}
