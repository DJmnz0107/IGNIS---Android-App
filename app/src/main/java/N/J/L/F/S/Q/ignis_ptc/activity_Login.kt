package N.J.L.F.S.Q.ignis_ptc

import Modelo.ClaseConexion
import Modelo.Usuarios
import Modelo.dataClassUsuarios
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.GoogleMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import oracle.security.crypto.core.MessageDigest
import java.sql.Connection
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

      val lBlOlvidar = findViewById<TextView>(R.id.lblOlvidarContraseña)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        val txtUsuario = findViewById<EditText>(R.id.txtUsuario)
        val txtPassword = findViewById<EditText>(R.id.txtPassword)

        val imgShow = findViewById<ImageView>(R.id.imgShow)

        val imgHide = findViewById<ImageView>(R.id.imgHide)

        lBlOlvidar.setOnClickListener { val pantallacontraseña = Intent(this,activity_contrasena::class.java)
           startActivity(pantallacontraseña)
        }

        fun hashSHA256(password: String): String {
            val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }

        imgHide.visibility = View.GONE
        imgShow.visibility = View.VISIBLE
        txtPassword.transformationMethod = PasswordTransformationMethod.getInstance()

        imgShow.setOnClickListener {
            txtPassword.transformationMethod = null
            imgShow.visibility = View.GONE
            imgHide.visibility = View.VISIBLE
        }

        imgHide.setOnClickListener {
            txtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            imgShow.visibility = View.VISIBLE
            imgHide.visibility = View.GONE
        }

        val lblRegistrarse = findViewById<TextView>(R.id.lblRegistrarse)

        lblRegistrarse.setOnClickListener {
            val pantallaRegister = Intent(this,activity_Register::class.java)
            startActivity(pantallaRegister)

        }
        btnLogin.setOnClickListener {
            val nombreUsuario = txtUsuario.text.toString()
            val password = txtPassword.text.toString()

            var validacion = false

            if (txtUsuario.text.isEmpty()) {
                txtUsuario.error = "Usuario requerido"
                validacion = true
            } else {
                txtUsuario.error = null
            }

            if (txtPassword.text.isEmpty()) {
                txtPassword.error = "Contraseña requerida"
                validacion = true
            } else {
                txtPassword.error = null
            }

            if (!validacion) {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val Usuarios = Usuarios()

                        val contrasenaEncriptada = hashSHA256(txtPassword.text.toString())

                        val nivelUsuario = Usuarios.obtenerNivelUsuario(nombreUsuario, contrasenaEncriptada)

                        val nivelUsuario2 = Usuarios.obtenerNivelUsuario(nombreUsuario, password)


                        println("El nivel de usuario es $nivelUsuario")

                        println("El nivel de usuario es $nivelUsuario2")


                        val objConexion = ClaseConexion().cadenaConexion()

                        val revisarUsuario = objConexion?.prepareStatement("SELECT * FROM Usuarios WHERE nombre_usuario = ? AND contrasena_usuario = ?")!!

                       if(nivelUsuario == 1) {
                           revisarUsuario.setString(1, nombreUsuario)
                           revisarUsuario.setString(2, contrasenaEncriptada)
                       } else if(nivelUsuario2 == 2) {
                           revisarUsuario.setString(1, nombreUsuario)
                           revisarUsuario.setString(2, password)
                       } else {
                           revisarUsuario.setString(1, nombreUsuario)
                           revisarUsuario.setString(2, contrasenaEncriptada)
                       }


                        val resultado = revisarUsuario.executeQuery()

                        if (resultado.next()) {
                            if(nivelUsuario != null || nivelUsuario2 != null) {
                                if (nivelUsuario == 1) {
                                    val pantallaMain = Intent(this@activity_Login, MainActivity::class.java)
                                    startActivity(pantallaMain)
                                }else if (nivelUsuario2 == 2) {
                                    val pantallaBombero = Intent(this@activity_Login, activity_bomberos::class.java)
                                    startActivity(pantallaBombero)
                                }
                                else {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(this@activity_Login, "Revisar si existe algun registro en la base de datos", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                        else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@activity_Login, "Usuario o contraseña incorrectos", Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        println("El error es: ${e.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@activity_Login, "Ha ocurrido un error al intentar iniciar sesión", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }















    }

}