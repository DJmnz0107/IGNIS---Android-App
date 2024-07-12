package N.J.L.F.S.Q.ignis_ptc

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.w3c.dom.Text

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [fragment_seguimiento1.newInstance] factory method to
 * create an instance of this fragment.
 */
class fragment_seguimiento1 : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var map:GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapSeguimiento) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =inflater.inflate(R.layout.fragment_seguimiento1, container, false)

        val mapContainer = root.findViewById<ConstraintLayout>(R.id.mapContainer)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapSeguimiento) as SupportMapFragment?

        val btnSeguimiento = root.findViewById<Button>(R.id.btnSeguimiento)

        val vBackground = root.findViewById<View>(R.id.vBackgorund)

        val vCircular = root.findViewById<View>(R.id.vCircular)

        val lblEstimado = root.findViewById<TextView>(R.id.lblTiempoEstimado)

        val lblTiempo = root.findViewById<TextView>(R.id.lblTiempo)

        val lblMensaje = root.findViewById<TextView>(R.id.lblMensaje)

        val lblUrgencia = root.findViewById<TextView>(R.id.lblUrgencia)

        val imgVolver = root.findViewById<ImageView>(R.id.imgVolverMapa)

        imgVolver.visibility = View.GONE

        val originalLayoutParams = mapFragment?.view?.layoutParams
        val width = originalLayoutParams?.width
        val height = originalLayoutParams?.height



        imgVolver.setOnClickListener {
            originalLayoutParams?.height = height
            originalLayoutParams?.width = width
            mapFragment?.view?.layoutParams = originalLayoutParams


            btnSeguimiento.visibility = View.VISIBLE

            vBackground.visibility = View.VISIBLE

            vCircular.visibility = View.VISIBLE

            lblEstimado.visibility = View.VISIBLE

            lblTiempo.visibility = View.VISIBLE

            lblMensaje.visibility = View.VISIBLE

            lblUrgencia.visibility = View.VISIBLE

            imgVolver.visibility = View.GONE

        }



        btnSeguimiento.setOnClickListener {
            val layoutParams = mapFragment?.view?.layoutParams
            layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
            mapFragment?.view?.layoutParams = layoutParams

            btnSeguimiento.visibility = View.GONE

            vBackground.visibility = View.GONE

            vCircular.visibility = View.GONE

            lblEstimado.visibility = View.GONE

            lblTiempo.visibility = View.GONE

            lblMensaje.visibility = View.GONE

            lblUrgencia.visibility = View.GONE

            imgVolver.visibility = View.VISIBLE



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
         * @return A new instance of fragment fragment_seguimiento1.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            fragment_seguimiento1().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onMapReady(googleMap:GoogleMap) {

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