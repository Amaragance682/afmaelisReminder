package com.afmaelis.reminder.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afmaelis.reminder.R
import com.afmaelis.reminder.domain.BirthdayValidator
import com.afmaelis.reminder.domain.DateError
import com.afmaelis.reminder.domain.NameError
import com.afmaelis.reminder.domain.YearError
import com.afmaelis.reminder.ui.AppViewModelFactory
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun EditBirthdayRoute(
    onDone: () -> Unit,
    viewModel: EditBirthdayViewModel = viewModel(factory = AppViewModelFactory.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.finished) {
        if (state.finished) onDone()
    }
    EditBirthdayScreen(
        state = state,
        onBack = onDone,
        onNameChange = viewModel::onNameChange,
        onMonthChange = viewModel::onMonthChange,
        onDayChange = viewModel::onDayChange,
        onYearChange = viewModel::onYearChange,
        onNoteChange = viewModel::onNoteChange,
        onSave = viewModel::save,
        onDeleteRequest = viewModel::onDeleteRequest,
        onDeleteDismiss = viewModel::onDeleteDismiss,
        onDeleteConfirm = viewModel::delete,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBirthdayScreen(
    state: EditBirthdayUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    onYearChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDeleteRequest: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onDeleteConfirm: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (state.isNew) R.string.add_birthday else R.string.edit_birthday))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = onDeleteRequest) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            val nameError = state.validation.nameError?.takeIf { state.nameTouched }
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.field_name)) },
                isError = nameError != null,
                supportingText = if (nameError != null) {
                    { Text(nameErrorText(nameError)) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Dropdown(
                    label = stringResource(R.string.field_day),
                    selected = state.day.toString(),
                    options = (1..BirthdayValidator.maxDayOfMonth(state.month)).toList(),
                    optionLabel = { it.toString() },
                    onSelect = onDayChange,
                    enabled = !state.loading,
                    modifier = Modifier.weight(1f),
                )
                Dropdown(
                    label = stringResource(R.string.field_month),
                    selected = monthName(state.month),
                    options = (1..12).toList(),
                    optionLabel = ::monthName,
                    onSelect = onMonthChange,
                    enabled = !state.loading,
                    modifier = Modifier.weight(2f),
                )
            }
            state.validation.dateError?.let {
                Text(dateErrorText(it), color = MaterialTheme.colorScheme.error)
            }

            val yearError = state.validation.yearError
            OutlinedTextField(
                value = state.yearText,
                onValueChange = onYearChange,
                label = { Text(stringResource(R.string.field_year)) },
                isError = yearError != null,
                supportingText = {
                    Text(yearError?.let { yearErrorText(it) } ?: stringResource(R.string.field_year_hint))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                label = { Text(stringResource(R.string.field_note)) },
                minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = onSave,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = onDeleteDismiss,
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_text, state.name)) },
            confirmButton = {
                TextButton(onClick = onDeleteConfirm) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = onDeleteDismiss) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Dropdown(
    label: String,
    selected: String,
    options: List<Int>,
    optionLabel: (Int) -> String,
    onSelect: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun monthName(month: Int): String =
    Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())

@Composable
private fun nameErrorText(error: NameError): String = when (error) {
    NameError.BLANK -> stringResource(R.string.error_name_blank)
}

@Composable
private fun dateErrorText(error: DateError): String = when (error) {
    DateError.INVALID_MONTH -> stringResource(R.string.error_month_invalid)
    DateError.INVALID_DAY -> stringResource(R.string.error_day_invalid)
}

@Composable
private fun yearErrorText(error: YearError): String = when (error) {
    YearError.NOT_A_NUMBER -> stringResource(R.string.error_year_not_number)
    YearError.OUT_OF_RANGE -> stringResource(R.string.error_year_range, BirthdayValidator.MIN_YEAR)
    YearError.IN_FUTURE -> stringResource(R.string.error_year_future)
    YearError.NOT_A_LEAP_YEAR -> stringResource(R.string.error_year_not_leap)
}
