package RecyclerViewHelpers

import Modelo.ClaseConexion
import Modelo.dataClassEmergencias
import N.J.L.F.S.Q.ignis_ptc.R
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Adaptador (var Datos: List<dataClassEmergencias>): RecyclerView.Adapter<ViewHolder>() {

    fun actualizarEstadoPost(id: Int, nuevoEstado: String) {
        val index = Datos.indexOfFirst { it.id == id }
        Datos[index].estadoEmergencia = nuevoEstado
        notifyDataSetChanged()
        notifyItemRemoved(index)
    }

    fun ActualizarEstado(nuevoEstado: String, id: Int){

        try {
            GlobalScope.launch(Dispatchers.IO) {
                val objConexion = ClaseConexion().cadenaConexion()

                val ActualizarEstado = objConexion?.prepareStatement("update Emergencias set estado_emergencia = ? where id_emergencia = ?")!!
                ActualizarEstado.setString(1, nuevoEstado)
                ActualizarEstado.setInt(2, id)
                ActualizarEstado.executeUpdate()

                val commit = objConexion.prepareStatement("commit")
                commit.executeUpdate()

                withContext(Dispatchers.Main){
                    actualizarEstadoPost(id,nuevoEstado)
                }

            }
        }
        catch (e:Exception){
            println("El error es $e")
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val vista = LayoutInflater.from(parent.context).inflate(R.layout.activity_item_card, parent, false)
        return ViewHolder(vista)
    }

    override fun getItemCount() = Datos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = Datos[position]
        holder.txtNombreDescripcion.text = item.descripcionEmergencia
        holder.txtEstadoDescripcion.text = item.estadoEmergencia





        holder.imgEditar.setOnClickListener {

            val context = holder.itemView.context
            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.dialogemergencias, null)

            val builder = AlertDialog.Builder(context)
            builder.setView(dialogView)

            val txtIngreseEstado = dialogView.findViewById<EditText>(R.id.txtIngreseEstado)
            txtIngreseEstado.hint = item.estadoEmergencia

            val dialog = builder.create()

            dialogView.findViewById<Button>(R.id.btnCancelar).setOnClickListener {
                dialog.dismiss()
            }

            dialogView.findViewById<Button>(R.id.btnActualizar).setOnClickListener {
                ActualizarEstado(txtIngreseEstado.text.toString(), item.id)
                dialog.dismiss()
            }

            dialog.show()
        }

    }





}