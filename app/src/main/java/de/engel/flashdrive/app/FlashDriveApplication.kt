package de.engel.flashdrive.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for FlashDrive.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation
 * and provide the application-level dependency container.
 */
@HiltAndroidApp
class FlashDriveApplication : Application()
