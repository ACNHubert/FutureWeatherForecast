package eu.tutorials.futureweatherforecast.futureForecast

import java.io.Serializable

data class Coord(
    val lat: Double,
    val lon: Double
) : Serializable