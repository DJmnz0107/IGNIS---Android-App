package N.J.L.F.S.Q.ignis_ptc

import N.J.L.F.S.Q.ignis_ptc.databinding.ActivityBomberosBinding
import N.J.L.F.S.Q.ignis_ptc.databinding.ActivityMainBinding
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class activity_bomberos : AppCompatActivity() {

    private lateinit var binding: ActivityBomberosBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bomberos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val navController = findNavController(R.id.nav_host_fragment_container)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.nav_bomberos)

        bottomNavigationView.setupWithNavController(navController)
    }
}