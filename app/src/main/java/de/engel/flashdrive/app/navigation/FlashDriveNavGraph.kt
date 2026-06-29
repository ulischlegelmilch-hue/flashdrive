package de.engel.flashdrive.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import de.engel.flashdrive.app.ui.screen.cardeditor.CardEditorScreen
import de.engel.flashdrive.app.ui.screen.deckdetail.DeckDetailScreen
import de.engel.flashdrive.app.ui.screen.decklist.DeckListScreen
import de.engel.flashdrive.feature.import.ui.ImportScreen
import de.engel.flashdrive.feature.study.ui.StudyScreen
import de.engel.flashdrive.feature.statistics.ui.StatisticsScreen
import de.engel.flashdrive.settings.ui.SettingsScreen

/**
 * Main navigation graph for the FlashDrive application.
 * Defines all composable destinations and their routes.
 */
@Composable
fun FlashDriveNavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        // ── Deck List ──────────────────────────────────────────────────────
        composable(route = Screen.DeckList.route) {
            DeckListScreen(
                onDeckClick = { deckId, deckName ->
                    navController.navigate(
                        Screen.DeckDetail.createRoute(deckId, deckName)
                    )
                },
                onStudyClick = { deckId ->
                    navController.navigate(Screen.Study.createRoute(deckId))
                },
                onImportClick = { deckId ->
                    navController.navigate(Screen.ImportDeck.createRoute(deckId))
                },
                onNavigate = { route ->
                    if (route != Screen.DeckList.route) {
                        navController.navigate(route) {
                            popUpTo(Screen.DeckList.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
            )
        }

        // ── Deck Detail ───────────────────────────────────────────────────
        composable(
            route = Screen.DeckDetail.ROUTE_PATTERN,
            arguments = listOf(
                navArgument("deckId") { type = NavType.LongType },
                navArgument("deckName") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: return@composable
            val deckName = backStackEntry.arguments?.getString("deckName").orEmpty()

            DeckDetailScreen(
                deckId = deckId,
                deckName = deckName,
                onNavigateBack = { navController.popBackStack() },
                onCardClick = { cardId ->
                    navController.navigate(
                        Screen.CardEditor.createRoute(deckId, cardId)
                    )
                },
                onAddCardClick = {
                    navController.navigate(
                        Screen.CardEditor.createRoute(deckId)
                    )
                },
                onStudyClick = {
                    navController.navigate(Screen.Study.createRoute(deckId))
                },
                onImportClick = {
                    navController.navigate(Screen.ImportDeck.createRoute(deckId))
                },
            )
        }

        // ── Card Editor ──────────────────────────────────────────────────
        composable(
            route = Screen.CardEditor.ROUTE_PATTERN,
            arguments = listOf(
                navArgument("deckId") { type = NavType.LongType },
                navArgument("cardId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: return@composable
            val rawCardId = backStackEntry.arguments?.getLong("cardId") ?: -1L
            val cardId = if (rawCardId > 0) rawCardId else null

            CardEditorScreen(
                deckId = deckId,
                cardId = cardId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Study ─────────────────────────────────────────────────────────
        composable(
            route = Screen.Study.ROUTE_PATTERN,
            arguments = listOf(
                navArgument("deckId") { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: return@composable

            StudyScreen(
                deckId = deckId,
                navigator = { screen -> navController.navigate(screen.route) },
            )
        }

        // ── Statistics ────────────────────────────────────────────────────
        composable(route = Screen.Statistics.route) {
            StatisticsScreen()
        }

        // ── Settings ──────────────────────────────────────────────────────
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Import Deck ───────────────────────────────────────────────────
        composable(
            route = Screen.ImportDeck.ROUTE_PATTERN,
            arguments = listOf(
                navArgument("deckId") { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: return@composable

            ImportScreen(deckId = deckId)
        }
    }
}
