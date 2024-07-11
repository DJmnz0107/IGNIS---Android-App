package N.J.L.F.S.Q.ignis_ptc

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class activity_contrasena : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contrasena)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnEnviar = findViewById<Button>(R.id.btnRecuperarContra)
        val txtCORREORECU = findViewById<EditText>(R.id.txtCorreoRecu)
        val txtUsuario = findViewById<EditText>(R.id.txtNombreUsuarioRecu)
        btnEnviar.setOnClickListener {
              val correoUser = txtCORREORECU.text.toString().trim()
            val userrecu = txtUsuario.text.toString()



            if(){

                if (correoUser.isNotEmpty()){

                    CoroutineScope(Dispatchers.Main).launch{
                        val codigoRecu = (100000..999999).random()
                        enviarCorreo(
                            correoUser,
                            "Recuperacion de CONTRASEÑA",
                            "Hola usuario${userrecu}, Te saluda el grupo de ignis tu codigo es: ${codigoRecu}"
                        )

                    }

                } else{
                    txtCORREORECU.error = "Introduce un campo valido"

                }
            }else()
        }
    }



}