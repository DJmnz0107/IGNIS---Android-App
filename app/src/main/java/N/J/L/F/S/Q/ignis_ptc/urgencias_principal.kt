package N.J.L.F.S.Q.ignis_ptc

import Modelo.fragmentAdaptadorUrgencias
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [urgencias_principal.newInstance] factory method to
 * create an instance of this fragment.
 */
class urgencias_principal : Fragment() {
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
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_urgencias_principal, container, false)

        tabLayout = root.findViewById(R.id.tabLayoutUrgencias)
        viewPager2 = root.findViewById(R.id.viewPager2Urgencias)

        val tabLayout: TabLayout = root.findViewById(R.id.tabLayoutUrgencias)

        val tab1 = tabLayout.newTab().apply {
            customView = layoutInflater.inflate(R.layout.tabitemlayout, null).apply {
                findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.ic_fuego)
            }
        }
        tabLayout.addTab(tab1)

        val tab2 = tabLayout.newTab().apply {
            customView = layoutInflater.inflate(R.layout.tabitemlayout, null).apply {
                findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.ic_derrumbe)
            }
        }
        tabLayout.addTab(tab2)

        val tab3 = tabLayout.newTab().apply {
            customView = layoutInflater.inflate(R.layout.tabitemlayout, null).apply {
                findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.ic_inundacion)
            }
        }
        tabLayout.addTab(tab3)



        viewPager2.adapter = fragmentAdaptadorUrgencias(childFragmentManager, lifecycle)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null){
                    viewPager2.currentItem = tab.position
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })
        viewPager2.adapter = fragmentAdaptadorUrgencias(childFragmentManager, lifecycle)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null){
                    viewPager2.currentItem = tab.position
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.selectTab(tabLayout.getTabAt(position))
                }
            })
        return root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment urgencias_principal.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            urgencias_principal().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}