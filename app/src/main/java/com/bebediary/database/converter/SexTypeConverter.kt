package com.bebediary.database.converter

import androidx.room.TypeConverter
import com.bebediary.database.entity.Sex

class SexTypeConverter {

    @TypeConverter
    fun sexToString(sex: Sex?): String? = sex?.value

    @TypeConverter
    fun stringToSex(sex: String?): Sex? =
        when (sex) {
            "male" -> Sex.Male
            "female" -> Sex.Female
            else -> null
        }
}
