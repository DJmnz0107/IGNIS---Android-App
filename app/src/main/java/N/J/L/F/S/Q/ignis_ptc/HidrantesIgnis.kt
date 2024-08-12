package N.J.L.F.S.Q.ignis_ptc

import DirectionsApi
import Modelo.LocationService
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val MARKERS_PREF = "markers_pref"
private const val MARKERS_KEY = "markers_key"

class HidrantesIgnis : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val apiKey = "AIzaSyCH9y8qPHiq4nT-te8GE4BYvKNFshV5ZG0"
    private lateinit var distanceTextView: TextView
    private lateinit var durationByCarTextView: TextView
    private lateinit var durationByWalkingTextView: TextView
    private lateinit var imgDistancia: ImageView
    private lateinit var imgCaminar: ImageView
    private lateinit var imgConducir:ImageView
    private val markers = mutableListOf<Marker>()
    private var ubicacion: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_hidrantes_ignis, container, false)

        // Obtener el fragmento del mapa
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapHidrantes) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        distanceTextView = root.findViewById(R.id.lblDistancia)
        durationByCarTextView = root.findViewById(R.id.lblConduciendo)
        durationByWalkingTextView = root.findViewById(R.id.lblCaminando)

        val imgBack = root.findViewById<ImageView>(R.id.imgBack)



        imgDistancia = root.findViewById(R.id.imgLocation)

        imgCaminar = root.findViewById(R.id.imgWalk)

        imgConducir = root.findViewById(R.id.imgDrive)

        imgDistancia.visibility = View.GONE

        imgCaminar.visibility = View.GONE

        imgConducir.visibility = View.GONE


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.haciaUbicaciones)
            }
        })

        imgBack.setOnClickListener {
            findNavController().navigate(R.id.haciaUbicaciones)
        }

        return root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true

        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.mapstyles)
            )
            if (!success) {
                Log.e("HidrantesIgnis", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("HidrantesIgnis", "Can't find style. Error: ", e)
        }

        map.setOnMapClickListener { latLng ->
            showMarkerDialog(latLng)
        }

        map.setOnMarkerClickListener { marker ->
            val origin = ubicacion?.let { "${it.latitude},${it.longitude}" } ?: return@setOnMarkerClickListener true
            val destination = "${marker.position.latitude},${marker.position.longitude}"

            val directionsApi = DirectionsApi(apiKey)

            imgDistancia.visibility = View.VISIBLE
            imgCaminar.visibility = View.VISIBLE
            imgConducir.visibility = View.VISIBLE

            directionsApi.getDirections(origin, destination, "driving") { distanceText, durationText, error ->
                if (error == null) {
                    val distanceInKm = distanceText?.let { convertirMillasAKm(it) }
                    durationByCarTextView.text = "${durationText ?: "N/A"}"
                    distanceTextView.text = "${distanceInKm ?: "N/A"} km"
                } else {
                    Log.e("DirectionsApi", "Error al obtener direcciones en carro: ${error?.message}")
                }
            }

            directionsApi.getDirections(origin, destination, "walking") { _, durationText, error ->
                if (error == null) {
                    durationByWalkingTextView.text = "${durationText ?: "N/A"}"
                } else {
                    Log.e("DirectionsApi", "Error al obtener direcciones a pie: ${error?.message}")
                }
            }

            marker.showInfoWindow()

            true
        }

        map.setOnInfoWindowClickListener { marker ->
            showDeleteMarkerDialog(marker)
        }


        CoroutineScope(Dispatchers.IO).launch {
            ubicacion = withContext(Dispatchers.IO) {
                LocationService(context as MainActivity).ubicacionLatLng(requireContext())
            }
            withContext(Dispatchers.Main) {
                posicionActual(ubicacion)
                animacionMarcador(ubicacion)
                loadMarkers()
            }
        }
    }

    private fun convertirMillasAKm(distanceText: String): String? {
        val distanciaMillas = distanceText.split(" ")?.firstOrNull()?.toDoubleOrNull()
        return distanciaMillas?.let {
            val distanciaKm = it * 1.60934
            String.format("%.2f", distanciaKm)
        }
    }


    private fun posicionActual(ubicacion: LatLng?) {
        if (ubicacion != null) {
            val markerOption = MarkerOptions().position(ubicacion).title("Mi Ubicación")
            map.addMarker(markerOption)
        } else {
            Log.d("HidrantesIgnis", "Ubicación no válida.")
        }
    }

    private fun animacionMarcador(coordenadas: LatLng?) {
        if (coordenadas != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(coordenadas, 18f), 4000, null)
        }
    }

    private fun showMarkerDialog(latLng: LatLng) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Añadir marcador")

        val input = EditText(requireContext())
        input.hint = "Nombre del marcador"
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, which ->
            val title = input.text.toString().trim()

            if (title.isNotEmpty()) {
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(getMarkerIconFromDrawable(R.drawable.hidranteicono))
                )
                if (marker != null) {
                    markers.add(marker)
                    saveMarkers()
                    map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                }
            } else {
                showErrorDialog("El nombre del marcador no puede estar vacío.")
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun showErrorDialog(message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Error")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
        builder.show()
    }

    private fun showDeleteMarkerDialog(marker: Marker) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Eliminar marcador")
        builder.setMessage("¿Desea eliminar este marcador?")

        builder.setPositiveButton("Sí") { dialog, which ->
            marker.remove()
            markers.remove(marker)
            saveMarkers()
        }
        builder.setNegativeButton("No") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun saveMarkers() {
        val sharedPreferences = requireActivity().getSharedPreferences(MARKERS_PREF, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val jsonArray = JSONArray()

        for (marker in markers) {
            val jsonObject = JSONObject()
            jsonObject.put("title", marker.title)
            jsonObject.put("latitude", marker.position.latitude)
            jsonObject.put("longitude", marker.position.longitude)
            jsonArray.put(jsonObject)
        }

        editor.putString(MARKERS_KEY, jsonArray.toString())
        editor.apply()
    }

    private fun loadMarkers() {
        val sharedPreferences = requireActivity().getSharedPreferences(MARKERS_PREF, Context.MODE_PRIVATE)
        val jsonString = sharedPreferences.getString(MARKERS_KEY, null)
        Log.d("HidrantesIgnis", "Cargando marcadores: $jsonString")

        if (jsonString != null) {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val title = jsonObject.getString("title")
                val latitude = jsonObject.getDouble("latitude")
                val longitude = jsonObject.getDouble("longitude")
                val latLng = LatLng(latitude, longitude)
                val marker = map.addMarker(MarkerOptions().position(latLng).title(title).icon(getMarkerIconFromDrawable(R.drawable.hidranteicono)))
                if (marker != null) {
                    markers.add(marker)
                } else {
                    Log.d("HidrantesIgnis", "Error al añadir marcador desde SharedPreferences.")
                }
            }
        }
    }

    private fun getMarkerIconFromDrawable(drawableId: Int): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(requireContext(), drawableId)
        val canvas = Canvas()
        val width = 128
        val height = 128
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable?.setBounds(0, 0, width, height)
        drawable?.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
