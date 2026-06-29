package de.engel.flashdrive.feature.import.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.engel.flashdrive.core.data.repository.DeckRepository
import de.engel.flashdrive.core.data.repository.FlashcardRepository
import de.engel.flashdrive.core.domain.usecase.ImportCsvUseCase
import javax.inject.Singleton

/**
 * Dagger Hilt module for the import feature.
 *
 * Provides use-cases and feature-specific dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object ImportModule {

    /**
     * Provides the [ImportCsvUseCase] for CSV parsing.
     */
    @Provides
    @Singleton
    fun provideImportCsvUseCase(): ImportCsvUseCase = ImportCsvUseCase()
}
