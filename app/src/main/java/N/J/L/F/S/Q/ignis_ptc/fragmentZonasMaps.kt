package N.J.L.F.S.Q.ignis_ptc

import Modelo.LocationService
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
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

class fragmentZonasMaps : Fragment(), OnMapReadyCallback {
    private var param1: String? = null
    private var param2: String? = null
    private val markers = mutableListOf<Marker>()
    private lateinit var map: GoogleMap
    private var ubicacion: LatLng? = null
    private var ubicacionActualMaracador: Marker? = null
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapZonas) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_zonas_maps, container, false)

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
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            fragmentZonasMaps().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        map.setOnMapClickListener { latLng ->
            showAddMarkerDialog(latLng)
        }

        map.setOnInfoWindowClickListener { marker ->
            if (marker != ubicacionActualMaracador) {
                eliminarAlertDialog(marker)
            } else {

            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            ubicacion = withContext(Dispatchers.IO) {
                LocationService(context as MainActivity).ubicacionLatLng(context as MainActivity)
            }
            withContext(Dispatchers.Main) {
                mostrarUbicacionActual(ubicacion)
                cargarMarcadores()
            }
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
            ubicacionActualMaracador = map.addMarker(markerOptions)

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18f))
        }
    }

    private fun eliminarAlertDialog(marker: Marker) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Eliminar marcador")
        builder.setMessage("¿Desea eliminar este marcador?")

        builder.setPositiveButton("Sí") { dialog, which ->
            marker.remove()
            markers.remove(marker)
            guardarMarcadores()
        }
        builder.setNegativeButton("No") { dialog, which -> dialog.cancel() }

        builder.show()
    }





    private fun cargarMarcadores() {
        val markersCollection = firestore.collection("MarcadoresHidrantes")
        markersCollection.get().addOnSuccessListener { result ->
            for (document in result) {
                val title = document.getString("title") ?: "Sin título"
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0
                val severity = document.getString("severity") ?: "bajo"

                val iconResource = when (severity) {
                    "alto" -> R.drawable.peligrorojo
                    "medio" -> R.drawable.peligroamarillo
                    else -> R.drawable.peligroverde
                }

                val latLng = LatLng(latitude, longitude)
                val marker = map.addMarker(
                    MarkerOptions().position(latLng).title(title).icon(iconoMarcador(iconResource))
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

    private fun iconoMarcador(resourceId: Int): BitmapDescriptor {
        return BitmapDescriptorFactory.fromResource(resourceId)
    }






    private fun showAddMarkerDialog(latLng: LatLng) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.alertzonas, null)

        val spinner = dialogView.findViewById<Spinner>(R.id.spTipoEmergenciaZonas)
        val types = arrayOf("Baja", "Moderada", "Alta")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.show()

        val btnAdd = dialogView.findViewById<Button>(R.id.btnAgregarZona)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelarZona)

        btnAdd.setOnClickListener {
            val title = dialogView.findViewById<EditText>(R.id.txtMarcador).text.toString()
            val selectedType = spinner.selectedItem.toString()

            val markerImage = when (selectedType) {
                "Baja" -> R.drawable.peligroverde
                "Moderada" -> R.drawable.peligroamarillo
                "Alta" -> R.drawable.peligrorojo
                else -> R.drawable.peligro_vector
            }

                val bitmapDrawable = ContextCompat.getDrawable(requireContext(), markerImage)
                val bitmap = bitmapDrawable?.toBitmap()
                val resizedBitmap = bitmap?.let { Bitmap.createScaledBitmap(it, 100, 100, false) }

                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .icon(resizedBitmap?.let { BitmapDescriptorFactory.fromBitmap(it) })

                val marker = map.addMarker(markerOptions)
                if (marker != null) {
                    markers.add(marker)
                    guardarMarcadores()
                }

            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }


    private fun guardarMarcadores() {
        val markersCollection = firestore.collection("MarcadoresZonas")
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




}
