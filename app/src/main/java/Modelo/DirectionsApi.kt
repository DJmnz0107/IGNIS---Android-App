import Modelo.DirectionsResponse
import Modelo.DirectionsService
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DirectionsApi(private val apiKey: String) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/maps/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(DirectionsService::class.java)

    fun getDirections(
        origin: String,
        destination: String,
        mode: String,
        callback: (distanceText: String?, durationText: String?, error: Throwable?) -> Unit
    ) {
        service.getDirections(origin, destination, apiKey, mode).enqueue(object : retrofit2.Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: retrofit2.Response<DirectionsResponse>
            ) {
                if (response.isSuccessful) {
                    val leg = response.body()?.routes?.firstOrNull()?.legs?.firstOrNull()
                    val distanceText = leg?.distance?.text
                    val durationText = leg?.duration?.text
                    callback(distanceText, durationText, null)
                } else {
                    callback(null, null, Exception("Response not successful"))
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                callback(null, null, t)
            }
        })
    }
}
