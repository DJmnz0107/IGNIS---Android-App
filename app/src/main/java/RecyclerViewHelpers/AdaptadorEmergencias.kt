package RecyclerViewHelpers

import Modelo.ClaseConexion
import Modelo.dataClassEmergencias
import N.J.L.F.S.Q.ignis_ptc.R
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdaptadorEmergencias(var Datos: List<dataClassEmergencias>):RecyclerView.Adapter<ViewHolderEmergencias>() {


    fun actualizarLista(nuevosDatos: List<dataClassEmergencias>) {
        Datos = nuevosDatos
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderEmergencias {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_emergencias, parent, false)
        return ViewHolderEmergencias(view)

    }

    override fun getItemCount(): Int {

        return Datos.size

    }

    override fun onBindViewHolder(holder: ViewHolderEmergencias, position: Int) {

        val emergencia = Datos[position]
        holder.txtDescripcionEmergencia.text = emergencia.descripcionEmergencia

        holder.itemView.setOnClickListener {

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val objConexion = ClaseConexion().cadenaConexion()
                    val query = "SELECT ubicacion_Emergencia FROM Emergencias WHERE id_emergencia = ?"
                    val statement = objConexion?.prepareStatement(query)

                    statement?.setInt(1, emergencia.id)
                    val resultSet = statement?.executeQuery()

                    if (resultSet?.next() == true) {
                        val ubicacionEmergencia = resultSet.getString("ubicacion_Emergencia")


                        withContext(Dispatchers.Main) {
                            Log.d("EmergenciaInfo", "Ubicación de Emergencia: $ubicacionEmergencia")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.d("EmergenciaInfo", "No se encontraron resultados para el id_emergencia: ${emergencia.id}")
                        }
                    }

                    statement?.close()
                    objConexion?.close()

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("EmergenciaInfo", "Error al obtener la ubicación de la emergencia: $e")
                    }
                }
            }
        }
    }





}