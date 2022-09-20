package eu.tutorials.futureweatherforecast.currentForecastModels

import java.io.Serializable

data class Wind(
    val speed: Double,
    val deg: Int
) : Serializable