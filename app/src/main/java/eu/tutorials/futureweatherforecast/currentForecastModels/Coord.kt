package eu.tutorials.futureweatherforecast.currentForecastModels

import java.io.Serializable

data class Coord(
    val lon: Double,
    val lat: Double
) : Serializable