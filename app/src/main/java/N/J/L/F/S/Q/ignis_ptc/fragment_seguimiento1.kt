package N.J.L.F.S.Q.ignis_ptc

import Modelo.LocationService
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable;
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.IOException
import java.util.concurrent.TimeUnit


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [fragment_seguimiento1.newInstance] factory method to
 * create an instance of this fragment.
 */
class fragment_seguimiento1 : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var map:GoogleMap
    private lateinit var truckMarker: Marker
    private lateinit var firestore: FirebaseFirestore
    private var marker: Marker? = null
    private var listenerRegistration: ListenerRegistration? = null






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }




    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapSeguimiento) as SupportMapFragment?
        mapFragment?.getMapAsync(this)



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =inflater.inflate(R.layout.fragment_seguimiento1, container, false)

        val mapContainer = root.findViewById<ConstraintLayout>(R.id.mapContainer)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapSeguimiento) as SupportMapFragment?

        val btnSeguimiento = root.findViewById<Button>(R.id.btnSeguimiento)

        val vBackground = root.findViewById<View>(R.id.vBackgorund)

        val vCircular = root.findViewById<View>(R.id.vCircular)

        val lblEstimado = root.findViewById<TextView>(R.id.lblTiempoEstimado)

        val lblTiempo = root.findViewById<TextView>(R.id.lblTiempo)

        val lblMensaje = root.findViewById<TextView>(R.id.lblMensaje)

        val lblUrgencia = root.findViewById<TextView>(R.id.lblUrgencia)

        val imgVolver = root.findViewById<ImageView>(R.id.imgVolverMapa)

        val animationView: LottieAnimationView = root.findViewById(R.id.lottieAnimationView)

        animationView.repeatCount = LottieDrawable.INFINITE

        animationView.playAnimation()

        imgVolver.visibility = View.GONE

        val originalLayoutParams = mapFragment?.view?.layoutParams
        val width = originalLayoutParams?.width
        val height = originalLayoutParams?.height



        imgVolver.setOnClickListener {
            originalLayoutParams?.height = height
            originalLayoutParams?.width = width
            mapFragment?.view?.layoutParams = originalLayoutParams


            btnSeguimiento.visibility = View.VISIBLE

            vBackground.visibility = View.VISIBLE

            vCircular.visibility = View.VISIBLE

            lblEstimado.visibility = View.VISIBLE

            lblTiempo.visibility = View.VISIBLE

            lblMensaje.visibility = View.VISIBLE

            lblUrgencia.visibility = View.VISIBLE

            animationView.visibility = View.VISIBLE

            imgVolver.visibility = View.GONE

        }



        btnSeguimiento.setOnClickListener {
            val layoutParams = mapFragment?.view?.layoutParams
            layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
            mapFragment?.view?.layoutParams = layoutParams

            btnSeguimiento.visibility = View.GONE

            vBackground.visibility = View.GONE

            vCircular.visibility = View.GONE

            lblEstimado.visibility = View.GONE

            lblTiempo.visibility = View.GONE

            lblMensaje.visibility = View.GONE

            lblUrgencia.visibility = View.GONE

            animationView.visibility = View.GONE

            imgVolver.visibility = View.VISIBLE



        }

        firestore = FirebaseFirestore.getInstance()





        return root

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment fragment_seguimiento1.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            fragment_seguimiento1().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    //Inicia el tracking para el camion de bomberos
    fun iniciarTracking(origin: LatLng, destination: LatLng?) {
        val apiKey = "AIzaSyCH9y8qPHiq4nT-te8GE4BYvKNFshV5ZG0"

        if (destination != null) {
            obtenerRuta(origin, destination, apiKey) { eta, route ->
                activity?.runOnUiThread {
                    val color = ContextCompat.getColor(requireContext(), R.color.naranjaDos)


                    map.addPolyline(PolylineOptions().addAll(route).color(color).width(20f).geodesic(true))

                    val lblTiempoEstimado = view?.findViewById<TextView>(R.id.lblTiempo)

                    lblTiempoEstimado?.text = "$eta"

                }
            }
        }
    }

    //Actualiza la ubicacion del camión de bomberos
    fun actualizarLocalizacion(truckId: String, latitude: Double, longitude: Double) {
        val firetruckRef = firestore.collection("CamionesBomberos").document(truckId)

        val updates = hashMapOf<String, Any>(
            "Latitud" to latitude,
            "Longitud" to longitude
        )

        firetruckRef.update(updates)
            .addOnSuccessListener {
                Log.d("Firestore", "DocumentSnapshot successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating document", e)
            }
    }


    //Verifica la ubicación del camión de bomberos
    private fun listenForTruckLocation() {
        listenerRegistration = firestore.collection("CamionesBomberos")
            .document("CamionesBomberosPrueba")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val resizedIcon = BitmapDescriptorFactory.fromBitmap(
                    reajustarTamanoCamion(R.drawable.camionbombero, 200, 200)
                )

                if (snapshot != null && snapshot.exists()) {
                    val latitude = snapshot.getDouble("Latitud") ?: 0.0
                    val longitude = snapshot.getDouble("Longitud") ?: 0.0
                    val truckLatLng = LatLng(37.42527591502073, -122.07760973154546)

                    if (marker != null) {
                        marker!!.position = truckLatLng
                    } else {
                        marker = map.addMarker(
                            MarkerOptions()
                                .position(truckLatLng)
                                .title("Camión de Bomberos")
                                .icon(resizedIcon)
                        )
                    }

                }
            }
    }


    //Configura las opciones a mostrar dentro del mapa
    override fun onMapReady(googleMap:GoogleMap) {

        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true



        val truckLatLng = LatLng(37.42527591502073, -122.07760973154546)


        CoroutineScope(Dispatchers.IO).launch {
            val ubicacion = withContext(Dispatchers.IO) {
                LocationService(context as MainActivity).ubicacionLatLng(context as MainActivity)
            }
            withContext(Dispatchers.Main) {
                iniciarTracking(truckLatLng, ubicacion)
                mostrarUbicacionActual(ubicacion)
                listenForTruckLocation()

            }
        }



    }

    //Muestra la ubicación actual con un marcador
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
             map.addMarker(markerOptions)

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18f))
        }
    }



    //Obtiene la ruta desde firebase
    private fun obtenerRuta(origin: LatLng, destination: LatLng, apiKey: String, callback: (String, List<LatLng>) -> Unit) {
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&key=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val json = responseBody.string()
                    val gson = Gson()
                    val jsonObject = gson.fromJson(json, JsonObject::class.java)
                    val routes = jsonObject.getAsJsonArray("routes")
                    if (routes.size() > 0) {
                        val route = routes[0].asJsonObject
                        val overviewPolyline = route.getAsJsonObject("overview_polyline")
                        val points = overviewPolyline.get("points").asString
                        val decodedPath = decodePoly(points)

                        val legs = route.getAsJsonArray("legs")
                        val duration = legs[0].asJsonObject.getAsJsonObject("duration").get("text").asString

                        callback(duration, decodedPath)
                    }
                }
            }
        })
    }

    //Reajusta el tamaño de la imagen
    private fun reajustarTamanoCamion(drawableId: Int, width: Int, height: Int): Bitmap {
        val imageBitmap = BitmapFactory.decodeResource(resources, drawableId)
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false)
    }




    //Crea una polyline personalizada
    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }

        return poly
    }
}






