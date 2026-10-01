package com.afmaelis.reminder.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.afmaelis.reminder.AfmaelisApp
import com.afmaelis.reminder.ui.edit.EditBirthdayViewModel
import com.afmaelis.reminder.ui.list.BirthdayListViewModel
import com.afmaelis.reminder.ui.settings.SettingsViewModel

object AppViewModelFactory {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            BirthdayListViewModel(app().repository)
        }
        initializer {
            EditBirthdayViewModel(app().repository, createSavedStateHandle())
        }
        initializer {
            val app = app()
            SettingsViewModel(app.settings, app.scheduler, app.notifications)
        }
    }

    private fun CreationExtras.app(): AfmaelisApp =
        this[APPLICATION_KEY] as AfmaelisApp
}
