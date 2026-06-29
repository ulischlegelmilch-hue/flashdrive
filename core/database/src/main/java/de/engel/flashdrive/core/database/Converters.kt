package de.engel.flashdrive.core.database

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split("|||")
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        if (list.isNullOrEmpty()) return null
        return list.joinToString("|||")
    }
}
