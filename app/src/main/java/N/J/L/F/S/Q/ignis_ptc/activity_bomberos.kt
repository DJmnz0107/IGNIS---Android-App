package N.J.L.F.S.Q.ignis_ptc

import Modelo.LocationService
import Modelo.LocationServiceBomberos
import N.J.L.F.S.Q.ignis_ptc.databinding.ActivityBomberosBinding
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView


class activity_bomberos : AppCompatActivity() {

    private lateinit var binding: ActivityBomberosBinding
    private lateinit var locationService: LocationServiceBomberos


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        locationService = LocationServiceBomberos(this@activity_bomberos)
        locationService.checkAndRequestPermissions()
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityBomberosBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navController = findNavController(R.id.nav_host_fragment_container)

        val navView: BottomNavigationView = binding.navBomberos


        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.inicio_Bombero, R.id.informe_Bomberos, R.id.ubicaciones_Bomberos
            )
        )

        navView.setupWithNavController(navController)



    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationService.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}