package com.example.gainsbookxml.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gainsbookxml.database.entities.Workout
import com.example.gainsbookxml.database.entities.Exercise
import com.example.gainsbookxml.database.entities.Year
import com.example.gainsbookxml.database.entities.Statistic
import com.example.gainsbookxml.database.entities.Profile
import com.example.gainsbookxml.database.entities.Variable

@Database(
    entities = [
        Workout::class,
        Exercise::class,
        Year::class,
        Statistic::class,
        Profile::class,
        Variable::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract val appDao: AppDao

    companion object {
        // @Volatile means that whenever the value of this variable is changed,
        // the change is immediately visible to other threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Will construct our database
        fun getInstance(context: Context): AppDatabase {
            // synchronize will make sure that whenever this block of code is executed,
            // it is executed by only a single thread
            // this refers to the companion object of SchoolDatabase
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gainsbook_db"
                ).build().also {
                    // updates INSTANCE variable to be our freshly built database
                    INSTANCE = it
                }
            }
        }
    }
}