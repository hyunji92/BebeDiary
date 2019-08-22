package com.bebediary.data

data class AirQuality(
    val dataTime: String,
    val sidoName: String,
    val cityName: String,
    val pm10: Int?
)