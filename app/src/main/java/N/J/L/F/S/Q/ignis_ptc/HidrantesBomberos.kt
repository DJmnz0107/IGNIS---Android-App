package N.J.L.F.S.Q.ignis_ptc

import DirectionsApi
import Modelo.LocationService
import Modelo.LocationServiceBomberos
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HidrantesBomberos.newInstance] factory method to
 * create an instance of this fragment.
 */
class HidrantesBomberos : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapHidrantesBomberos) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_hidrantes_bomberos, container, false)

        lblDistancia = root.findViewById(R.id.lblDistanciaBombero)
        lblDistanciaAuto = root.findViewById(R.id.lblConduciendoBombero)
        lblDistanciaCaminando = root.findViewById(R.id.lblCaminandoBombero)


        imgDistancia = root.findViewById(R.id.imgDistanciaBombero)

        imgCaminar = root.findViewById(R.id.imgCaminandoBombero)

        imgConducir = root.findViewById(R.id.imgConduciendoBombero)

        imgDistancia.visibility = View.GONE

        imgCaminar.visibility = View.GONE

        imgConducir.visibility = View.GONE

        val imgBack = root.findViewById<ImageView>(R.id.imgBack)

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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HidrantesBomberos.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HidrantesBomberos().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
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
            mostrarAlertDialog(latLng)
        }

        map.setOnCameraIdleListener {
            ajustarVisibilidadMarcadores(map.cameraPosition.zoom)
        }

        map.setOnMarkerClickListener { marker ->
            if (marker != currentLocationMarker) {
                val origin = ubicacion?.let { "${it.latitude},${it.longitude}" } ?: return@setOnMarkerClickListener true
                val destination = "${marker.position.latitude},${marker.position.longitude}"

                val directionsApi = DirectionsApi(apiKey)

                imgDistancia.visibility = View.VISIBLE
                imgCaminar.visibility = View.VISIBLE
                imgConducir.visibility = View.VISIBLE

                directionsApi.getDirections(origin, destination, "driving") { distanceText, durationText, error ->
                    if (error == null) {
                        val distanceInKm = distanceText?.let { convertirDistanciaAKm(it) }
                        Log.d("DirectionsApi", "distanceInKm: $distanceInKm, durationText: $durationText")
                        lblDistanciaAuto.text = "${durationText ?: "N/A"}"
                        lblDistancia.text = "${distanceInKm ?: "N/A"} km"
                    } else {
                        Log.e("DirectionsApi", "Error al obtener direcciones en carro: ${error.message}")
                    }
                }

                directionsApi.getDirections(origin, destination, "walking") { _, durationText, error ->
                    if (error == null) {
                        lblDistanciaCaminando.text = "${durationText ?: "N/A"}"
                    } else {
                        Log.e("DirectionsApi", "Error al obtener direcciones a pie: ${error.message}")
                    }
                }

            } else {
                imgDistancia.visibility = View.GONE
                imgCaminar.visibility = View.GONE
                imgConducir.visibility = View.GONE

                lblDistanciaAuto.text = ""
                lblDistancia.text = ""
                lblDistanciaCaminando.text = ""
            }

            marker.showInfoWindow()
            true
        }



        map.setOnInfoWindowClickListener { marker ->
            eliminarAlertDialog(marker)
        }


        CoroutineScope(Dispatchers.IO).launch {
            ubicacion = withContext(Dispatchers.IO) {
                LocationServiceBomberos(context as activity_bomberos).ubicacionLatLng(context as activity_bomberos)
            }
            withContext(Dispatchers.Main) {
                mostrarUbicacionActual(ubicacion)
                cargarMarcadores()
            }
        }
    }

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
    private fun mostrarAlertDialogError(message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Error")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
        builder.show()
    }
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

    private fun ajustarVisibilidadMarcadores(zoom: Float) {
        for (marker in markers) {
            marker.isVisible = zoom >= 15
        }
    }
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




}