package com.midani.wacs.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DetectedNumber::class],
    version = 1,
    exportSchema = false
)
abstract class WacsDatabase : RoomDatabase() {
    
    abstract fun detectedNumberDao(): DetectedNumberDao
    
    companion object {
        @Volatile
        private var INSTANCE: WacsDatabase? = null
        
        fun getInstance(context: Context): WacsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WacsDatabase::class.java,
                    "wacs_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
