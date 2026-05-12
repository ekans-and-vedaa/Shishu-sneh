package com.shishusneh.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VaccinationRecord::class,
        Milestone::class,
        GrowthRecord::class,
        BabyProfile::class
    ],
    version = 6
)
abstract class ShishuDatabase : RoomDatabase() {
    abstract fun dao(): ShishuDao

    companion object {
        @Volatile
        private var INSTANCE: ShishuDatabase? = null

        fun getDatabase(context: Context): ShishuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShishuDatabase::class.java,
                    "shishu_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}