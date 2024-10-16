package N.J.L.F.S.Q.ignis_ptc.ui.home

import Modelo.ClaseConexion
import Modelo.LocationService
import Modelo.LocationServiceUrgencia
import N.J.L.F.S.Q.ignis_ptc.MainActivity
import N.J.L.F.S.Q.ignis_ptc.R
import N.J.L.F.S.Q.ignis_ptc.activity_Login
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.sql.PreparedStatement
import java.sql.SQLException

class SinsesionActivity : AppCompatActivity() {

    object EmergenciaState {
        var idEmergencia: Int? = null // Variable para almacenar el ID de la emergencia enviada
        var enviada: Boolean = false // Variable para verificar si la emergencia ha sido enviada
    }

    private lateinit var locationService:LocationServiceUrgencia
    private lateinit var mAuth: FirebaseAuth
    private var isCooldown = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.sinsesion) // Asegúrate de que este es el layout correcto

        mAuth = FirebaseAuth.getInstance() // Inicializa la autenticación de Firebase

        // Configuración para Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.id_client))
            .requestEmail()
            .build()


        // Obtiene el usuario actual
        val auth = Firebase.auth
        val user = auth.currentUser

        // Inicializa el servicio de ubicación
        locationService = LocationServiceUrgencia(this@SinsesionActivity)
        locationService.checkAndRequestPermissions()

        val btnEmergencia = findViewById<Button>(R.id.btnEmergencia)


        btnEmergencia.setOnClickListener {
            showBottomSheet() // Muestra el BottomSheet para emergencias
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val builder = AlertDialog.Builder(this@SinsesionActivity, R.style.CustomAlertDialog)
                val customLayout = layoutInflater.inflate(R.layout.logout_personalizado, null)
                builder.setView(customLayout)

                val positiveButton: Button = customLayout.findViewById(R.id.positiveButton)
                val negativeButton: Button = customLayout.findViewById(R.id.negativeButton)

                val dialog = builder.create()

                positiveButton.setOnClickListener {
                    val pantallaLogin = Intent(this@SinsesionActivity, activity_Login::class.java)
                    startActivity(pantallaLogin) // Redirige al login
                    finish() // Cierra la actividad actual
                    dialog.dismiss()
                }

                negativeButton.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
        })


    }





    // Muestra el BottomSheet al usuario
    private fun showBottomSheet() {
        val context = this

        // Configuración del BottomSheet
        val bottomSheetDialog = BottomSheetDialog(context)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_emergencias, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val spGravedad = bottomSheetView.findViewById<Spinner>(R.id.spGravedad)
        val listadoGravedad = arrayOf("Baja", "Media", "Alta")
        val adapterSpinner = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listadoGravedad)
        spGravedad.adapter = adapterSpinner

        val txtTipoEmergencia = bottomSheetView.findViewById<EditText>(R.id.txtTipoEmergencia)
        val btnIncendio = bottomSheetView.findViewById<ToggleButton>(R.id.tbtnIncendio)
        val btnRescate = bottomSheetView.findViewById<ToggleButton>(R.id.tbtnRescate)
        val btnDerrumbe = bottomSheetView.findViewById<ToggleButton>(R.id.tbtnDerrumbe)
        val btnInundacion = bottomSheetView.findViewById<ToggleButton>(R.id.tbtnInundacion)
        val btnDerrame = bottomSheetView.findViewById<ToggleButton>(R.id.tbtnDerrame)
        val txtDescripcion = bottomSheetView.findViewById<EditText>(R.id.txtDescripcion)

        val toggleButtons = listOf(btnIncendio, btnRescate, btnDerrumbe, btnInundacion, btnDerrame)

        // Variable de cooldown

        // Revisa los items del spGravedad
        spGravedad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                toggleButtons.forEach { toggleButton ->
                    toggleButton.setOnClickListener {
                        if (toggleButton.isChecked) {
                            toggleButtons.filter { it != toggleButton }.forEach { it.isChecked = false }
                            txtTipoEmergencia.isEnabled = false
                            txtTipoEmergencia.setText(toggleButton.text.toString())
                        } else {
                            txtTipoEmergencia.isEnabled = true
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val btnAtras = bottomSheetView.findViewById<Button>(R.id.btnAtras)

        bottomSheetDialog.show()

        btnAtras.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        val btnEnviar = bottomSheetView.findViewById<Button>(R.id.btnEnviar)
        btnEnviar.setOnClickListener {
            if (!isCooldown) {
                var validacion = false

                val descripcion = txtDescripcion.text.toString()
                val tipo = txtTipoEmergencia.text.toString()

                if (descripcion.isEmpty()) {
                    txtDescripcion.error = "Descripción obligatoria"
                    validacion = true
                } else {
                    txtDescripcion.error = null
                }

                if (!validacion) {
                    // Activa el cooldown
                    isCooldown = true
                    CoroutineScope(Dispatchers.IO).launch {
                        var insertEmergencia: PreparedStatement? = null
                        try {
                            val ubicacion = withContext(Dispatchers.IO) {
                                LocationServiceUrgencia(this@SinsesionActivity).getUbicacionAsync(this@SinsesionActivity)
                            }

                            if (ubicacion.isNotBlank()) {
                                val objConexion = ClaseConexion().cadenaConexion()
                                insertEmergencia = objConexion?.prepareStatement(
                                    "INSERT INTO Emergencias (ubicacion_emergencia, descripcion_emergencia, gravedad_emergencia, tipo_emergencia, respuesta_notificacion, estado_emergencia) VALUES (?, ?, ?, ?, ?, ?)"
                                )

                                if (insertEmergencia != null) {
                                    insertEmergencia.setString(1, ubicacion)
                                    insertEmergencia.setString(2, txtDescripcion.text.toString())
                                    insertEmergencia.setString(3, spGravedad.selectedItem.toString())
                                    insertEmergencia.setString(4, txtTipoEmergencia.text.toString())
                                    insertEmergencia.setString(5, "En espera")
                                    insertEmergencia.setString(6, "En proceso")

                                    val resultado = insertEmergencia.executeUpdate()
                                    if (resultado > 0) {
                                        EmergenciaState.idEmergencia = resultado
                                        EmergenciaState.enviada = true

                                        // Aquí asegúrate de que estás en el hilo principal antes de actualizar la UI
                                        withContext(Dispatchers.Main) {
                                            MotionToast.createColorToast(
                                                this@SinsesionActivity,
                                                "Emergencia enviada",
                                                "Datos enviados correctamente",
                                                MotionToastStyle.SUCCESS,
                                                MotionToast.GRAVITY_BOTTOM,
                                                MotionToast.LONG_DURATION,
                                                ResourcesCompat.getFont(this@SinsesionActivity, R.font.cabin_bold)
                                            )

                                            // Cierra el BottomSheet en el hilo principal
                                            bottomSheetDialog.dismiss()
                                        }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        MotionToast.createColorToast(
                                            this@SinsesionActivity,
                                            "¡Error!",
                                            "Error al preparar la consulta.",
                                            MotionToastStyle.ERROR,
                                            MotionToast.GRAVITY_BOTTOM,
                                            MotionToast.LONG_DURATION,
                                            ResourcesCompat.getFont(this@SinsesionActivity, R.font.cabin_bold)
                                        )
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    MotionToast.createColorToast(
                                        this@SinsesionActivity,
                                        "¡Error!",
                                        "No se pudo obtener la ubicación.",
                                        MotionToastStyle.ERROR,
                                        MotionToast.GRAVITY_BOTTOM,
                                        MotionToast.LONG_DURATION,
                                        ResourcesCompat.getFont(this@SinsesionActivity, R.font.cabin_bold)
                                    )
                                }
                            }
                        } catch (e: SQLException) {
                            withContext(Dispatchers.Main) {
                                MotionToast.createColorToast(
                                    this@SinsesionActivity,
                                    "¡Error SQL!",
                                    "Error al enviar la emergencia: ${e.message}",
                                    MotionToastStyle.ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(this@SinsesionActivity, R.font.cabin_bold)
                                )
                            }
                            println("El error es: " + e)
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                MotionToast.createColorToast(
                                    this@SinsesionActivity,
                                    "¡Error!",
                                    "Error inesperado: ${e.message}",
                                    MotionToastStyle.ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(this@SinsesionActivity, R.font.cabin_bold)
                                )
                            }
                        } finally {
                            insertEmergencia?.close()
                            delay(90000) // 1 minuto y medio
                            isCooldown = false
                        }
                    }
                }
            } else {

                // Mensaje indicando que está en cooldown
                MotionToast.createColorToast(
                    this@SinsesionActivity,
                    "¡Espera!",
                    "Por favor, espera 1 minuto y medio antes de enviar otra emergencia.",
                    MotionToastStyle.INFO,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this@SinsesionActivity, R.font.cabin_bold)
                )
            }
        }
    }



    //Pide los permisos de ubicación para ser utilizados
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationService.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
