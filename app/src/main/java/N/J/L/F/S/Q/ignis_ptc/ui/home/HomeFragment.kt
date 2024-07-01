package N.J.L.F.S.Q.ignis_ptc.ui.home

import N.J.L.F.S.Q.ignis_ptc.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import N.J.L.F.S.Q.ignis_ptc.databinding.FragmentHomeBinding
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val btnEmergencia = root.findViewById<Button>(R.id.btnEmergencia)

        btnEmergencia.setOnClickListener {

            showBottomSheet()

        }



        return root
    }

    private fun showBottomSheet() {
        val context = context?: return

        val bottomSheetDialog = BottomSheetDialog(context)

        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_emergencias, null)

        bottomSheetDialog.setContentView(bottomSheetView)

        val behavior = BottomSheetBehavior.from(bottomSheetView.parent as View)

        behavior.isFitToContents = true
        behavior.state = BottomSheetBehavior.STATE_EXPANDED


        val spGravedad = bottomSheetView.findViewById<Spinner>(R.id.spGravedad)

        val listadoGravedad = arrayOf("Baja", "Media", "Alta")

        val AdapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listadoGravedad)

        spGravedad.adapter = AdapterSpinner

        bottomSheetDialog.show()


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}