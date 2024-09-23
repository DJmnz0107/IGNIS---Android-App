import Modelo.DirectionsResponse
import Modelo.DirectionsService
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DirectionsApi(private val apiKey: String) {

    // Configuración de Retrofit para hacer solicitudes a la API de Google Maps.
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/maps/api/")  // URL base de la API.
        .addConverterFactory(GsonConverterFactory.create())  // Conversor para convertir JSON a objetos Kotlin.
        .build()

    // Creación del servicio que define las solicitudes a la API.
    private val service = retrofit.create(DirectionsService::class.java)

    // Método para obtener direcciones entre un origen y un destino.
    fun getDirections(
        origin: String,  // Punto de partida.
        destination: String,  // Punto de destino.
        mode: String,  // Modo de transporte (e.g., driving, walking).
        callback: (distanceText: String?, durationText: String?, error: Throwable?) -> Unit  // Callback para manejar la respuesta.
    ) {
        // Realiza una llamada asíncrona a la API de direcciones.
        service.getDirections(origin, destination, apiKey, mode).enqueue(object : retrofit2.Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: retrofit2.Response<DirectionsResponse>
            ) {
                // Verifica si la respuesta fue exitosa.
                if (response.isSuccessful) {
                    // Obtiene la primera ruta y su primer tramo.
                    val leg = response.body()?.routes?.firstOrNull()?.legs?.firstOrNull()
                    // Extrae la distancia y duración en formato de texto.
                    val distanceText = leg?.distance?.text
                    val durationText = leg?.duration?.text
                    // Llama al callback con los datos obtenidos.
                    callback(distanceText, durationText, null)
                } else {
                    // Si la respuesta no fue exitosa, llama al callback con un error.
                    callback(null, null, Exception("Response not successful"))
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                // Maneja fallas en la llamada, llamando al callback con el error.
                callback(null, null, t)
            }
        })
    }
}

