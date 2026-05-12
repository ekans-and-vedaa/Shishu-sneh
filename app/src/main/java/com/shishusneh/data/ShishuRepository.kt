package com.shishusneh.data

import kotlinx.coroutines.flow.Flow

class ShishuRepository(private val dao: ShishuDao) {

    // ---------- PROFILE ----------
    val hasProfile: Flow<Boolean> = dao.hasProfile()

    fun getBabyProfile(): Flow<BabyProfile?> = dao.getBabyProfile()

    suspend fun saveBabyProfile(profile: BabyProfile) {
        dao.insertBabyProfile(profile)
    }

    suspend fun updateBabyProfileImage(uri: String) {
        dao.updateBabyImage(uri, id = 1)
    }

    // ---------- GROWTH ----------
    val growthRecords: Flow<List<GrowthRecord>> = dao.getAllGrowthRecords()

    suspend fun addGrowth(weight: Float, height: Float) {
        dao.insertGrowth(
            GrowthRecord(
                date = System.currentTimeMillis(),
                weightKg = weight,
                heightCm = height
            )
        )
    }

    // ---------- VACCINES ----------
    val vaccines: Flow<List<VaccinationRecord>> = dao.getAllVaccines()

    suspend fun saveVaccine(vaccinationRecord: VaccinationRecord) {
        dao.upsertVaccine(vaccinationRecord)
    }

    // ---------- MILESTONES ----------
    val milestones: Flow<List<Milestone>> = dao.getAllMilestones()

    suspend fun saveMilestone(milestone: Milestone) {
        dao.upsertMilestone(milestone)
    }

    suspend fun seedData() {
        val milestoneList = listOf(
            Milestone("w1_faces", "Focuses on Faces", "1 Week", "Baby starts to look at your face."),
            Milestone("w2_sounds", "Reacts to Sounds", "2 Weeks", "Baby startles or reacts to loud noises."),
            Milestone("w4_head", "Lifts Head", "4 Weeks", "Briefly lifts head when on tummy."),
            Milestone("w6_smile", "First Smile", "6 Weeks", "Baby gives a real smile to people."),
            Milestone("w8_coo", "Cooing Sounds", "8 Weeks", "Makes 'oo' and 'ah' sounds."),
            Milestone("w10_track", "Tracks Objects", "10 Weeks", "Follows moving things with eyes."),
            Milestone("w12_steady", "Head Steady", "12 Weeks", "Holds head steady when being held."),
            Milestone("w16_reach", "Reaching", "16 Weeks", "Tries to reach for and grab toys."),
            Milestone("roll", "Rolling Over", "24 Weeks", "Rolls from tummy to back."),
            Milestone("sit_support", "Sitting with Support", "26 Weeks", "Sits up with help from hands or pillows."),
            Milestone("sit_no_support", "Sitting Alone", "36 Weeks", "Sits without any support."),
            Milestone("crawl", "Crawling", "40 Weeks", "Moves on hands and knees."),
            Milestone("stand", "Standing Alone", "48 Weeks", "Stands for a few seconds without support."),
            Milestone("walk", "First Steps", "52 Weeks", "Takes a few steps without holding on."),
            Milestone("bye", "Waving Bye-Bye", "1 Year", "Waves goodbye to people."),
            Milestone("points", "Pointing", "1.5 Years", "Points to show someone what they want."),
            Milestone("words", "Several Words", "1.5 Years", "Says several single words (e.g., Mama, Dada)."),
            Milestone("walk_well", "Walking Well", "1.5 Years", "Walks and may even start to run."),
            Milestone("two_words", "Word Combinations", "2 Years", "Says at least two words together (e.g., More milk)."),
            Milestone("run", "Running", "2 Years", "Starts to run well."),
            Milestone("kick", "Kicking a Ball", "2 Years", "Can kick a large ball forward."),
            Milestone("climb", "Climbing", "3 Years", "Climbs well on furniture and playground equipment."),
            Milestone("sentences", "Simple Sentences", "3 Years", "Speaks in sentences of 3 to 4 words."),
            Milestone("hop", "Hopping", "4 Years", "Hops and stands on one foot for up to 2 seconds."),
            Milestone("stories", "Telling Stories", "4 Years", "Can tell a simple story or recount their day."),
            Milestone("swing", "Swinging & Climbing", "5 Years", "Can swing and climb easily."),
            Milestone("speak_clear", "Clear Speech", "5 Years", "Speaks clearly and uses complex sentences.")
        )
        // insertInitialMilestones uses OnConflictStrategy.IGNORE so existing data is preserved
        dao.insertInitialMilestones(milestoneList)
    }
}
