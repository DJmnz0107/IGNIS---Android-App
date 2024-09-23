package RecyclerViewHelpers

import Modelo.ClaseConexion
import Modelo.dataClassEmergencias
import N.J.L.F.S.Q.ignis_ptc.R
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Adaptador(
    var Datos: List<dataClassEmergencias>,
    private val callback: (dataClassEmergencias) -> Unit
) : RecyclerView.Adapter<Adaptador.ViewHolder>() {




    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombreDescripcion: TextView = itemView.findViewById(R.id.txtNombreDescripcion)
        val txtEstadoDescripcion: TextView = itemView.findViewById(R.id.txtEstadoDescripcion)
        val imgBorrar: ImageView = itemView.findViewById(R.id.imgBorrar)
        val imgEditar: ImageView = itemView.findViewById(R.id.imgEditar)

        fun bind(emergencia: dataClassEmergencias) {
            txtNombreDescripcion.text = emergencia.descripcionEmergencia
            txtEstadoDescripcion.text = emergencia.estadoEmergencia

            itemView.setOnClickListener {
                callback(emergencia) // Pasa solo el objeto al callback
            }

            imgBorrar.setOnClickListener {
                showDeleteDialog(emergencia, adapterPosition)
            }

            imgEditar.setOnClickListener {
                showEditDialog(emergencia)
            }
        }

        private fun showDeleteDialog(emergencia: dataClassEmergencias, position: Int) {
            val context = itemView.context
            val builder = MaterialAlertDialogBuilder(context)
            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.eliminar_diseno, null)

            val btnSi = dialogView.findViewById<Button>(R.id.btnAceptarEliminacion)
            val btnNo = dialogView.findViewById<Button>(R.id.btnDenegar)

            builder.setView(dialogView)
            val dialog = builder.create()

            btnSi.setOnClickListener {
                EliminarDatos(emergencia.descripcionEmergencia, position)
                dialog.dismiss()
            }

            btnNo.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        private fun showEditDialog(emergencia: dataClassEmergencias) {
            val context = itemView.context
            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.dialogemergencias, null)

            val builder = AlertDialog.Builder(context)
            builder.setView(dialogView)

            val txtIngreseEstado = dialogView.findViewById<EditText>(R.id.txtIngreseEstado)
            txtIngreseEstado.hint = emergencia.estadoEmergencia

            val dialog = builder.create()

            dialogView.findViewById<Button>(R.id.btnCancelar).setOnClickListener {
                dialog.dismiss()
            }

            dialogView.findViewById<Button>(R.id.btnActualizar).setOnClickListener {
                ActualizarEstado(txtIngreseEstado.text.toString(), emergencia.id)
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    fun ActualizarEstado(nuevoEstado: String, id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val objConexion = ClaseConexion().cadenaConexion()
                val ActualizarEstado = objConexion?.prepareStatement("update Emergencias set estado_emergencia = ? where id_emergencia = ?")!!
                ActualizarEstado.setString(1, nuevoEstado)
                ActualizarEstado.setInt(2, id)
                ActualizarEstado.executeUpdate()

                val commit = objConexion.prepareStatement("commit")
                commit.executeUpdate()

                withContext(Dispatchers.Main) {
                    actualizarEstadoPost(id, nuevoEstado)
                }

            } catch (e: Exception) {
                println("El error es $e")
            }
        }
    }

    fun actualizarEstadoPost(id: Int, nuevoEstado: String) {
        val index = Datos.indexOfFirst { it.id == id }
        Datos[index].estadoEmergencia = nuevoEstado
        notifyItemChanged(index)
    }

    fun EliminarDatos(descripcionEmergencias: String, posicion: Int) {
        val ListaDatos = Datos.toMutableList()
        ListaDatos.removeAt(posicion)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val objConexion = ClaseConexion().cadenaConexion()
                val deleteEmergencia = objConexion?.prepareStatement("DELETE FROM Emergencias WHERE descripcion_emergencia = ?")!!
                deleteEmergencia.setString(1, descripcionEmergencias)
                deleteEmergencia.executeUpdate()

                val commit = objConexion.prepareStatement("commit")
                commit.executeUpdate()

                withContext(Dispatchers.Main) {
                    Datos = ListaDatos.toList()
                    notifyItemRemoved(posicion)
                    notifyItemRangeChanged(posicion, itemCount)
                }

            } catch (e: Exception) {
                Log.e("Adaptador", "Error al eliminar la emergencia: $e")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.activity_item_card, parent, false)
        return ViewHolder(vista)
    }

    override fun getItemCount() = Datos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = Datos[position]
        holder.bind(item)
    }
}

