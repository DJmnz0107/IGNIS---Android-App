package N.J.L.F.S.Q.ignis_ptc

import Modelo.ClaseConexion
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class activity_contrasena : AppCompatActivity() {

    companion object numAleatorio{
         var codigoRecu: Int = 0
        lateinit var nombreUser : String
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contrasena)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnEnviar = findViewById<Button>(R.id.EnviarCorreo)
        val txtCORREORECU = findViewById<EditText>(R.id.txtCorreoRecu)
        val txtUsuario = findViewById<EditText>(R.id.txtNombreUsuarioRecu)
        val btnRecuperar = findViewById<Button>(R.id.btnRegistrarse)

        val imgVolver = findViewById<ImageView>(R.id.imgVolverLogin)

        imgVolver.setOnClickListener {
            val pantallaLogin = Intent(this, activity_Login::class.java)
            startActivity(pantallaLogin)
        }



        btnRecuperar.setOnClickListener {

            val pantallaRecucontra = Intent(this,activity_recucontra::class.java)
            startActivity(pantallaRecucontra)
        }

        btnEnviar.setOnClickListener {
            val correoUser = txtCORREORECU.text.toString().trim()
            val userrecu = txtUsuario.text.toString()
            var validacion = false
            nombreUser = userrecu

            val patronEmail = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")

            if (!correoUser.matches(patronEmail)) {
                txtCORREORECU.error = "El correo no tiene un formato válido"
                validacion = true
            } else {
                txtCORREORECU.error = null
            }

            if(!validacion) {
                try {
                    CoroutineScope(Dispatchers.IO).launch {

                        codigoRecu = (100000..999999).random()

                        val objConexion = ClaseConexion().cadenaConexion()
                        val revisarUsuarioContra =
                            objConexion?.prepareStatement("SELECT * FROM Usuarios WHERE nombre_usuario = ?")!!
                        revisarUsuarioContra.setString(1, userrecu)
                        val result = revisarUsuarioContra.executeQuery()
                        println("El usuario es:$userrecu")
                        println("El correo es: $correoUser")
                        if (result.next()) {

                            if (correoUser.isNotEmpty()) {

                                CoroutineScope(Dispatchers.Main).launch {

                                    enviarCorreo(
                                        correoUser,
                                        "Recuperacion de CONTRASEÑA",
                                        "Hola usuario ${userrecu}, Te saluda el grupo de ignis tu codigo es: ${codigoRecu}"
                                    )
                                    MotionToast.createColorToast(this@activity_contrasena,
                                        "Envio de correo",
                                        "El correo ha sido enviado, revisa tu bandeja de entrada",
                                        MotionToastStyle.INFO,
                                        MotionToast.GRAVITY_BOTTOM,
                                        MotionToast.LONG_DURATION,
                                        ResourcesCompat.getFont(this@activity_contrasena,R.font.cabin_bold))
                                    txtCORREORECU.setText("")
                                    txtUsuario.setText("")


                                }

                            } else {
                                txtCORREORECU.error = "Introduce un campo valido"

                            }
                        } else {
                           withContext(Dispatchers.Main) {
                               MotionToast.createColorToast(this@activity_contrasena,
                                   "Envio de correo",
                                   "Ha habido un error al enviar el correo",
                                   MotionToastStyle.ERROR,
                                   MotionToast.GRAVITY_BOTTOM,
                                   MotionToast.LONG_DURATION,
                                   ResourcesCompat.getFont(this@activity_contrasena,R.font.cabin_bold))
                           }
                        }
                    }
                }
                catch (e:Exception){
                    println("El error es $e")}
            }
        }




    }



}