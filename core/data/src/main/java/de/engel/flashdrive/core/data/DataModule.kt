package de.engel.flashdrive.core.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.engel.flashdrive.core.data.repository.CardRepository
import de.engel.flashdrive.core.data.repository.DeckRepository
import de.engel.flashdrive.core.data.repository.FlashcardRepository
import de.engel.flashdrive.core.data.repository.StudyRepository
import de.engel.flashdrive.core.data.repository.impl.DeckRepositoryImpl
import de.engel.flashdrive.core.data.repository.impl.FlashcardRepositoryImpl
import de.engel.flashdrive.core.data.repository.impl.StudyRepositoryImpl
import de.engel.flashdrive.core.domain.usecase.CardRepository as DomainCardRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindFlashcardRepository(
        impl: FlashcardRepositoryImpl,
    ): FlashcardRepository

    @Binds
    @Singleton
    abstract fun bindDeckRepository(
        impl: DeckRepositoryImpl,
    ): DeckRepository

    @Binds
    @Singleton
    abstract fun bindStudyRepository(
        impl: StudyRepositoryImpl,
    ): StudyRepository

    @Binds
    @Singleton
    abstract fun bindCardRepository(
        impl: CardRepository,
    ): DomainCardRepository
}
