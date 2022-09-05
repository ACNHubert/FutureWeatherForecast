package eu.tutorials.futureweatherforecast.futureForecast

import java.io.Serializable

data class FutureWeather(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<WeatherResponse>,
    val message: Int
) : Serializable