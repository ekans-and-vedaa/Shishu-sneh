package com.shishusneh.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShishuDao {
    // --- Baby Profile ---
    @Query("SELECT EXISTS(SELECT 1 FROM baby_profile LIMIT 1)")
    fun hasProfile(): Flow<Boolean>

    @Query("SELECT * FROM baby_profile LIMIT 1")
    fun getBabyProfile(): Flow<BabyProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBabyProfile(profile: BabyProfile)

    @Query("UPDATE baby_profile SET imageUri = :uri WHERE id = :id")
    suspend fun updateBabyImage(uri: String, id: Int = 1)

    // --- Growth Records ---
    @Query("SELECT * FROM growth_records ORDER BY date ASC")
    fun getAllGrowthRecords(): Flow<List<GrowthRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrowth(record: GrowthRecord)

    // --- Vaccines ---
    @Query("SELECT * FROM vaccination_records")
    fun getAllVaccines(): Flow<List<VaccinationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVaccine(vaccine: VaccinationRecord)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialVaccines(vaccines: List<VaccinationRecord>)

    @Query("SELECT COUNT(*) FROM vaccination_records")
    suspend fun getVaccineCount(): Int

    // --- Milestones ---
    @Query("SELECT * FROM milestones")
    fun getAllMilestones(): Flow<List<Milestone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMilestone(milestone: Milestone)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialMilestones(milestones: List<Milestone>)
}