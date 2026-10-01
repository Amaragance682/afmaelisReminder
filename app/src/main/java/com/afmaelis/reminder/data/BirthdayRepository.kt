package com.afmaelis.reminder.data

import kotlinx.coroutines.flow.Flow

class BirthdayRepository(private val dao: BirthdayDao) {
    val birthdays: Flow<List<Birthday>> = dao.observeAll()

    suspend fun getAll(): List<Birthday> = dao.getAll()

    suspend fun get(id: Long): Birthday? = dao.getById(id)

    suspend fun save(birthday: Birthday) {
        if (birthday.id == 0L) dao.insert(birthday) else dao.update(birthday)
    }

    suspend fun delete(birthday: Birthday) = dao.delete(birthday)
}
