package RecyclerViewHelpers

import N.J.L.F.S.Q.ignis_ptc.R
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ViewHolderEmergencias(view: View):RecyclerView.ViewHolder(view) {

    val txtDescripcionEmergencia = view.findViewById<TextView>(R.id.txtdescripcionEmergencia)


}