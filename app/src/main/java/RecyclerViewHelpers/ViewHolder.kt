package RecyclerViewHelpers

import N.J.L.F.S.Q.ignis_ptc.R
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

    val txtNombreDescripcion = view.findViewById<TextView>(R.id.txtNombreDescripcion)
}