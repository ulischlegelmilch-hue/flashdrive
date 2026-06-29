package de.engel.flashdrive.app.navigation

/**
 * Sealed class representing all navigable screens in the FlashDrive app.
 * Each screen defines its route string for use with Jetpack Navigation Compose.
 */
sealed class Screen(val route: String) {

    /** List of all decks. */
    data object DeckList : Screen("deck_list")

    /** Detail view for a specific deck. */
    data class DeckDetail(
        val deckId: Long,
        val deckName: String,
    ) : Screen("deck_list/{deckId}?deckName={deckName}") {
        companion object {
            const val ROUTE_PATTERN = "deck_list/{deckId}?deckName={deckName}"

            fun createRoute(deckId: Long, deckName: String): String {
                return "deck_list/$deckId?deckName=${android.net.Uri.encode(deckName)}"
            }
        }
    }

    /** Editor for creating or editing a flashcard. */
    data class CardEditor(
        val deckId: Long,
        val cardId: Long? = null,
    ) : Screen("card_editor/{deckId}?cardId={cardId}") {
        companion object {
            const val ROUTE_PATTERN = "card_editor/{deckId}?cardId={cardId}"

            fun createRoute(deckId: Long, cardId: Long? = null): String {
                return if (cardId != null) {
                    "card_editor/$deckId?cardId=$cardId"
                } else {
                    "card_editor/$deckId"
                }
            }
        }
    }

    /** Study session for a specific deck. */
    data class Study(
        val deckId: Long,
    ) : Screen("study/{deckId}") {
        companion object {
            const val ROUTE_PATTERN = "study/{deckId}"

            fun createRoute(deckId: Long): String = "study/$deckId"
        }
    }

    /** Statistics and progress overview. */
    data object Statistics : Screen("statistics")

    /** App settings. */
    data object Settings : Screen("settings")

    /** Import a deck (e.g., from CSV). */
    data class ImportDeck(
        val deckId: Long,
    ) : Screen("import_deck/{deckId}") {
        companion object {
            const val ROUTE_PATTERN = "import_deck/{deckId}"

            fun createRoute(deckId: Long): String = "import_deck/$deckId"
        }
    }
}

/**
 * List of top-level screens shown in the bottom navigation bar.
 */
val bottomNavScreens = listOf(
    Screen.DeckList,
    Screen.Statistics,
    Screen.Settings,
)
