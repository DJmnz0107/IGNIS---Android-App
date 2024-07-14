package N.J.L.F.S.Q.ignis_ptc

import Modelo.ClaseConexion
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import oracle.security.crypto.core.MessageDigest

class activity_recucontra : AppCompatActivity() {

    fun hashSHA256(password: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recucontra)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }





        val txtcodigo = findViewById<EditText>(R.id.txtCodigoRecu)

        val codigoColocado = txtcodigo.text.toString().toInt()

        val btnCodigoreco = findViewById<Button>(R.id.btnCambiarContra)

        btnCodigoreco.setOnClickListener {



            if (codigoColocado == activity_contrasena.codigoRecu){
              mostrarDialogoActualizarContrasena()

            }
            else{Toast.makeText(this@activity_recucontra,"Codigo Incorrecto",Toast.LENGTH_LONG).show()}

        }
    }

    private fun mostrarDialogoActualizarContrasena() {
        val builder = MaterialAlertDialogBuilder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.actucontra, null)
        val etNewPassword = dialogLayout.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogLayout.findViewById<TextInputEditText>(R.id.etConfirmPassword)

        builder.setView(dialogLayout)
            .setTitle("Actualizar Contraseña")
            .setPositiveButton("Actualizar", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    val newPassword = etNewPassword.text.toString()
                    val contraEncriptada = hashSHA256(newPassword)
                    val confirmPassword = etConfirmPassword.text.toString()

                    if (newPassword == confirmPassword) {

                        actualizarContrasena(contraEncriptada)
                    } else {

                        etConfirmPassword.error = "Las contraseñas no coinciden"
                    }
                    dialog?.dismiss()
                }
            })
            .setNegativeButton("Cancelar", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.dismiss()
                }
            })
            .create()
            .show()
    }

    private fun actualizarContrasena(nuevaContrasena: String) {
       try {
          val nombreUsuario = activity_contrasena.nombreUser
           CoroutineScope(Dispatchers.IO).launch {

               val objConexion = ClaseConexion().cadenaConexion()

               val UpdateContra = objConexion?.prepareStatement("UPDATE Usuarios SET contrasena_usuario = ? WHERE nombre_usuario = ?")!!


               UpdateContra.setString(1,nuevaContrasena)
               UpdateContra.setString(2,nombreUsuario)
               UpdateContra.executeUpdate()

               withContext(Dispatchers.Main){

                   Toast.makeText(this@activity_recucontra,"Contraseña acutalizada con exito",Toast.LENGTH_LONG).show()
               }
           }
       }catch (e:Exception){
           println("El error fue ${e}")}
    }


}