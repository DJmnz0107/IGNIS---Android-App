package N.J.L.F.S.Q.ignis_ptc

import Modelo.ClaseConexion
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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



        btnRecuperar.setOnClickListener {

            val pantallaRecucontra = Intent(this,activity_recucontra::class.java)
            startActivity(pantallaRecucontra)
        }

        btnEnviar.setOnClickListener {
              val correoUser = txtCORREORECU.text.toString().trim()
            val userrecu = txtUsuario.text.toString()
             nombreUser = userrecu


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
                                Toast.makeText(this@activity_contrasena, "Correo enviado satisfactoriamente", Toast.LENGTH_SHORT).show();
                                txtCORREORECU.setText("")
                                txtUsuario.setText("")


                            }

                        } else {
                            txtCORREORECU.error = "Introduce un campo valido"

                        }
                    } else {
                        println("Ocurrio un Error")}
                }
            }
            catch (e:Exception){
                println("El error es $e")}
        }




    }



}