package com.afmaelis.reminder.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afmaelis.reminder.data.Birthday
import com.afmaelis.reminder.data.BirthdayRepository
import com.afmaelis.reminder.domain.BirthdayValidator
import com.afmaelis.reminder.domain.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class EditBirthdayUiState(
    val isNew: Boolean,
    val loading: Boolean,
    val name: String = "",
    val month: Int,
    val day: Int,
    val yearText: String = "",
    val note: String = "",
    val nameTouched: Boolean = false,
    val validation: ValidationResult = ValidationResult(),
    val showDeleteConfirm: Boolean = false,
    val finished: Boolean = false,
) {
    val canSave: Boolean get() = !loading && validation.isValid
}

class EditBirthdayViewModel(
    private val repository: BirthdayRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val id: Long = savedStateHandle.get<Long>(ARG_ID) ?: NEW_ID
    private var original: Birthday? = null

    private val _uiState = MutableStateFlow(
        LocalDate.now().let { today ->
            validated(
                EditBirthdayUiState(
                    isNew = id == NEW_ID,
                    loading = id != NEW_ID,
                    month = today.monthValue,
                    day = today.dayOfMonth,
                ),
            )
        },
    )
    val uiState: StateFlow<EditBirthdayUiState> = _uiState.asStateFlow()

    init {
        if (id != NEW_ID) load()
    }

    private fun load() {
        viewModelScope.launch {
            val birthday = repository.get(id)
            if (birthday == null) {
                _uiState.update { it.copy(finished = true) }
                return@launch
            }
            original = birthday
            _uiState.update {
                validated(
                    it.copy(
                        loading = false,
                        name = birthday.name,
                        month = birthday.month,
                        day = birthday.day,
                        yearText = birthday.year?.toString().orEmpty(),
                        note = birthday.note.orEmpty(),
                    ),
                )
            }
        }
    }

    fun onNameChange(name: String) = edit { it.copy(name = name, nameTouched = true) }

    fun onMonthChange(month: Int) = edit {
        it.copy(month = month, day = it.day.coerceAtMost(BirthdayValidator.maxDayOfMonth(month)))
    }

    fun onDayChange(day: Int) = edit { it.copy(day = day) }

    fun onYearChange(yearText: String) = edit { it.copy(yearText = yearText.filter(Char::isDigit).take(4)) }

    fun onNoteChange(note: String) = edit { it.copy(note = note) }

    fun onDeleteRequest() = _uiState.update { it.copy(showDeleteConfirm = true) }

    fun onDeleteDismiss() = _uiState.update { it.copy(showDeleteConfirm = false) }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        val birthday = Birthday(
            id = original?.id ?: 0,
            name = state.name.trim(),
            month = state.month,
            day = state.day,
            year = BirthdayValidator.parseYear(state.yearText).getOrNull(),
            note = state.note.trim().ifEmpty { null },
        )
        viewModelScope.launch {
            repository.save(birthday)
            _uiState.update { it.copy(finished = true) }
        }
    }

    fun delete() {
        val birthday = original ?: return
        viewModelScope.launch {
            repository.delete(birthday)
            _uiState.update { it.copy(showDeleteConfirm = false, finished = true) }
        }
    }

    private fun edit(change: (EditBirthdayUiState) -> EditBirthdayUiState) =
        _uiState.update { validated(change(it)) }

    private fun validated(state: EditBirthdayUiState): EditBirthdayUiState = state.copy(
        validation = BirthdayValidator.validate(state.name, state.month, state.day, state.yearText, LocalDate.now()),
    )

    companion object {
        const val ARG_ID = "id"
        const val NEW_ID = -1L
    }
}
