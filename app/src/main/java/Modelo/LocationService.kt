package Modelo

import N.J.L.F.S.Q.ignis_ptc.MainActivity
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine


@OptIn(ExperimentalCoroutinesApi::class)
class LocationService(private val activity: MainActivity) {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            onPermissionsGranted()
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionsGranted()
                } else {
                    onPermissionsDenied()
                }
            }
        }
    }

    private fun onPermissionsGranted() {
        CoroutineScope(Dispatchers.Main).launch {
            val location = getUserLocation(activity)
            location?.let {
                val location = "Latitud: ${it.latitude}, Longitud: ${it.longitude}"
                showToast("Ubicación obtenida: $location")
            }
        }
    }

    private fun onPermissionsDenied() {
        showToast("Permisos de ubicación no concedidos")
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }


    @SuppressLint("MissingPermission")
    suspend fun getUserLocation(context:Context):android.location.Location? {
        val fusedLocationproviderClient = LocationServices.getFusedLocationProviderClient(context)
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)


        if (!isGPSEnabled) {
            showToast("Activa el GPS para obtener la ubicación")
            return null
    }

        return suspendCancellableCoroutine { cont ->
            fusedLocationproviderClient.lastLocation.apply {
                if(isComplete) {
                    if(isSuccessful) {
                        cont.resume(result){}
                    } else {
                        cont.resume(null){}
                    }
                    return@suspendCancellableCoroutine
                }
                addOnSuccessListener {
                    cont.resume(it){}
                }
                addOnFailureListener {
                    cont.resume(null){}
                }
                addOnCanceledListener {
                    cont.resume(null){}
                }
            }

        }
}








}