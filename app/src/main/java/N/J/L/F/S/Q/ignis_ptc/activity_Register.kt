package N.J.L.F.S.Q.ignis_ptc

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class activity_Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val txtUsuario = findViewById<EditText>(R.id.txtUserRegister)
        val txtPassword = findViewById<EditText>(R.id.txtPasswordRegister)
        val txtEdad = findViewById<EditText>(R.id.txtEdadRegister)
        val txtDUI = findViewById<EditText>(R.id.txtDuiRegister)
        val btnRegistrarse = findViewById<Button>(R.id.btnRegistrarse)

        val imgShow = findViewById<ImageView>(R.id.imgShow1)

        val imgHide = findViewById<ImageView>(R.id.imgHide1)

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


        //validacion de campos
        btnRegistrarse.setOnClickListener {
            val nombre = txtUsuario.text.toString()
            val password = txtPassword.text.toString()
            val edad = txtEdad.text.toString()
            val dui = txtDUI.text.toString()

            var validacion = false

            if (nombre.isEmpty()) {
                txtUsuario.error = "Nombre obligatorio"
                validacion = true
            } else {
                txtUsuario.error = null
            }

            if (password.isEmpty()) {
                txtPassword.error = "Contraseña obligatoria"
                validacion = true
            } else {
                txtPassword.error = null
            }

            if (edad.isEmpty()) {
                txtEdad.error = "Edad obligatoria"
                validacion = true
            } else {
                txtEdad.error = null
            }

            if (!edad.matches(Regex("[0-9]+"))) {
                txtEdad.error = "La edad debe contener solo números"
                validacion = true
            } else {
                txtEdad.error = null
            }

            if (!password.matches(Regex("^(?=.*[A-Z])(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}$"))) {
                txtPassword.error = "La contraseña debe tener al menos 8 caracteres, una letra mayúscula y un símbolo especial"
                validacion = true
            } else {
                txtPassword.error = null
            }

            if (!password.matches(Regex("[a-zA-Z]+"))) {

            }
        }



    }
}