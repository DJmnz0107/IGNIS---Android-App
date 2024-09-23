package Modelo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

object LocationEventBus {
    // MutableLiveData para almacenar actualizaciones de ubicación.
    private val _locationUpdates = MutableLiveData<LatLng>()

    // LiveData pública para observar las actualizaciones de ubicación.
    val locationUpdates: LiveData<LatLng> get() = _locationUpdates

    // Método para publicar una nueva actualización de ubicación.
    fun postLocationUpdate(location: LatLng) {
        _locationUpdates.postValue(location)  // Publica el nuevo valor de ubicación.
    }
}
