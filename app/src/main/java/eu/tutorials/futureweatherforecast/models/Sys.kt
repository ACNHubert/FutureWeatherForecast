package eu.tutorials.futureweatherforecast.models


import java.io.Serializable

data class Sys(
    val type: Int,
    val message: Double,
    val country: String,
    val sunset: Int,
    val sunrise: Int
) : Serializable