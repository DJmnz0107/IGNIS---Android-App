package N.J.L.F.S.Q.ignis_ptc

import Modelo.LocationService
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import N.J.L.F.S.Q.ignis_ptc.databinding.ActivityMainBinding
import android.content.Intent
import android.view.Window
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationService: LocationService



    override fun onCreate(savedInstanceState: Bundle?) {







        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        locationService = LocationService(this)
        locationService.checkAndRequestPermissions()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.fragment_seguimiento, R.id.urgencias_principal
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
        locationService.onRequestPermissionsResult(requestCode, permissions, grantResults) //Pide los permisos de ubicación en la activity
    }

    private fun showToast(message:String) {

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}
