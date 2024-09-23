package Modelo

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ObjPlaces {
    // URL base para la API de Google Maps.
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

    // Instancia de AnsEstaciones creada de manera perezosa (lazy) para asegurar que solo se inicialice cuando se necesite.
    val instance: AnsEstaciones by lazy {
        Retrofit.Builder()
            // Configura la URL base para las solicitudes.
            .baseUrl(BASE_URL)
            // Añade un convertidor para manejar la conversión de JSON a objetos de Kotlin.
            .addConverterFactory(GsonConverterFactory.create())
            // Crea una instancia de Retrofit.
            .build()
            // Crea una implementación de la interfaz AnsEstaciones para realizar las llamadas a la API.
            .create(AnsEstaciones::class.java)
    }
}
