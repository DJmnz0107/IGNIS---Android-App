package N.J.L.F.S.Q.ignis_ptc

import Modelo.ClaseConexion
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        val btnCodigoreco = findViewById<Button>(R.id.btnCambiarContra)

        btnCodigoreco.setOnClickListener {
            val codigoText = txtcodigo.text.toString().trim()

            if (codigoText.isEmpty()) {
                Toast.makeText(this@activity_recucontra, "Por favor ingrese el código", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            try {
                val codigoColocado = codigoText.toInt()
                if (codigoColocado == activity_contrasena.codigoRecu) {
                    ActualizarContra()
                } else {
                    Toast.makeText(this@activity_recucontra, "Código Incorrecto", Toast.LENGTH_LONG).show()
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(this@activity_recucontra, "Código no válido", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun ActualizarContra() {
        val builder = MaterialAlertDialogBuilder(this)
        val inflater = layoutInflater
        val disenoDialogo = inflater.inflate(R.layout.actucontra, null)

        val txtNueva = disenoDialogo.findViewById<EditText>(R.id.txtNuevaContra)
        val txtConfirmar = disenoDialogo.findViewById<EditText>(R.id.txtConfirmar)
        val imgVerNuevaContra = disenoDialogo.findViewById<ImageView>(R.id.imgVerContra)
        val imgVerConfirmar = disenoDialogo.findViewById<ImageView>(R.id.imgVerConfirmar)

        builder.setView(disenoDialogo)
            .setPositiveButton("Actualizar", null)
            .setNegativeButton("Cancelar", null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            val alertTitleId = resources.getIdentifier("alertTitle", "id", "android")
            val alertTitle = dialog.findViewById<TextView>(alertTitleId)
            alertTitle?.setBackgroundColor(Color.parseColor("#F2844C"))
            alertTitle?.setTextColor(Color.WHITE)

            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setBackgroundColor(Color.parseColor("#F2844C"))
            positiveButton.setTextColor(Color.WHITE)
            positiveButton.setOnClickListener {
                val nuevaContra = txtNueva.text.toString()
                val confirmarContra = txtConfirmar.text.toString()

                var validacion = true

                if (nuevaContra.isEmpty()) {
                    txtNueva.error = "Debe colocar una nueva contraseña válida"
                    validacion = false
                } else if(!nuevaContra.matches(Regex("^(?=.*[A-Z])(?=.*[!@#\$%^&*(),.?_\":{}|<>]).{8,}$"))) {
                    txtNueva.error = "La contraseña debe tener al menos 8 caracteres, una letra mayúscula y un símbolo especial"
                    validacion = false
                } else {
                    txtNueva.error = null
                }

                if (confirmarContra.isEmpty()) {
                    txtConfirmar.error = "Debe confirmar su contraseña"
                    validacion = false
                }

                if (validacion) {
                    if (nuevaContra == confirmarContra) {
                        val contraEncriptada = hashSHA256(nuevaContra)
                        actualizarContrasena(contraEncriptada)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this@activity_recucontra, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show()
                    }
                }
            }

            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setBackgroundColor(Color.parseColor("#F2844C"))
            negativeButton.setTextColor(Color.WHITE)
            negativeButton.setOnClickListener {
                dialog.dismiss()
            }

            imgVerNuevaContra.setOnClickListener {
                verYOcultarContrasena(txtNueva, imgVerNuevaContra)
            }

            imgVerConfirmar.setOnClickListener {
                verYOcultarContrasena(txtConfirmar, imgVerConfirmar)
            }
        }

        dialog.show()
    }

    fun verYOcultarContrasena(editText: EditText, imageView: ImageView) {
        val isVisible = editText.transformationMethod != PasswordTransformationMethod.getInstance()

        if (isVisible) {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            imageView.setImageResource(R.drawable.eye_show)
        } else {
            editText.transformationMethod = null
            imageView.setImageResource(R.drawable.eye_hide)
        }

        imageView.postDelayed({ imageView.jumpDrawablesToCurrentState() }, 200)

        editText.setSelection(editText.text.length)
        editText.requestFocus()
        imageView.requestFocus()
    }
    private fun actualizarContrasena(nuevaContrasena: String) {
        try {
            val nombreUsuario = activity_contrasena.nombreUser
            CoroutineScope(Dispatchers.IO).launch {
                val objConexion = ClaseConexion().cadenaConexion()
                val updateContra = objConexion?.prepareStatement("UPDATE Usuarios SET contrasena_usuario = ? WHERE nombre_usuario = ?")!!
                updateContra.setString(1, nuevaContrasena)
                updateContra.setString(2, nombreUsuario)
                updateContra.executeUpdate()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@activity_recucontra, "Contraseña actualizada con éxito", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            println("El error fue $e")
        }
    }
}
