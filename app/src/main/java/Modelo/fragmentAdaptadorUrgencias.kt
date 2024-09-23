package Modelo

import N.J.L.F.S.Q.ignis_ptc.urgenciasIncendio
import N.J.L.F.S.Q.ignis_ptc.urgencias_derrumbes
import N.J.L.F.S.Q.ignis_ptc.urgencias_inundacion
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class fragmentAdaptadorUrgencias(
    fragmentManager: FragmentManager,  // Gestor de fragmentos que maneja la transacción de fragmentos.
    lifecycle: Lifecycle  // Ciclo de vida del fragmento para manejar su estado.
) : FragmentStateAdapter(fragmentManager, lifecycle) {  // Extiende FragmentStateAdapter para manejar múltiples fragmentos.

    // Devuelve el número total de fragmentos que se mostrarán.
    override fun getItemCount(): Int {
        return 3  // En este caso, hay 3 fragmentos que se gestionan.
    }

    // Crea un nuevo fragmento en función de la posición del adaptador.
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                urgenciasIncendio()  // Retorna el fragmento para urgencias de incendio.
            }
            1 -> {
                urgencias_derrumbes()  // Retorna el fragmento para urgencias de derrumbes.
            }
            else -> {
                urgencias_inundacion()  // Retorna el fragmento para urgencias de inundaciones.
            }
        }
    }
}
