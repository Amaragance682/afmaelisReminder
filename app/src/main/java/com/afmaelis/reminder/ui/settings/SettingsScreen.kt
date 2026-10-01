package com.afmaelis.reminder.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afmaelis.reminder.R
import com.afmaelis.reminder.ui.AppViewModelFactory
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelFactory.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleResumeEffect(Unit) {
        viewModel.refreshPermission()
        onPauseOrDispose { }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        // The system stops showing the dialog after repeated denials; fall back to app settings.
        if (granted) viewModel.refreshPermission() else openNotificationSettings(context)
    }

    SettingsScreen(
        state = state,
        is24Hour = DateFormat.is24HourFormat(context),
        onBack = onBack,
        onTimeChange = viewModel::setNotifyTime,
        onRemindDayBeforeChange = viewModel::setRemindDayBefore,
        onSendTest = viewModel::sendTestNotification,
        onGrantPermission = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                openNotificationSettings(context)
            }
        },
    )
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    context.startActivity(intent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    is24Hour: Boolean,
    onBack: () -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onRemindDayBeforeChange: (Boolean) -> Unit,
    onSendTest: () -> Unit,
    onGrantPermission: () -> Unit,
) {
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.setting_time)) },
                supportingContent = {
                    Text(state.notifyTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)))
                },
                modifier = Modifier.clickable { showTimePicker = true },
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text(stringResource(R.string.setting_day_before)) },
                trailingContent = {
                    Switch(checked = state.remindDayBefore, onCheckedChange = onRemindDayBeforeChange)
                },
                modifier = Modifier.clickable { onRemindDayBeforeChange(!state.remindDayBefore) },
            )
            HorizontalDivider()
            ListItem(
                leadingContent = {
                    Icon(
                        if (state.notificationsEnabled) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsOff,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(
                        stringResource(
                            if (state.notificationsEnabled) R.string.permission_granted else R.string.permission_denied,
                        ),
                    )
                },
                supportingContent = if (state.notificationsEnabled) {
                    null
                } else {
                    { Text(stringResource(R.string.permission_denied_hint)) }
                },
                trailingContent = if (state.notificationsEnabled) {
                    null
                } else {
                    { TextButton(onClick = onGrantPermission) { Text(stringResource(R.string.permission_grant)) } }
                },
            )
            HorizontalDivider()
            OutlinedButton(
                onClick = onSendTest,
                enabled = state.notificationsEnabled,
                modifier = Modifier.padding(16.dp),
            ) {
                Text(stringResource(R.string.send_test_notification))
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            initial = state.notifyTime,
            is24Hour = is24Hour,
            onDismiss = { showTimePicker = false },
            onConfirm = {
                onTimeChange(it)
                showTimePicker = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initial: LocalTime,
    is24Hour: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val pickerState = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = is24Hour,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.setting_time)) },
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(pickerState.hour, pickerState.minute)) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
