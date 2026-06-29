package de.engel.flashdrive.feature.auto.screen

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import de.engel.flashdrive.core.model.Deck
import de.engel.flashdrive.feature.auto.R

/**
 * Screen that displays the list of available decks in the car.
 * Uses a ListTemplate with a maximum of 6 items (Android Auto best practice).
 * Each deck row has a single "Lernen" action to start studying.
 */
class AutoDeckListScreen(
    carContext: CarContext,
    private val decks: List<Deck> = emptyList(),
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val itemListBuilder = ItemList.Builder()

        // Android Auto recommends max items for glanceable UI.
        val displayDecks = decks.take(MAX_DECKS)

        for (deck in displayDecks) {
            val dueInfo = if (dueCardsCount(deck) > 0) {
                " • ${dueCardsCount(deck)} fällig"
            } else {
                ""
            }

            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle(deck.name)
                    .addText(deck.cardCount.toString() + " Karten" + dueInfo)
                    .setOnClickListener {
                        startStudySession(deck)
                    }
                    .build()
            )
        }

        return ListTemplate.Builder()
            .setSingleList(itemListBuilder.build())
            .setHeaderAction(Action.BACK)
            .setTitle("FlashDrive – Decks")
            .build()
    }

    private fun startStudySession(deck: Deck) {
        screenManager.push(
            AutoStudyScreen(
                carContext = carContext,
                deckId = deck.id,
                deckName = deck.name,
            )
        )
    }

    private fun dueCardsCount(deck: Deck): Int = deck.dueCardsCount

    companion object {
        /** Maximum number of items recommended for glanceable car UI. */
        private const val MAX_DECKS = 6
    }
}
