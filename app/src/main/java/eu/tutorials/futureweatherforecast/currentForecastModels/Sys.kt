package eu.tutorials.futureweatherforecast.currentForecastModels


import java.io.Serializable

data class Sys(
    val type: Int,
    val message: Double,
    val country: String,
    val sunset: Int,
    val sunrise: Int
) : Serializable