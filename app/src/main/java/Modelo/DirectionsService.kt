package Modelo

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsService {
    // Define una solicitud GET a la API de direcciones de Google Maps.
    @GET("directions/json")
    fun getDirections(
        @Query("origin") origin: String,  // Punto de partida.
        @Query("destination") destination: String,  // Punto de destino.
        @Query("key") apiKey: String,  // Clave API para autenticar la solicitud.
        @Query("mode") mode: String  // Modo de transporte (e.g., driving, walking).
    ): Call<DirectionsResponse>  // Retorna un objeto Call que contendrá la respuesta de tipo DirectionsResponse.
}
