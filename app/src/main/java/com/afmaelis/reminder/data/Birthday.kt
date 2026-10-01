package com.afmaelis.reminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.afmaelis.reminder.domain.BirthdayInfo

@Entity(tableName = "birthdays")
data class Birthday(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    override val name: String,
    override val month: Int,
    override val day: Int,
    override val year: Int? = null,
    val note: String? = null,
) : BirthdayInfo
