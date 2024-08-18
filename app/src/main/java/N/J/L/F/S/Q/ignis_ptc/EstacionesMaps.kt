package N.J.L.F.S.Q.ignis_ptc

import Modelo.LocationEventBus
import Modelo.LocationService
import Modelo.ObjPlaces
import N.J.L.F.S.Q.ignis_ptc.databinding.FragmentUbicacionesBomberosBinding
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.helper.widget.Carousel
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EstacionesMaps.newInstance] factory method to
 * create an instance of this fragment.
 */
class EstacionesMaps : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var locationService: LocationService
    private var ubicacion: LatLng? = null
    private lateinit var map:GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        locationService = LocationService(requireActivity() as MainActivity)

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), R.string.google_maps_key.toString())
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapEstaciones) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        LocationEventBus.locationUpdates.observe(viewLifecycleOwner, { location ->
            location?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    buscarEstacionesCercanas(it.latitude, it.longitude)
                }
            }
        })

    }





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_estaciones_maps, container, false)

        val imgBack = root.findViewById<ImageView>(R.id.imgBack)

        val carrusel:ImageCarousel = root.findViewById(R.id.carousel)

        carrusel.registerLifecycle(lifecycle)

        val list = mutableListOf<CarouselItem>()

        list.add(
            CarouselItem(
                imageUrl = "https://www.gobernacion.gob.sv/wp-content/uploads/2021/11/WhatsApp-Image-2021-11-03-at-13.06.33.jpeg"
            )
        )

        list.add(
            CarouselItem(
                imageUrl = "https://pbs.twimg.com/media/FYyz7iJXkAIuDhW.jpg:large"
            )
        )

        list.add(
            CarouselItem(
                imageUrl = "https://lh3.googleusercontent.com/p/AF1QipO9m76LMcIC5L4Rdn7c7e1Rn2EYGRLxq0xnn2F4=s680-w680-h510"
            )
        )

        carrusel.setData(list)




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
         * @return A new instance of fragment EstacionesMaps.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EstacionesMaps().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        locationService.checkAndRequestPermissions()
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


        CoroutineScope(Dispatchers.Main).launch {
            val location = locationService.ubicacionLatLng(requireContext())
            location?.let {
                buscarEstacionesCercanas(it.latitude, it.longitude)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            ubicacion = withContext(Dispatchers.IO) {
                LocationService(context as MainActivity).ubicacionLatLng(context as MainActivity)
            }
            withContext(Dispatchers.Main) {
                mostrarUbicacionActual(ubicacion)
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
            map.addMarker(markerOptions)

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18f))
        }
    }




    private suspend fun buscarEstacionesCercanas(latitude: Double, longitude: Double) {
        val apiKey = getString(R.string.google_maps_key)
        val location = "$latitude,$longitude"
        val radius = 5000
        val type = "fire_station"

        try {
            val response = ObjPlaces.instance.searchNearbyPlaces(
                location = location,
                radius = radius,
                type = type,
                apiKey = apiKey
            )

            if (response.isSuccessful) {
                val places = response.body()?.results
                places?.forEach { place ->
                    val latLng = LatLng(place.geometry.location.lat, place.geometry.location.lng)
                    withContext(Dispatchers.Main) {
                        agregarMarcadorEstacion(place.name, latLng)
                    }
                }
            } else {
                Log.e("PlacesAPI", "Error en la respuesta: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("PlacesAPI", "Excepción al buscar lugares: ${e.message}")
        }
    }



    private fun agregarMarcadorEstacion(name: String, latLng: LatLng) {
        val bitmapDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.estacionubicacion)
        val bitmap = bitmapDrawable?.toBitmap()

        val resizedBitmap =
            bitmap?.let { Bitmap.createScaledBitmap(it, 100, 100, false) }

        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(name)
            .icon(resizedBitmap?.let { BitmapDescriptorFactory.fromBitmap(it) })

        map.addMarker(markerOptions)
    }

    private fun Drawable.toBitmap(): Bitmap? {
        if (this is BitmapDrawable) {
            return this.bitmap
        }
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }
}


