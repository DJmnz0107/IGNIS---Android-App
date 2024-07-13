package N.J.L.F.S.Q.ignis_ptc

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Inicio_Bombero.newInstance] factory method to
 * create an instance of this fragment.
 */
class Inicio_Bombero : Fragment() {
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
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_inicio__bombero, container, false)

        val btnCerrar = root.findViewById<Button>(R.id.btnLogOut)


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val context = requireContext()

                val builder = androidx.appcompat.app.AlertDialog.Builder(context, R.style.CustomAlertDialog)
                val customLayout = layoutInflater.inflate(R.layout.logout_personalizado, null)
                builder.setView(customLayout)

                val positiveButton: Button = customLayout.findViewById(R.id.positiveButton)
                val negativeButton: Button = customLayout.findViewById(R.id.negativeButton)

                val dialog = builder.create()

                positiveButton.setOnClickListener {
                    val pantallaLogin = Intent(requireActivity(), activity_Login::class.java)
                    startActivity(pantallaLogin)
                    requireActivity().finish()
                    dialog.dismiss()
                }

                negativeButton.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
        })

        btnCerrar.setOnClickListener {
            val context = requireContext()

            val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
            val customLayout = layoutInflater.inflate(R.layout.logout_personalizado, null)
            builder.setView(customLayout)

            val positiveButton: Button = customLayout.findViewById(R.id.positiveButton)
            val negativeButton: Button = customLayout.findViewById(R.id.negativeButton)

            val dialog = builder.create()

            positiveButton.setOnClickListener {
                val pantallaLogin = Intent(requireActivity(), activity_Login::class.java)
                startActivity(pantallaLogin)
                requireActivity().finish()
                dialog.dismiss()
            }

            negativeButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
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
         * @return A new instance of fragment Inicio_Bombero.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Inicio_Bombero().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}