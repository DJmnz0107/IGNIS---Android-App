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
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import oracle.security.crypto.core.MessageDigest
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.sql.Connection
import kotlin.random.Random

class activity_Login : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    private lateinit var auth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        val lBlOlvidar = findViewById<TextView>(R.id.lblOlvidarContraseña)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        val txtUsuario = findViewById<EditText>(R.id.txtUsuario)
        val txtPassword = findViewById<EditText>(R.id.txtPassword)

        val imgShow = findViewById<ImageView>(R.id.imgShow)

        val imgHide = findViewById<ImageView>(R.id.imgHide)

        val btnLoginGoogle = findViewById<Button>(R.id.btnLoginGoogle)

        btnLoginGoogle.setOnClickListener {
            signIn()
        }



        lBlOlvidar.setOnClickListener { val pantallacontraseña = Intent(this,activity_contrasena::class.java)
           startActivity(pantallacontraseña)
        }

        //Encriptación de la contraseña
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

            // Validación en caso de los campos estar vacíos
            if (!validacion) {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val contrasenaEncriptada = hashSHA256(txtPassword.text.toString())

                        val objConexion = ClaseConexion().cadenaConexion()
                        val revisarUsuario = objConexion?.prepareStatement(
                            "SELECT id_usuario, id_nivelusuario FROM Usuarios WHERE nombre_usuario = ? AND contrasena_usuario = ?"
                        )
                        revisarUsuario?.setString(1, nombreUsuario)
                        revisarUsuario?.setString(2, contrasenaEncriptada)

                        val resultado = revisarUsuario?.executeQuery()

                        if (resultado != null && resultado.next()) {
                            val idUsuario = resultado.getInt("id_usuario") // Obtener el ID del usuario
                            val nivelUsuario = resultado.getInt("id_nivelusuario") // Obtener el nivel del usuario

                            // Manejo del inicio de sesión basado en el nivel de usuario
                            withContext(Dispatchers.Main) {
                                when (nivelUsuario) {
                                    1 -> {
                                        MotionToast.createColorToast(this@activity_Login,
                                            "Sesión iniciada con éxito",
                                            "Bienvenido a ignis",
                                            MotionToastStyle.SUCCESS,
                                            MotionToast.GRAVITY_BOTTOM,
                                            MotionToast.LONG_DURATION,
                                            ResourcesCompat.getFont(this@activity_Login, R.font.cabin_bold))
                                        // Lógica para otro nivel de usuario

                                        val pantallaUsuarios = Intent(this@activity_Login, MainActivity::class.java)
                                        pantallaUsuarios.putExtra("userId", idUsuario) // Pasar el ID del usuario
                                        startActivity(pantallaUsuarios)
                                        finish()
                                    }
                                    2 -> { // Bombero
                                        MotionToast.createColorToast(this@activity_Login,
                                            "Sesión iniciada con éxito",
                                            "Bienvenido a ignis, bombero",
                                            MotionToastStyle.SUCCESS,
                                            MotionToast.GRAVITY_BOTTOM,
                                            MotionToast.LONG_DURATION,
                                            ResourcesCompat.getFont(this@activity_Login, R.font.cabin_bold))

                                        val pantallaBombero = Intent(this@activity_Login, activity_bomberos::class.java)
                                        pantallaBombero.putExtra("userId", idUsuario) // Pasar el ID del usuario
                                        startActivity(pantallaBombero)
                                        finish()
                                    }
                                    else -> {
                                        // Manejo de otros niveles de usuario si es necesario
                                        MotionToast.createColorToast(this@activity_Login,
                                            "Acceso denegado",
                                            "No tienes permisos para acceder a esta aplicación",
                                            MotionToastStyle.WARNING,
                                            MotionToast.GRAVITY_BOTTOM,
                                            MotionToast.LONG_DURATION,
                                            ResourcesCompat.getFont(this@activity_Login, R.font.cabin_bold))
                                    }
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                MotionToast.createColorToast(this@activity_Login,
                                    "Error al iniciar sesión",
                                    "Las credenciales ingresadas son incorrectas",
                                    MotionToastStyle.ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(this@activity_Login, R.font.cabin_bold))
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




    //Pestaña para seleccionar la cuenta
    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.id_client))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Permite el inicio de sesión con Google
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    MotionToast.createColorToast(this,
                        "Sesión iniciada con éxito",
                        "Bienvenido a ignis, ${user?.displayName}",
                        MotionToastStyle.SUCCESS,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(this,R.font.cabin_bold))
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

}