package de.engel.flashdrive.feature.auto

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/**
 * Service entry point for Android Auto.
 * Hosts the FlashDrive session that drives the car display experience.
 */
class FlashDriveCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator {
        // For development / projected hosts allow all; production should restrict.
        return if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        } else {
            HostValidator.Builder(applicationContext)
                .addAllowedHosts("com.google.android.apps.automotive.car")
                .build()
        }
    }

    override fun onCreateSession(): Session {
        return FlashDriveSession()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}
