package Modelo

import N.J.L.F.S.Q.ignis_ptc.urgenciasIncendio
import N.J.L.F.S.Q.ignis_ptc.urgencias_derrumbes
import N.J.L.F.S.Q.ignis_ptc.urgencias_inundacion
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class fragmentAdaptadorUrgencias(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                urgenciasIncendio()
            }
            1 -> {
                urgencias_derrumbes()
            }
            else -> {
                urgencias_inundacion()
            }
        }
    }
}