package eu.tutorials.futureweatherforecast.futureForecastModel

import java.io.Serializable

data class Coord(
    val lat: Double,
    val lon: Double
) : Serializable