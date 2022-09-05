package eu.tutorials.futureweatherforecast.futureForecast

import java.io.Serializable

data class Wind(
    val deg: Int,
    val gust: Double,
    val speed: Double
) : Serializable