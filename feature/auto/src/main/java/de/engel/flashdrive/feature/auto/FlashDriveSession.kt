package de.engel.flashdrive.feature.auto

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

/**
 * Root session for the FlashDrive Android Auto experience.
 * Presents the deck list as the first screen when the car connects.
 */
class FlashDriveSession : Session() {

    override fun onCreateScreen(intent: Intent): Screen {
        return AutoDeckListScreen(carContext = carContext)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }
}
