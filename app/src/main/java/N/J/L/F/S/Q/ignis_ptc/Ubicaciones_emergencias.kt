package N.J.L.F.S.Q.ignis_ptc

import Modelo.ClaseConexion
import Modelo.dataClassEmergencias
import RecyclerViewHelpers.Adaptador
import RecyclerViewHelpers.AdaptadorEmergencias
import android.os.Bundle
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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

    private lateinit var map: GoogleMap

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

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapEstaciones2) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

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

        CoroutineScope(Dispatchers.IO).launch {
            val descripcionDB = obtenerDescripcion()
            withContext(Dispatchers.Main){
                val adapter = Adaptador(descripcionDB)
                rcvEmergencias.adapter = adapter
            }
        }



        return root



    }



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





    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        createMarker()
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