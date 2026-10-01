package com.afmaelis.reminder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Birthday::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun birthdayDao(): BirthdayDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "afmaelis.db").build()
    }
}
