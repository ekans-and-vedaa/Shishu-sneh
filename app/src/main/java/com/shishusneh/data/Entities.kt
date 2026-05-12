package com.shishusneh.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "baby_profile")
data class BabyProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dateOfBirth: Long,
    val gender: String,
    val imageUri: String? = null
)

@Entity(tableName = "growth_records")
data class GrowthRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val weightKg: Float,
    val heightCm: Float
)

@Entity(tableName = "vaccination_records")
data class VaccinationRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val diseaseName: String,
    val description: String,
    val scheduledTimeframe: String,
    val dueDateMillis: Long,
    val isCompleted: Boolean = false
)

@Entity(tableName = "milestones")
data class Milestone(
    @PrimaryKey val id: String,
    val name: String,
    val age: String,
    val description: String,
    val isCompleted: Boolean = false,
    val imageUri: String? = null
)