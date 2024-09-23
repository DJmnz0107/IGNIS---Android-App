package Modelo

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AnsEstaciones {  // Interfaz para operaciones de API.

    @GET("place/nearbysearch/json")  // URL para buscar lugares cercanos.
    suspend fun searchNearbyPlaces(  // Método asíncrono para buscar lugares.
        @Query("location") location: String,  // Ubicación en formato "lat,lng".
        @Query("radius") radius: Int,  // Radio de búsqueda en metros.
        @Query("type") type: String,  // Tipo de lugar (ej. "fire_station").
        @Query("key") apiKey: String  // Clave de API para autenticar la solicitud.
    ): Response<RespuestaEstaciones>  // Respuesta de la API que contiene datos de lugares.
}
