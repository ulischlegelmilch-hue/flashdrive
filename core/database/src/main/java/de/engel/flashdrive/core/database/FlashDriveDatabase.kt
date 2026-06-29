package de.engel.flashdrive.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.engel.flashdrive.core.database.dao.DeckDao
import de.engel.flashdrive.core.database.dao.FlashcardDao
import de.engel.flashdrive.core.database.dao.StudyRecordDao
import de.engel.flashdrive.core.database.dao.StudySessionDao
import de.engel.flashdrive.core.database.entity.DeckEntity
import de.engel.flashdrive.core.database.entity.FlashcardEntity
import de.engel.flashdrive.core.database.entity.StudyRecordEntity
import de.engel.flashdrive.core.database.entity.StudySessionEntity

@Database(
    entities = [
        DeckEntity::class,
        FlashcardEntity::class,
        StudySessionEntity::class,
        StudyRecordEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class FlashDriveDatabase : RoomDatabase() {

    abstract fun deckDao(): DeckDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun studyRecordDao(): StudyRecordDao

    companion object {
        const val DATABASE_NAME = "flashdrive.db"
    }
}
