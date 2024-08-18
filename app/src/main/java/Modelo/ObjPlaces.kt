package Modelo

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ObjPlaces {
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

    val instance: AnsEstaciones by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AnsEstaciones::class.java)
    }
}