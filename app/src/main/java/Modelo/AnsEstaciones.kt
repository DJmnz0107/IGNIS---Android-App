package Modelo

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AnsEstaciones {
    @GET("place/nearbysearch/json")
    suspend fun searchNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") apiKey: String
    ): Response<RespuestaEstaciones>
}