package Modelo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

object LocationEventBus {
    private val _locationUpdates = MutableLiveData<LatLng>()
    val locationUpdates: LiveData<LatLng> get() = _locationUpdates

    fun postLocationUpdate(location: LatLng) {
        _locationUpdates.postValue(location)
    }

}