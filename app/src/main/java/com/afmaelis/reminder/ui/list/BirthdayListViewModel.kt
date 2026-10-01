package com.afmaelis.reminder.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afmaelis.reminder.data.Birthday
import com.afmaelis.reminder.data.BirthdayRepository
import com.afmaelis.reminder.domain.BirthdayCalculator
import com.afmaelis.reminder.domain.UpcomingBirthday
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class BirthdayListUiState(
    val loading: Boolean = true,
    val today: List<UpcomingBirthday<Birthday>> = emptyList(),
    val upcoming: List<UpcomingBirthday<Birthday>> = emptyList(),
) {
    val isEmpty: Boolean get() = !loading && today.isEmpty() && upcoming.isEmpty()
}

class BirthdayListViewModel(repository: BirthdayRepository) : ViewModel() {

    private val today = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<BirthdayListUiState> =
        combine(repository.birthdays, today) { birthdays, date ->
            val (todays, others) = BirthdayCalculator.upcoming(birthdays, date).partition { it.isToday }
            BirthdayListUiState(loading = false, today = todays, upcoming = others)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BirthdayListUiState())

    /** Called when the screen resumes so the list is correct after midnight. */
    fun refreshDate() {
        today.value = LocalDate.now()
    }
}
