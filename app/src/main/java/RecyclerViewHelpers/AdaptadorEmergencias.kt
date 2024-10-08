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

    }





}