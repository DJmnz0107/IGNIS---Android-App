package Modelo

import N.J.L.F.S.Q.ignis_ptc.EstacionesMaps
import N.J.L.F.S.Q.ignis_ptc.MainActivity
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext


@OptIn(ExperimentalCoroutinesApi::class)
class LocationService(private val activity: MainActivity) {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    // Verifica y solicita permisos de ubicación.
    fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicita permisos de ubicación si no han sido concedidos.
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Llama al método si los permisos ya están concedidos.
            onPermissionsGranted()
        }
    }

    // Maneja el resultado de la solicitud de permisos.
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                // Verifica si los permisos fueron concedidos.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionsGranted()
                } else {
                    onPermissionsDenied()
                }
            }
        }
    }

    // Obtiene la ubicación del usuario de forma asíncrona y devuelve un String.
    suspend fun getUbicacionAsync(activity: Context): String {
        return withContext(Dispatchers.Main) {
            val location = getUserLocation(activity)
            location?.let {
                return@withContext "${it.latitude} ${it.longitude}"
            }
            return@withContext "Ubicación no disponible"
        }
    }

    // Obtiene la ubicación del usuario y devuelve un objeto LatLng.
    suspend fun ubicacionLatLng(activity: Context): LatLng? {
        return withContext(Dispatchers.Main) {
            val location = getUserLocation(activity)
            location?.let {
                return@withContext LatLng(it.latitude, it.longitude)
            }
            return@withContext null
        }
    }

    // Se llama cuando los permisos son concedidos.
    private fun onPermissionsGranted() {
        CoroutineScope(Dispatchers.Main).launch {
            val location = getUserLocation(activity)
            location?.let {
                // Crea un string con la ubicación y notifica a través de un bus de eventos.
                val ubicacion = "Latitud: ${it.latitude}, Longitud: ${it.longitude}"
                val latLng = LatLng(it.latitude, it.longitude)
                LocationEventBus.postLocationUpdate(latLng)
            }
        }
    }

    // Se llama cuando los permisos son denegados.
    private fun onPermissionsDenied() {
        showToast("Permisos de ubicación no concedidos")
    }

    // Muestra un mensaje Toast.
    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    // Obtiene la ubicación del usuario de forma suspendida.
    suspend fun getUserLocation(context: Context): android.location.Location? {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Verifica si el GPS está habilitado.
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!isGPSEnabled) {
            showToast("Activa el GPS para obtener la ubicación")
            return null
        }

        // Usar una coroutine para obtener la ubicación.
        return suspendCancellableCoroutine { cont ->
            fusedLocationProviderClient.lastLocation.apply {
                // Si la tarea ya se completó, maneja el resultado.
                if (isComplete) {
                    if (isSuccessful) {
                        cont.resume(result) {}
                    } else {
                        cont.resume(null) {}
                    }
                    return@suspendCancellableCoroutine
                }
                // Agrega escuchadores para manejar el éxito o el fracaso.
                addOnSuccessListener {
                    cont.resume(it) {}
                }
                addOnFailureListener {
                    cont.resume(null) {}
                }
                addOnCanceledListener {
                    cont.resume(null) {}
                }
            }
        }
    }
}
