package de.engel.flashdrive.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import de.engel.flashdrive.app.navigation.BottomNavBar
import de.engel.flashdrive.app.navigation.FlashDriveNavGraph
import de.engel.flashdrive.app.navigation.Screen
import de.engel.flashdrive.app.navigation.bottomNavScreens
import de.engel.flashdrive.app.ui.theme.FlashDriveTheme

/**
 * Main entry point activity for the FlashDrive phone application.
 * Uses EdgeToEdge display and Hilt dependency injection.
 * Hosts the persistent BottomNavBar for all top-level destinations.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FlashDriveTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = bottomNavScreens.any { it.route == currentRoute }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(Screen.DeckList.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            )
                        }
                    },
                ) { innerPadding ->
                    FlashDriveNavGraph(
                        navController = navController,
                        startDestination = Screen.DeckList.route,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
