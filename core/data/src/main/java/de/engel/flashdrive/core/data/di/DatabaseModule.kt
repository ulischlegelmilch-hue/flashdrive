package de.engel.flashdrive.core.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.engel.flashdrive.core.database.FlashDriveDatabase
import de.engel.flashdrive.core.database.dao.DeckDao
import de.engel.flashdrive.core.database.dao.FlashcardDao
import de.engel.flashdrive.core.database.dao.StudyRecordDao
import de.engel.flashdrive.core.database.dao.StudySessionDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFlashDriveDatabase(
        @ApplicationContext context: Context,
    ): FlashDriveDatabase {
        return Room.databaseBuilder(
            context,
            FlashDriveDatabase::class.java,
            FlashDriveDatabase.DATABASE_NAME,
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDeckDao(database: FlashDriveDatabase): DeckDao {
        return database.deckDao()
    }

    @Provides
    @Singleton
    fun provideFlashcardDao(database: FlashDriveDatabase): FlashcardDao {
        return database.flashcardDao()
    }

    @Provides
    @Singleton
    fun provideStudySessionDao(database: FlashDriveDatabase): StudySessionDao {
        return database.studySessionDao()
    }

    @Provides
    @Singleton
    fun provideStudyRecordDao(database: FlashDriveDatabase): StudyRecordDao {
        return database.studyRecordDao()
    }
}
