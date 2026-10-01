package com.afmaelis.reminder.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afmaelis.reminder.R
import com.afmaelis.reminder.data.Birthday
import com.afmaelis.reminder.domain.UpcomingBirthday
import com.afmaelis.reminder.ui.AppViewModelFactory
import java.time.MonthDay
import java.time.format.DateTimeFormatter

@Composable
fun BirthdayListRoute(
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: BirthdayListViewModel = viewModel(factory = AppViewModelFactory.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.refreshDate()
        onPauseOrDispose { }
    }
    BirthdayListScreen(state, onAdd, onEdit, onOpenSettings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayListScreen(
    state: BirthdayListUiState,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onOpenSettings: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_birthday))
            }
        },
    ) { padding ->
        if (state.isEmpty) {
            EmptyState(Modifier.padding(padding))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 88.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (state.today.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.section_today)) }
                    items(state.today, key = { it.birthday.id }) { item ->
                        TodayCard(item, onClick = { onEdit(item.birthday.id) })
                    }
                }
                if (state.upcoming.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.section_upcoming)) }
                    items(state.upcoming, key = { it.birthday.id }) { item ->
                        UpcomingRow(item, onClick = { onEdit(item.birthday.id) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun TodayCard(item: UpcomingBirthday<Birthday>, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Cake,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(36.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = item.birthday.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                item.ageTurning?.let { age ->
                    Text(
                        text = stringResource(R.string.turns_today, age),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingRow(item: UpcomingBirthday<Birthday>, onClick: () -> Unit) {
    val whenText = if (item.daysUntil == 1) {
        stringResource(R.string.tomorrow)
    } else {
        pluralStringResource(R.plurals.in_days, item.daysUntil, item.daysUntil)
    }
    ListItem(
        headlineContent = { Text(item.birthday.name) },
        supportingContent = { Text(formatDayMonth(item.birthday)) },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(whenText, style = MaterialTheme.typography.bodyMedium)
                item.ageTurning?.let { age ->
                    Text(stringResource(R.string.turns_age, age), style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Cake,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.empty_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.empty_hint),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private val dayMonthFormatter = DateTimeFormatter.ofPattern("d MMMM")

private fun formatDayMonth(birthday: Birthday): String =
    MonthDay.of(birthday.month, birthday.day).format(dayMonthFormatter)
