package de.engel.flashdrive.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Application-level Hilt module for providing app-scoped dependencies.
 *
 * Note: Repository bindings are already provided by [de.engel.flashdrive.core.data.DataModule].
 * This module is reserved for app-specific dependencies (e.g., DataStore preferences,
 * WorkManager configuration, or analytics trackers) that don't belong in core modules.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Add app-scoped @Provides / @Binds here as needed.
    // Example:
    // @Provides
    // @Singleton
    // fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
    //     PreferenceDataStoreFactory.create { context.dataStoreFile("settings.preferences_pb") }
}
