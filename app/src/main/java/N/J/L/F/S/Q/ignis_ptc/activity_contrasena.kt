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
import enviarCorreo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class activity_contrasena : AppCompatActivity() {

    companion object numAleatorio {
        var codigoRecu: Int = 0 // Código de recuperación aleatorio
        lateinit var nombreUser: String // Nombre de usuario para recuperación
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Habilita el diseño de borde a borde
        setContentView(R.layout.activity_contrasena) // Establece el layout de la actividad

        // Ajusta el padding del layout principal según los bordes del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa botones y campos de texto
        val btnEnviar = findViewById<Button>(R.id.EnviarCorreo)
        val txtCORREORECU = findViewById<EditText>(R.id.txtCorreoRecu)
        val txtUsuario = findViewById<EditText>(R.id.txtNombreUsuarioRecu)
        val btnRecuperar = findViewById<Button>(R.id.btnRegistrarse)
        val imgVolver = findViewById<ImageView>(R.id.imgVolverLogin)

        // Maneja el clic en la imagen de volver
        imgVolver.setOnClickListener {
            val pantallaLogin = Intent(this, activity_Login::class.java) // Redirige a la pantalla de login
            startActivity(pantallaLogin)
        }

        // Maneja el clic en el botón de recuperar contraseña
        btnRecuperar.setOnClickListener {
            val pantallaRecucontra = Intent(this, activity_recucontra::class.java) // Redirige a la pantalla de recuperación de contraseña
            startActivity(pantallaRecucontra)
        }

        // Maneja el clic en el botón de enviar correo
        btnEnviar.setOnClickListener {
            val correoUser = txtCORREORECU.text.toString().trim() // Obtiene el correo ingresado
            val userrecu = txtUsuario.text.toString() // Obtiene el nombre de usuario ingresado
            var validacion = false // Variable de validación
            nombreUser = userrecu // Asigna el nombre de usuario

            // Regex para validar formato de correo electrónico
            val patronEmail = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")

            // Validación del correo electrónico
            if (!correoUser.matches(patronEmail)) {
                txtCORREORECU.error = "El correo no tiene un formato válido" // Mensaje de error
                validacion = true
            } else {
                txtCORREORECU.error = null // Limpia el error si el formato es válido
            }

            // Si no hay errores de validación
            if (!validacion) {
                try {
                    CoroutineScope(Dispatchers.IO).launch {
                        codigoRecu = (100000..999999).random() // Genera un código de recuperación aleatorio

                        val objConexion = ClaseConexion().cadenaConexion() // Establece la conexión a la base de datos
                        val revisarUsuarioContra =
                            objConexion?.prepareStatement("SELECT * FROM Usuarios WHERE nombre_usuario = ?")!!
                        revisarUsuarioContra.setString(1, userrecu) // Configura la consulta para buscar el usuario
                        val result = revisarUsuarioContra.executeQuery() // Ejecuta la consulta


                        // Si se encuentra el usuario en la base de datos
                        if (result.next()) {
                            if (correoUser.isNotEmpty()) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    // Envía el correo de recuperación
                                    enviarCorreo(
                                        correoUser,
                                        "Recuperacion de CONTRASEÑA",
                                        "Hola usuario ${userrecu}, Te saluda el grupo de ignis tu codigo es: ${codigoRecu}"
                                    )
                                    // Muestra un mensaje de éxito
                                    MotionToast.createColorToast(this@activity_contrasena,
                                        "Envio de correo",
                                        "El correo ha sido enviado, revisa tu bandeja de entrada",
                                        MotionToastStyle.INFO,
                                        MotionToast.GRAVITY_BOTTOM,
                                        MotionToast.LONG_DURATION,
                                        ResourcesCompat.getFont(this@activity_contrasena, R.font.cabin_bold))
                                    // Limpia los campos de texto
                                    txtCORREORECU.setText("")
                                    txtUsuario.setText("")
                                }
                            } else {
                                txtCORREORECU.error = "Introduce un campo valido" // Mensaje de error si el correo está vacío
                            }
                        } else {
                            // Mensaje de error si no se encuentra el usuario
                            withContext(Dispatchers.Main) {
                                MotionToast.createColorToast(this@activity_contrasena,
                                    "Envio de correo",
                                    "Ha habido un error al enviar el correo",
                                    MotionToastStyle.ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(this@activity_contrasena, R.font.cabin_bold))
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("El error es $e") // Imprime el error en caso de excepción
                }
            }
        }
    }
}
