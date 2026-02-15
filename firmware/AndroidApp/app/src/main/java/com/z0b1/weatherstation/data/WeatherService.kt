package com.z0b1.weatherstation.data

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    @Json(name = "Timestamp") val timestamp: String,
    @Json(name = "Temp") val temp: Double,
    @Json(name = "Hum") val hum: Double,
    @Json(name = "Speed") val speed: Double,
    @Json(name = "Heading") val heading: Int,
    @Json(name = "SoilTemp") val soilTemp: Double = 0.0,
    @Json(name = "SurfTemp") val surfTemp: Double = 0.0
)

data class SatelliteRecording(
    val name: String,
    val timestamp: String,
    val images: List<String>,
    val path: String
)

interface WeatherService {
    @GET("/weather/latest")
    suspend fun getLatestWeather(): WeatherResponse

    @GET("/weather/history")
    suspend fun getWeatherHistory(@Query("hours") hours: Int = 24): List<WeatherResponse>

    @GET("/satellites")
    suspend fun getSatellites(): List<SatelliteRecording>
}
