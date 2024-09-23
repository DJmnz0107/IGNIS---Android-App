package N.J.L.F.S.Q.ignis_ptc

import Modelo.ClaseConexion
import Modelo.LocationService
import Modelo.LocationServiceBomberos
import Modelo.dataClassEmergencias
import RecyclerViewHelpers.Adaptador
import RecyclerViewHelpers.AdaptadorEmergencias
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Ubicaciones_emergencias.newInstance] factory method to
 * create an instance of this fragment.
 */
class Ubicaciones_emergencias : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var ubicacion: LatLng? = null

    private lateinit var map: GoogleMap
    private var ubicacionActualMaracador: Marker? = null

    private var emergencyMarker: Marker? = null
    private var polyline: Polyline? = null


    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Ubicaciones_emergencias.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Ubicaciones_emergencias().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    //Busca las emergencias basadas en la descripcion de esta
    private fun searchEmergencias(termino: String, adaptadorEmergencias: AdaptadorEmergencias) {
        val emergencias = mutableListOf<dataClassEmergencias>()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val objConexion = ClaseConexion().cadenaConexion()

                val busquedaEmergencia = objConexion?.prepareStatement("SELECT * FROM Emergencias WHERE descripcion_emergencia LIKE ?")

                busquedaEmergencia?.setString(1, "%$termino%")

                val resultSet = busquedaEmergencia?.executeQuery()



                while (resultSet?.next() == true) {
                    val id = resultSet.getInt("id_emergencia")
                    val ubicacion = resultSet.getString("ubicacion_emergencia")
                    val descripcion = resultSet.getString("descripcion_emergencia")
                    val gravedad = resultSet.getString("gravedad_emergencia")
                    val tipo = resultSet.getString("tipo_emergencia")
                    val respuesta = resultSet.getString("respuesta_notificacion")
                    val estado = resultSet.getString("estado_emergencia")

                    val emergencia = dataClassEmergencias(
                        id,
                        ubicacion,
                        descripcion,
                        gravedad,
                        tipo,
                        respuesta,
                        estado
                    )

                    emergencias.add(emergencia)
                }



                withContext(Dispatchers.Main) {
                    adaptadorEmergencias.actualizarLista(emergencias)
                }

            } catch (e: Exception) {
                println("El error es $e")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)


        val mapFragment = childFragmentManager.findFragmentById(R.id.mapEstaciones2) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }


    private fun setupRecyclerView() {
        CoroutineScope(Dispatchers.Main).launch {
            val emergencias = getEmergencias()
            val recyclerView = view?.findViewById<RecyclerView>(R.id.rcvEmergencia)
            val adaptador = Adaptador(emergencias) { emergencia ->
                actualizarUbicacion(emergencia)
            }
            recyclerView?.adapter = adaptador
        }
    }

    //Obtiene todas las emergencias de la base de datos
    private suspend fun getEmergencias(): List<dataClassEmergencias> {
        return withContext(Dispatchers.IO) {
            try {
                val objConexion = ClaseConexion().cadenaConexion()
                val query = """
                SELECT id_emergencia, ubicacion_emergencia, descripcion_emergencia, 
                       gravedad_emergencia, tipo_emergencia, respuesta_notificacion, estado_emergencia 
                FROM Emergencias
            """
                val statement = objConexion?.prepareStatement(query)
                val resultSet = statement?.executeQuery()

                val emergencias = mutableListOf<dataClassEmergencias>()

                while (resultSet?.next() == true) {
                    val id = resultSet.getInt("id_emergencia")
                    val ubicacion = resultSet.getString("ubicacion_emergencia")
                    val descripcion = resultSet.getString("descripcion_emergencia")
                    val gravedad = resultSet.getString("gravedad_emergencia")
                    val tipo = resultSet.getString("tipo_emergencia")
                    val respuesta = resultSet.getString("respuesta_notificacion")
                    val estado = resultSet.getString("estado_emergencia")

                    val emergencia = dataClassEmergencias(
                        id, ubicacion, descripcion, gravedad, tipo, respuesta, estado
                    )
                    emergencias.add(emergencia)
                }

                resultSet?.close()
                statement?.close()
                objConexion?.close()

                emergencias

            } catch (e: Exception) {
                Log.e("MiFragmento", "Error al obtener las emergencias: $e")
                emptyList() // Retorna una lista vacía en caso de error
            }
        }
    }





    //Actualizar la ubicacion en el mapa, mostrando donde esta la emergencia
    private fun actualizarUbicacion(emergencia: dataClassEmergencias) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val objConexion = ClaseConexion().cadenaConexion()
                val query = "SELECT ubicacion_Emergencia FROM Emergencias WHERE id_emergencia = ?"
                val statement = objConexion?.prepareStatement(query)
                statement?.setInt(1, emergencia.id)
                val resultSet = statement?.executeQuery()

                if (resultSet?.next() == true) {
                    val ubicacionEmergencia = resultSet.getString("ubicacion_Emergencia")
                    val parts = ubicacionEmergencia.split(" ") //Parte la ubicación para convertirla en LatLng

                    if (parts.size == 2) {
                        val latitude = parts[0].toDoubleOrNull()
                        val longitude = parts[1].toDoubleOrNull()

                        if (latitude != null && longitude != null) {
                            val coordinates = LatLng(latitude, longitude)

                            withContext(Dispatchers.Main) {
                                if (emergencyMarker == null) {
                                    emergencyMarker = map.addMarker(
                                        MarkerOptions().position(coordinates).title("Ubicación de la emergencia")
                                    )
                                } else {
                                    emergencyMarker?.position = coordinates
                                }

                                if (ubicacionActualMaracador != null) {
                                    // Añadir Polyline entre la ubicación actual y la ubicación de la emergencia
                                    addPolyline(ubicacionActualMaracador!!.position, coordinates)
                                }

                                map.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(coordinates, 18f), 4000, null
                                )
                            }
                        } else {
                            Log.e("Ubicacion", "Error: No se pudo convertir la latitud o longitud a Double.")
                        }
                    } else {
                        Log.e("Ubicacion", "Error: Formato incorrecto de latLngString.")
                    }
                }

                statement?.close()
                objConexion?.close()

            } catch (e: Exception) {
                Log.e("Ubicacion", "Error al obtener la ubicación de la emergencia: $e")
            }
        }
    }


    //Añade una polyline desde la ubicación del bombero hasta la emergencia
    private fun addPolyline(start: LatLng, end: LatLng) {
        // Remueve la polyline existente si ya hay alguna
        polyline?.remove()

        // Añadir una nueva polyline
        polyline = map.addPolyline(
            PolylineOptions()
                .add(start, end)
                .color(ContextCompat.getColor(requireContext(), R.color.naranjaDos)) // Color de la polyline
                .width(10f) // Ancho de la polyline
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_ubicaciones_emergencias, container, false)

        val rcvEmergencias = root.findViewById<RecyclerView>(R.id.rcvEmergencia)
        val btnVolverEmergencias = root.findViewById<ImageView>(R.id.imgvolverEmergencias)

        val viewBuscar = root.findViewById<View>(R.id.vBuscar)

        val imgBuscar = root.findViewById<ImageView>(R.id.imgBuscar)

        viewBuscar.setOnClickListener {

            showBottomSheet()

        }



        btnVolverEmergencias.setOnClickListener {
            findNavController().navigate(R.id.emergenciaAbomberos)
        }
        rcvEmergencias.layoutManager = LinearLayoutManager(context)

        //Obtiene la descripcion de la emergencia para añadirla a la card
        fun obtenerDescripcion(): List<dataClassEmergencias>{

            try {
                val objConexion = ClaseConexion().cadenaConexion()

                val statement = objConexion?.createStatement()
                val resultSet = statement?.executeQuery("SELECT * FROM Emergencias")!!

                val listaEmergencias = mutableListOf<dataClassEmergencias>()

                while (resultSet.next()){
                    val id = resultSet.getInt("id_emergencia")
                    val UbicacionEmergencia = resultSet.getString("ubicacion_emergencia")
                    val descripcionEmergencia = resultSet.getString("descripcion_emergencia")
                    val gravedadEmergencia = resultSet.getString("gravedad_emergencia")
                    val tipoEmergencia = resultSet.getString("tipo_emergencia")
                    val respuestaNotificacion = resultSet.getString("respuesta_notificacion")
                    val estadoEmergencia = resultSet.getString("estado_emergencia")

                    val datosCompletos = dataClassEmergencias(id, UbicacionEmergencia, descripcionEmergencia, gravedadEmergencia, tipoEmergencia, respuestaNotificacion, estadoEmergencia)

                    listaEmergencias.add(datosCompletos)
                }
                return listaEmergencias
            }

            catch (e: Exception){
                println("El error es $e")
                return emptyList()
            }


        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val descripcionDB = obtenerDescripcion() // Obtenemos la lista de emergencias desde la BD

                withContext(Dispatchers.Main) {
                    if (descripcionDB.isNotEmpty()) {
                        val adapter = Adaptador(descripcionDB) { emergencia ->
                            // Aquí colocas lo que deseas hacer cuando se hace clic en un ítem
                            Toast.makeText(context, "Emergencia seleccionada: ${emergencia.descripcionEmergencia}", Toast.LENGTH_SHORT).show()
                        }
                        rcvEmergencias.adapter = adapter
                    } else {
                        Toast.makeText(context, "No se encontraron emergencias", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al obtener las emergencias: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }







        return root



    }






    //Muestra el bottom sheet que contiene el buscar
    private fun showBottomSheet() {
        val context = requireContext()

        val bottomSheetDialog = BottomSheetDialog(context)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_busqueda, null)
        bottomSheetDialog.setContentView(bottomSheetView)


        val buscarResultado = bottomSheetView.findViewById<EditText>(R.id.txtBuscar)
        val imgBuscar = bottomSheetView.findViewById<ImageView>(R.id.imgBuscarEmergencia)
        val  resultadoRcv= bottomSheetView.findViewById<RecyclerView>(R.id.rcvBusqueda)


        resultadoRcv.layoutManager = LinearLayoutManager(context)
        val emergenciaAdapter = AdaptadorEmergencias(emptyList())
        resultadoRcv.adapter = emergenciaAdapter


        imgBuscar.setOnClickListener {
            val termino = buscarResultado.text.toString().trim()
            if (termino.isNotEmpty()) {
                searchEmergencias(termino, emergenciaAdapter)
            } else {
                Toast.makeText(context, "Ingrese una emergencia existente", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetDialog.show()
    }





    //Configura el mapa
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        CoroutineScope(Dispatchers.IO).launch {
            ubicacion = withContext(Dispatchers.IO) {
                LocationServiceBomberos(context as activity_bomberos).ubicacionLatLng(context as activity_bomberos)
            }
            withContext(Dispatchers.Main) {
                mostrarUbicacionActual(ubicacion)
                setupRecyclerView()
            }
        }
    }

    //Muestra la ubicacion actual del bombero
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
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) // Icono visible
            ubicacionActualMaracador = map.addMarker(markerOptions)

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18f))
        } else {
            Log.d("Mapa", "La ubicación es nula")
        }
    }




    private fun createMarker() {

        val coordinates = LatLng(13.722845, -89.205214)

        val marker = MarkerOptions().position(coordinates).title("El ITR")

        map.addMarker(marker)

        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coordinates, 18f), 4000, null
        )

    }

}