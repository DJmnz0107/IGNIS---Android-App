package Modelo

import com.google.android.gms.maps.model.LatLng

interface UbicacionCallBack {
    fun onUbicacionRecibida(ubicacion: LatLng)
}