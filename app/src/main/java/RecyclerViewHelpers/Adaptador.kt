package RecyclerViewHelpers

import Modelo.ClaseConexion
import Modelo.dataClassEmergencias
import N.J.L.F.S.Q.ignis_ptc.R
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    fun EliminarDatos (descripcionEmergencias:String,posicion:Int){
        val ListaDatos=Datos.toMutableList()
        ListaDatos.removeAt(posicion)

        try {
            GlobalScope.launch(Dispatchers.IO){
                val objConexion=ClaseConexion().cadenaConexion()

                val deleteEmergencia= objConexion?.prepareStatement("DELETE EMERGENCIAS WHERE descripcion_emergencia = ?")!!
                deleteEmergencia.setString(1, descripcionEmergencias)
                deleteEmergencia.executeUpdate()

                val commit = objConexion.prepareStatement("commit")
                commit.executeUpdate()
            }
            Datos = ListaDatos.toList()
            notifyItemRemoved(posicion)
            notifyDataSetChanged()
        } catch (e:Exception) {
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


        holder.imgBorrar.setOnClickListener {
            val context = holder.itemView.context
            val builder = MaterialAlertDialogBuilder(context)
            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.eliminar_diseno, null)

            val btnSi = dialogView.findViewById<Button>(R.id.btnAceptarEliminacion)
            val btnNo = dialogView.findViewById<Button>(R.id.btnDenegar)


            builder.setView(dialogView)
            val dialog = builder.create()

            btnSi.setOnClickListener {
                EliminarDatos(item.descripcionEmergencia, position)
                dialog.dismiss()
            }

            btnNo.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()

        }



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