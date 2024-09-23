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
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
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
    private lateinit var lblDistancia: TextView
    private lateinit var lblDistanciaAuto: TextView
    private lateinit var lblDistanciaCaminando: TextView
    private lateinit var imgDistancia: ImageView
    private lateinit var imgCaminar: ImageView
    private lateinit var imgConducir:ImageView
    private val markers = mutableListOf<Marker>()
    private var ubicacion: LatLng? = null
    private var currentLocationMarker: Marker? = null
    private val firestore = FirebaseFirestore.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_hidrantes_ignis, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapHidrantes) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        lblDistancia = root.findViewById(R.id.lblDistancia)
        lblDistanciaAuto = root.findViewById(R.id.lblConduciendo)
        lblDistanciaCaminando = root.findViewById(R.id.lblCaminando)

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

    // Configura todas las opciones del mapa
    override fun onMapReady(googleMap: GoogleMap) {
        // Inicializa el objeto 'map' con la instancia del mapa proporcionada
        map = googleMap

        // Habilita los controles de zoom en la interfaz del mapa
        map.uiSettings.isZoomControlsEnabled = true

        // Intenta aplicar un estilo personalizado al mapa
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.mapstyles)
            )
            // Verifica si la aplicación del estilo fue exitosa
            if (!success) {
                Log.e("HidrantesIgnis", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            // Maneja el caso en que el recurso del estilo no se encuentra
            Log.e("HidrantesIgnis", "Can't find style. Error: ", e)
        }

        // Configura un listener para manejar clics en el mapa
        map.setOnMapClickListener { latLng ->
            mostrarAlertDialog(latLng) // Muestra un diálogo de alerta en la posición clickeada
        }

        // Configura un listener que se activa cuando la cámara del mapa se detiene
        map.setOnCameraIdleListener {
            ajustarVisibilidadMarcadores(map.cameraPosition.zoom) // Ajusta la visibilidad de los marcadores según el zoom
        }

        // Configura un listener para manejar clics en los marcadores
        map.setOnMarkerClickListener { marker ->
            // Verifica si el marcador clickeado no es el de la ubicación actual
            if (marker != currentLocationMarker) {
                // Obtiene la ubicación de origen
                val origin = ubicacion?.let { "${it.latitude},${it.longitude}" } ?: return@setOnMarkerClickListener true
                // Establece la ubicación de destino desde el marcador
                val destination = "${marker.position.latitude},${marker.position.longitude}"

                // Crea una instancia de la API de direcciones
                val directionsApi = DirectionsApi(apiKey)

                // Muestra los íconos de distancia y rutas
                imgDistancia.visibility = View.VISIBLE
                imgCaminar.visibility = View.VISIBLE
                imgConducir.visibility = View.VISIBLE

                // Llama a la API para obtener direcciones en carro
                directionsApi.getDirections(origin, destination, "driving") { distanceText, durationText, error ->
                    if (error == null) {
                        // Convierte la distancia a kilómetros y muestra la información
                        val distanceInKm = distanceText?.let { convertirDistanciaAKm(it) }
                        Log.d("DirectionsApi", "distanceInKm: $distanceInKm, durationText: $durationText")
                        lblDistanciaAuto.text = "${durationText ?: "N/A"}"
                        lblDistancia.text = "${distanceInKm ?: "N/A"} km"
                    } else {
                        Log.e("DirectionsApi", "Error al obtener direcciones en carro: ${error.message}")
                    }
                }

                // Llama a la API para obtener direcciones a pie
                directionsApi.getDirections(origin, destination, "walking") { _, durationText, error ->
                    if (error == null) {
                        lblDistanciaCaminando.text = "${durationText ?: "N/A"}"
                    } else {
                        Log.e("DirectionsApi", "Error al obtener direcciones a pie: ${error.message}")
                    }
                }

            } else {
                // Oculta los íconos y limpia los textos si se clickea en la ubicación actual
                imgDistancia.visibility = View.GONE
                imgCaminar.visibility = View.GONE
                imgConducir.visibility = View.GONE

                lblDistanciaAuto.text = ""
                lblDistancia.text = ""
                lblDistanciaCaminando.text = ""
            }

            // Muestra la ventana de información del marcador clickeado
            marker.showInfoWindow()
            true
        }

        // Configura un listener para manejar clics en la ventana de información del marcador
        map.setOnInfoWindowClickListener { marker ->
            eliminarAlertDialog(marker) // Elimina el diálogo de alerta asociado al marcador
        }

        // Inicia una corrutina para obtener la ubicación actual y cargar marcadores
        CoroutineScope(Dispatchers.IO).launch {
            ubicacion = withContext(Dispatchers.IO) {
                LocationService(context as MainActivity).ubicacionLatLng(context as MainActivity)
            }
            withContext(Dispatchers.Main) {
                mostrarUbicacionActual(ubicacion) // Muestra la ubicación actual en el mapa
                cargarMarcadores() // Carga los marcadores en el mapa
            }
        }
    }


    //Muestra la ubicación actual a través de un circulo
    private fun mostrarUbicacionActual(location: LatLng?) {
        if (location != null) {
            val circleOptions = CircleOptions()
                .center(location)
                .radius(30.0)
                .strokeColor(ContextCompat.getColor(requireContext(), R.color.naranjaDos))
                .fillColor(ContextCompat.getColor(requireContext(), R.color.Amarillo))
                .strokeWidth(20f)
            map.addCircle(circleOptions)

            val markerOptions = MarkerOptions()
                .position(location)
                .title("Mi Ubicación")
                .icon(
                    BitmapDescriptorFactory.fromBitmap(
                        Bitmap.createBitmap(
                            1,
                            1,
                            Bitmap.Config.ARGB_8888
                        )
                    )
                )
            currentLocationMarker = map.addMarker(markerOptions)

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18f))
        }
    }


    //Convierte la distancia a KM
    private fun convertirDistanciaAKm(distanceText: String): String? {
        Log.d("convertirDistanciaAKm", "distanceText: $distanceText")

        val parts = distanceText.split(" ")
        if (parts.size < 2) {
            Log.e("convertirDistanciaAKm", "Formato de distancia no válido: $distanceText")
            return null
        }

        val distanciaValor = parts[0].toDoubleOrNull()
        val unidad = parts[1]

        Log.d("convertirDistanciaAKm", "distanciaValor: $distanciaValor, unidad: $unidad")

        return distanciaValor?.let {
            when (unidad) {
                "ft" -> {
                    val distanciaKm = it * 0.0003048
                    String.format("%.2f", distanciaKm)
                }
                "mi" -> {
                    val distanciaKm = it * 1.60934
                    String.format("%.2f", distanciaKm)
                }
                else -> {
                    Log.e("convertirDistanciaAKm", "Unidad desconocida: $unidad")
                    null
                }
            }
        } ?: run {
            Log.e("convertirDistanciaAKm", "No se pudo convertir distancia: $distanceText")
            null
        }
    }




    private fun ajustarVisibilidadMarcadores(zoom: Float) {
        for (marker in markers) {
            marker.isVisible = zoom >= 15
        }
    }


    private fun animacionMarcador(coordenadas: LatLng?) {
        if (coordenadas != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(coordenadas, 18f), 4000, null)
        }
    }

    //Muestra el alert dialog para añadir el marcador
    private fun mostrarAlertDialog(latLng: LatLng) {
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
                        .icon(iconoMarcador(R.drawable.hidranteicono))
                )
                if (marker != null) {
                    markers.add(marker)
                    saveMarkers()
                    map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                }
            } else {
                mostrarAlertDialogError("El nombre del marcador no puede estar vacío.")
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun mostrarAlertDialogError(message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Error")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
        builder.show()
    }

    //Muestra el alert dialog para eliminar el marcador
    private fun eliminarAlertDialog(marker: Marker) {
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

    //Guarda los marcadores en firebase
    private fun saveMarkers() {
        val markersCollection = firestore.collection("MarcadoresHidrantes")
        markersCollection.get().addOnSuccessListener { result ->
            for (document in result) {
                document.reference.delete()
            }

            for (marker in markers) {
                val markerData = hashMapOf(
                    "title" to marker.title,
                    "latitude" to marker.position.latitude,
                    "longitude" to marker.position.longitude
                )
                markersCollection.add(markerData)
            }
        }.addOnFailureListener { e ->
            Log.w("Firestore", "Error getting documents: ", e)
        }
    }


    //Carga las ubicaciones de la colección y las transforma a marcadores
    private fun cargarMarcadores() {
        val markersCollection = firestore.collection("MarcadoresHidrantes")
        markersCollection.get().addOnSuccessListener { result ->
            for (document in result) {
                val title = document.getString("title") ?: "Sin título"
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0
                val latLng = LatLng(latitude, longitude)
                val marker = map.addMarker(
                    MarkerOptions().position(latLng).title(title).icon(iconoMarcador(R.drawable.hidranteicono))
                )
                if (marker != null) {
                    markers.add(marker)
                } else {
                    Log.d("HidrantesIgnis", "Error al añadir marcador desde Firestore.")
                }
            }
        }.addOnFailureListener { e ->
            Log.w("Firestore", "Error getting documents: ", e)
        }
    }


    //Imagen del marcador
    private fun iconoMarcador(drawableId: Int): BitmapDescriptor {
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
