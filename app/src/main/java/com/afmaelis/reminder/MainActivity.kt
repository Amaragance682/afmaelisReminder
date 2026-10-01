package com.afmaelis.reminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.afmaelis.reminder.ui.edit.EditBirthdayRoute
import com.afmaelis.reminder.ui.edit.EditBirthdayViewModel.Companion.ARG_ID
import com.afmaelis.reminder.ui.edit.EditBirthdayViewModel.Companion.NEW_ID
import com.afmaelis.reminder.ui.list.BirthdayListRoute
import com.afmaelis.reminder.ui.settings.SettingsRoute
import com.afmaelis.reminder.ui.theme.AfmaelisTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                val app = application as AfmaelisApp
                app.appScope.launch { app.catchUp() }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            AfmaelisTheme {
                AppNavHost()
            }
        }
    }
}

private object Routes {
    const val LIST = "list"
    const val SETTINGS = "settings"
    const val EDIT = "edit?$ARG_ID={$ARG_ID}"

    fun edit(id: Long = NEW_ID) = "edit?$ARG_ID=$id"
}

@Composable
private fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            BirthdayListRoute(
                onAdd = { navController.navigate(Routes.edit()) },
                onEdit = { id -> navController.navigate(Routes.edit(id)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(
            route = Routes.EDIT,
            arguments = listOf(
                navArgument(ARG_ID) {
                    type = NavType.LongType
                    defaultValue = NEW_ID
                },
            ),
        ) {
            EditBirthdayRoute(onDone = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
    }
}
