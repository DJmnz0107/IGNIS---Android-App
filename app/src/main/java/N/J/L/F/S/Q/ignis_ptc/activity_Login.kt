package N.J.L.F.S.Q.ignis_ptc

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

class activity_Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnLogin = findViewById<Button>(R.id.btnLogin)

        val lblRegistrarse = findViewById<TextView>(R.id.lblRegistrarse)

        lblRegistrarse.setOnClickListener {
            val pantallaRegister = Intent(this,activity_Register::class.java)
            startActivity(pantallaRegister)
        }

        btnLogin.setOnClickListener {
            val randomNumber = Random.nextInt(2)

            if (randomNumber == 0) {
                val pantallaBombero = Intent(this, activity_bomberos::class.java)
                startActivity(pantallaBombero)
            }
            else {
                val pantallaMain = Intent(this, MainActivity::class.java)
                startActivity(pantallaMain)
            }

        }



    }
}