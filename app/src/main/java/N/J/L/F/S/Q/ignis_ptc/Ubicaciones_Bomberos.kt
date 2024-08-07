package N.J.L.F.S.Q.ignis_ptc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Ubicaciones_Bomberos.newInstance] factory method to
 * create an instance of this fragment.
 */
class Ubicaciones_Bomberos : Fragment() {
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

        val root = inflater.inflate(R.layout.fragment_ubicaciones__bomberos, container, false)

        val btnHidrantesBomberos = root.findViewById<Button>(R.id.btnHidrantesBomberos)

        btnHidrantesBomberos.setOnClickListener {
            findNavController().navigate(R.id.haciaHidrantesBomberos)
        }

        val btnUbiEmergencia = root.findViewById<Button>(R.id.btnEmergenciaBombero)

        btnUbiEmergencia.setOnClickListener {
            findNavController().navigate(R.id.haciaEmergencias)
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
         * @return A new instance of fragment Ubicaciones_Bomberos.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Ubicaciones_Bomberos().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}