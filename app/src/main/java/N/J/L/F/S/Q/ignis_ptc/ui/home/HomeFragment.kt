package N.J.L.F.S.Q.ignis_ptc.ui.home

import Modelo.ClaseConexion
import Modelo.LocationService
import N.J.L.F.S.Q.ignis_ptc.MainActivity
import N.J.L.F.S.Q.ignis_ptc.R
import N.J.L.F.S.Q.ignis_ptc.activity_Login
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import N.J.L.F.S.Q.ignis_ptc.databinding.FragmentHomeBinding
import android.content.Intent
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var locationService: LocationService
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mAuth = FirebaseAuth.getInstance() // Inicializa la autenticación de Firebase

        // Configuración para Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.id_client))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        // Obtiene el usuario actual
        val auth = Firebase.auth

        mAuth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // Inicializa el servicio de ubicación
        locationService = LocationService(MainActivity())

        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val btnEmergencia = root.findViewById<Button>(R.id.btnEmergencia)

        val btnCerrarSesion = root.findViewById<Button>(R.id.btnCerrarSesion)

        btnEmergencia.setOnClickListener {

            showBottomSheet() // Muestra el BottomSheet para emergencias

        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val context = requireContext()

                val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
                val customLayout = layoutInflater.inflate(R.layout.logout_personalizado, null)
                builder.setView(customLayout)

                val positiveButton: Button = customLayout.findViewById(R.id.positiveButton)
                val negativeButton: Button = customLayout.findViewById(R.id.negativeButton)

                val dialog = builder.create()

                positiveButton.setOnClickListener {
                    val pantallaLogin = Intent(requireActivity(), activity_Login::class.java)
                    startActivity(pantallaLogin) // Redirige al login
                    requireActivity().finish() // Cierra la actividad actual
                    dialog.dismiss()
                }

                negativeButton.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
        })

        btnCerrarSesion.setOnClickListener {
            val context = requireContext()

            val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
            val customLayout = layoutInflater.inflate(R.layout.logout_personalizado, null)
            builder.setView(customLayout)

            val positiveButton: Button = customLayout.findViewById(R.id.positiveButton)
            val negativeButton: Button = customLayout.findViewById(R.id.negativeButton)

            val dialog = builder.create()

            positiveButton.setOnClickListener {
                val user = mAuth.currentUser
                if (user != null) {
                    handleSignOut(user) //Cierra la sesión al usuario si esta conectado con Google
                }else {
                    val pantallaLogin = Intent(requireActivity(), activity_Login::class.java)
                    startActivity(pantallaLogin)
                    requireActivity().finish() //Cierra la sesión al usuario si esta conectado a la base de datos
                }
                dialog.dismiss()
            }

            negativeButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }



        return root
    }


    //Maneja el cierre de sesión con Google
    private fun handleSignOut(user: FirebaseUser) {
        user.providerData.forEach { userInfo ->
            if (userInfo.providerId == "google.com") {
                signOutAndStartSignInActivity()
                return
            }
        }


    }


    //Manejo del inicio de sesión con google para iniciar otra actividad
    private fun signOutAndStartSignInActivity() {
        mAuth.signOut()

        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity()) {
            val intent = Intent(requireActivity(), activity_Login::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }


    //Muestra el bottomSheet al usuario
    private fun showBottomSheet() {
        val context = context ?: return

        //Configuración del bottomsheet
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


        //Revisa los items del spGravedad
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

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val textDescripcion = txtDescripcion.text.toString()

        val txtTipo = txtTipoEmergencia.text.toString()



        val btnAtras = bottomSheetView.findViewById<Button>(R.id.btnAtras)

        bottomSheetDialog.show()

        btnAtras.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        val btnEnviar = bottomSheetView.findViewById<Button>(R.id.btnEnviar)
        btnEnviar.setOnClickListener {

            var validacion = false

            val descripcion = txtDescripcion.text.toString()
            val tipo = txtTipoEmergencia.text.toString()

            if (descripcion.isEmpty()) {
                txtDescripcion.error = "Descripción obligatoria"
                validacion = true
            } else {
                txtDescripcion.error = null
            }

            //Si la validacion es false, entonces continua con el código, sino, muestra errores
            if(!validacion) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val ubicacion = withContext(Dispatchers.IO) {
                            LocationService(context as MainActivity).getUbicacionAsync(context)
                        }

                        if (ubicacion.isNotBlank()) {
                            val objConexion = ClaseConexion().cadenaConexion()
                            val insertEmergencia = objConexion?.prepareStatement("INSERT INTO Emergencias (ubicacion_emergencia, descripcion_emergencia, gravedad_emergencia, tipo_emergencia, respuesta_notificacion, estado_emergencia) VALUES (?, ?, ?, ?, ?, ?)")!!

                            insertEmergencia.setString(1, ubicacion)
                            insertEmergencia.setString(2, txtDescripcion.text.toString())
                            insertEmergencia.setString(3, spGravedad.selectedItem.toString())
                            insertEmergencia.setString(4,txtTipoEmergencia.text.toString())
                            insertEmergencia.setString(5, "En espera")
                            insertEmergencia.setString(6, "En proceso")

                            insertEmergencia.executeUpdate()

                            withContext(Dispatchers.Main) {
                                MotionToast.createColorToast(requireContext() as MainActivity,
                                    "Emergencia enviada",
                                    "Datos enviado correctamente",
                                    MotionToastStyle.SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(requireContext() as MainActivity,R.font.cabin_bold))
                                txtDescripcion.text.clear()
                                txtTipoEmergencia.text.clear()
                            }
                        } else {
                            MotionToast.createColorToast(requireContext() as MainActivity,
                                "Permisos de ubicación",
                                "Por favor, activa los permisos de ubicación para enviar la emergencia",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(requireContext() as MainActivity,R.font.cabin_bold))
                            txtDescripcion.text.clear()
                            txtTipoEmergencia.text.clear()                        }
                    } catch (e: Exception) {
                        println("El error es: $e")
                        withContext(Dispatchers.Main) {
                            MotionToast.createColorToast(requireContext() as MainActivity,
                                "Campos llenos",
                                "Por favor, revisa si has llenado todos los campos.",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(requireContext() as MainActivity,R.font.cabin_bold))                        }
                    }
                }
            }


        }
    }









    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}