package N.J.L.F.S.Q.ignis_ptc

import Modelo.ClaseConexion
import Modelo.dataClassMisiones
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Informe_Bomberos.newInstance] factory method to
 * create an instance of this fragment.
 */
class Informe_Bomberos : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val root = inflater.inflate(R.layout.fragment_informe__bomberos, container, false)


        val listadoDeResultados = arrayOf("Exitosa", "Fallida")

        val adaptadorResultado = ArrayAdapter (requireContext(),android.R.layout.simple_spinner_dropdown_item, listadoDeResultados)

        val spResultados: Spinner = root.findViewById(R.id.spResultados)

        spResultados.adapter = adaptadorResultado

        val spMision = root.findViewById<Spinner>(R.id.spMision)

        fun obtenerMisiones(idBombero: Int?): List<dataClassMisiones> {
            try {
                val objConexion = ClaseConexion().cadenaConexion()
                val statement = objConexion?.createStatement()

                // Consulta para obtener misiones según el id_bombero
                val query = """
            SELECT m.id_mision, m.descripcion_mision, m.fecha_mision, m.id_emergencia 
            FROM Misiones m
            JOIN Misiones_Bomberos mb ON m.id_mision = mb.id_mision
            WHERE mb.id_bombero = ?
        """

                val preparedStatement = objConexion?.prepareStatement(query)

                // Verificar si idBombero es nulo
                if (idBombero != null) {
                    preparedStatement?.setInt(1, idBombero) // Establecer el id_bombero en la consulta
                } else {
                    println("El id_bombero es nulo.")
                    return emptyList() // O maneja el caso nulo de la forma que prefieras
                }

                val resultSet = preparedStatement?.executeQuery()!!

                val listadoDeResultados = mutableListOf<dataClassMisiones>()

                while (resultSet.next()) {
                    val idMision = resultSet.getInt("id_mision")
                    val descripcionMision = resultSet.getString("descripcion_mision")
                    val fechaMision = resultSet.getDate("fecha_mision")
                    val idEmergencia = resultSet.getInt("id_emergencia")

                    val misionesCompletas = dataClassMisiones(idMision, descripcionMision, fechaMision, idEmergencia)

                    listadoDeResultados.add(misionesCompletas)
                }
                return listadoDeResultados
            } catch (e: Exception) {
                println("El error es $e")
                return emptyList()
            }
        }



        try {
            CoroutineScope(Dispatchers.IO).launch {
                val listadoResultados = obtenerMisiones(activity_bomberos.datosBombero.idBombero)
                val descripcionMision = listadoResultados.map { it.descripcionMision }
                withContext(Dispatchers.Main){
                    val miAdaptador = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, descripcionMision )
                    spMision.adapter = miAdaptador
                }
            }
        }catch (e:Exception){
            println("El error es $e")
        }



        val enviarInforme = root.findViewById<Button>(R.id.btnEnviarInforme)

        val textoInforme = root.findViewById<TextView>(R.id.mtInforme)

        var validacion = false

        val mtInforme = textoInforme.text.toString()

        enviarInforme.setOnClickListener {
            val mtInforme = textoInforme.text.toString()

            if (mtInforme.isEmpty()) {
                textoInforme.error = "Descripción obligatoria"
                return@setOnClickListener // Salir del evento click si la validación falla
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val objConexion = ClaseConexion().cadenaConexion()

                    // Obtener las misiones del bombero y seleccionar la misión correspondiente
                    val mision = obtenerMisiones(activity_bomberos.datosBombero.idBombero)
                    val misionSeleccionada = mision[spMision.selectedItemPosition]

                    // Obtener el ID de la emergencia asociada a la misión
                    val idEmergencia = misionSeleccionada.idEmergencia

                    // Insertar el informe
                    val agregarInforme = objConexion?.prepareStatement("INSERT INTO Informes(id_mision, resultado_mision, descripcion_mision) VALUES(?, ?, ?)")!!
                    agregarInforme.setInt(1, misionSeleccionada.idMision)
                    agregarInforme.setString(2, spResultados.selectedItem.toString())
                    agregarInforme.setString(3, mtInforme)
                    agregarInforme.executeUpdate()

                    // Actualizar el estado de la emergencia a 'finalizada'
                    val actualizarEmergencia = objConexion.prepareStatement("UPDATE Emergencias SET estado_emergencia = 'Finalizada' WHERE id_emergencia = ?")
                    actualizarEmergencia.setInt(1, idEmergencia)
                    actualizarEmergencia.executeUpdate()

                    objConexion.commit()


                    // Mostrar mensaje de éxito
                    withContext(Dispatchers.Main) {
                        textoInforme.setText("")
                        MotionToast.createColorToast(requireContext() as activity_bomberos,
                            "Emergencia enviada",
                            "Datos enviados correctamente",
                            MotionToastStyle.SUCCESS,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(requireContext() as activity_bomberos,R.font.cabin_bold))
                    }
                } catch (e: Exception) {
                    Log.e("InformeError", "Error al agregar el informe: $e")
                }
            }
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
         * @return A new instance of fragment Informe_Bomberos.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Informe_Bomberos().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}