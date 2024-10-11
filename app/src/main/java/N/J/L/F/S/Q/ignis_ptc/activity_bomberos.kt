package N.J.L.F.S.Q.ignis_ptc

import Modelo.ClaseConexion
import Modelo.Emergencia
import Modelo.LocationService
import Modelo.LocationServiceBomberos
import N.J.L.F.S.Q.ignis_ptc.databinding.ActivityBomberosBinding
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.sql.SQLException


class activity_bomberos : AppCompatActivity() {

    private lateinit var binding: ActivityBomberosBinding
    private lateinit var locationService: LocationServiceBomberos
    private var userId: Int = -1 // Variable para almacenar el ID del usuario
    private var idBombero:Int? = -1
    private var respuestaNotificacion: String? = ""
    private var idEmergencia:Int? = null
    private var shouldContinueUpdating = true
    private var lastLocation: LatLng? = null
    private var isUpdatingLocation = false
    private val THRESHOLD_DISTANCE = 10.0 // Distancia en metros



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        //Llama a los permisos de ubicación
        locationService = LocationServiceBomberos(this@activity_bomberos)

        //Chequea si estos han sido otorgados
        locationService.checkAndRequestPermissions()
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityBomberosBinding.inflate(layoutInflater)
        setContentView(binding.root)


        userId = intent.getIntExtra("userId", -1)


        comprobarActualizarEmergencia()






        val navController = findNavController(R.id.nav_host_fragment_container)

        val navView: BottomNavigationView = binding.navBomberos


        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.inicio_Bombero, R.id.informe_Bomberos, R.id.ubicaciones_Bomberos
            )
        )

        navView.setupWithNavController(navController)



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



    private fun iniciarActualizacionUbicacion() {
        if (isUpdatingLocation) return

        isUpdatingLocation = true
        CoroutineScope(Dispatchers.IO).launch {
            while (shouldContinueUpdating) {
                val ubicacion = locationService.ubicacionLatLng(this@activity_bomberos)
                if (ubicacion != null && idBombero != null && idEmergencia != null) {
                    if (shouldSendUpdate(ubicacion)) {
                        actualizarUbicacionBombero(idBombero, ubicacion, idEmergencia)
                        lastLocation = ubicacion // Actualiza la última ubicación
                    }
                }
                delay(5000) // Intervalo ajustable
            }
            isUpdatingLocation = false
        }
    }


    private fun shouldSendUpdate(newLocation: LatLng): Boolean {
        // Implementa lógica para determinar si se debe enviar la ubicación
        if (lastLocation == null) return true // Si es la primera ubicación
        val distance = calculateDistance(lastLocation!!, newLocation) // Implementa una función para calcular la distancia
        return distance > THRESHOLD_DISTANCE // THRESHOLD_DISTANCE es la distancia mínima para enviar una actualización
    }

    private fun calculateDistance(lastLocation: LatLng, newLocation: LatLng): Float {
        val locationA = Location("point A")
        locationA.latitude = lastLocation.latitude
        locationA.longitude = lastLocation.longitude

        val locationB = Location("point B")
        locationB.latitude = newLocation.latitude
        locationB.longitude = newLocation.longitude

        return locationA.distanceTo(locationB) // Retorna la distancia en metros
    }


    private fun actualizarUbicacionBombero(idBombero: Int?, ubicacion: LatLng, idEmergencia: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
            while (shouldContinueUpdating) {
                val db = FirebaseFirestore.getInstance()
                val docRef = db.collection("CamionesBomberos").document("ID_del_bombero_$idBombero")

                // Verifica que el ID de emergencia no sea nulo
                if (idEmergencia == null) {
                    Log.e("ActivityBomberos", "El ID de emergencia es nulo.")
                    return@launch // Sale de la coroutine si el ID es nulo
                }

                try {
                    // Verifica si el documento del bombero existe
                    val document = docRef.get().await()
                    if (document.exists()) {
                        // Obtiene el idEmergencia del documento
                        val idEmergenciaActual = document.getLong("idEmergencia")?.toInt()

                        if (idEmergenciaActual == null) {
                            Log.d("ActivityBomberos", "No se encontró idEmergencia en el documento del bombero.")
                            // Aquí puedes decidir qué hacer si el idEmergencia no existe
                            crearUbicacionBombero(idBombero, ubicacion, idEmergencia)
                        } else if (idEmergenciaActual == idEmergencia) {
                            // Si el idEmergencia coincide, actualiza la ubicación del bombero
                            Log.d("ActivityBomberos", "idEmergencia encontrado y coincide: $idEmergenciaActual")
                            docRef.update("ubicacion", com.google.firebase.firestore.GeoPoint(ubicacion.latitude, ubicacion.longitude))
                                .addOnSuccessListener {
                                    Log.d("ActivityBomberos", "Ubicación actualizada exitosamente: $ubicacion")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("ActivityBomberos", "Error al actualizar la ubicación: $e")
                                }
                        } else {
                            Log.d("ActivityBomberos", "El idEmergencia no coincide. Actual: $idEmergenciaActual, Nuevo: $idEmergencia")
                            // Si es necesario, aquí puedes decidir crear la ubicación
                            crearUbicacionBombero(idBombero, ubicacion, idEmergencia)
                        }
                    } else {
                        Log.d("ActivityBomberos", "No se encontró documento para el bombero: ID $idBombero.")
                        crearUbicacionBombero(idBombero, ubicacion, idEmergencia) // Crea una nueva ubicación si no hay documento
                    }
                } catch (e: Exception) {
                    Log.e("ActivityBomberos", "Error al verificar el documento del bombero: ${e.message}")
                }

                delay(5000) // Agrega un retraso para evitar llamadas constantes
            }
        }
    }








    fun comprobarActualizarEmergencia() {
        // Asumiendo que estás dentro de un CoroutineScope
        if (userId != -1) {
            println("ID del usuario: $userId")

            // Llama a la función suspendida en el lifecycleScope
            lifecycleScope.launch {
                // Llama a la función para obtener el id del bombero
                idBombero = obtenerIdBomberoPorUsuarioId(userId)

                println(idBombero)

                if (idBombero != null) {
                    println("ID del bombero encontrado: $idBombero")

                    // Llama a la función para obtener la emergencia asignada
                    val emergencia = obtenerEmergenciaAsignada(idBombero)

                    if (emergencia != null) {

                        idEmergencia = emergencia.id
                        respuestaNotificacion = obtenerRespuestaNotificacion(idEmergencia!!)

                        println(respuestaNotificacion)

                        if(respuestaNotificacion == "En camino") {
                            iniciarActualizacionUbicacion()
                        } else {
                            println("No se encontró una emergencia enviada : ${emergencia.respuestaNotificacion}")
                        }
                        // Procesa la emergencia aquí
                        println("Emergencia encontrada: ${emergencia.descripcionEmergencia}")
                    } else {
                        println("No se encontró emergencia asignada para el bombero con ID: $idBombero")
                    }
                } else {
                    println("No se encontró un bombero para el ID de usuario: $userId")
                }
            }
        } else {
            println("No se recibió el ID del usuario.")
        }
    }

    fun detenerActualizacionUbicacion() {
        shouldContinueUpdating = false
    }

    private fun crearUbicacionBombero(idBombero: Int?, ubicacion: LatLng, idEmergencia: Int?) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("CamionesBomberos").document("ID_del_bombero_$idBombero")

        // Verifica si ya existe el documento
        docRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    // Solo crea el documento si no existe
                    val data = hashMapOf(
                        "ubicacion" to com.google.firebase.firestore.GeoPoint(ubicacion.latitude, ubicacion.longitude),
                        "idEmergencia" to idEmergencia // Almacena el ID de la emergencia
                    )

                    docRef.set(data)
                        .addOnSuccessListener {
                            Log.d("ActivityBomberos", "Ubicación creada exitosamente: $ubicacion con ID de emergencia: $idEmergencia")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ActivityBomberos", "Error al crear la ubicación: $e")
                        }
                } else {
                    Log.d("ActivityBomberos", "La ubicación ya existe para ID del bombero: $idBombero")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ActivityBomberos", "Error al verificar la existencia del documento: $e")
            }
    }








    suspend fun obtenerEmergenciaAsignada(idBombero: Int?): Emergencia? {
        if (idBombero == null) {
            println("ID del bombero es null, no se puede buscar la emergencia asignada.")
            return null // Devuelve null si el ID es null
        }

        var emergenciaAsignada: Emergencia? = null

        // Ejecuta la consulta en el hilo de IO
        withContext(Dispatchers.IO) {
            val objConexion = ClaseConexion().cadenaConexion()

            objConexion?.use { conexion ->
                val statement = conexion.prepareStatement(
                    """
                SELECT e.id_emergencia, e.descripcion_emergencia, e.ubicacion_emergencia, 
                       e.gravedad_emergencia, e.tipo_emergencia, e.respuesta_notificacion, 
                       e.estado_emergencia
                FROM Misiones_Bomberos mb
                JOIN Misiones m ON mb.id_mision = m.id_mision
                JOIN Emergencias e ON m.id_emergencia = e.id_emergencia
                WHERE mb.id_bombero = ?
                ORDER BY m.fecha_mision DESC
                FETCH FIRST 1 ROW ONLY
                """
                )

                statement.setInt(1, idBombero)

                val result = statement.executeQuery()

                if (result.next()) {
                    val idEmergencia = result.getInt("id_emergencia")
                    val descripcionEmergencia = result.getString("descripcion_emergencia")
                    val ubicacionEmergencia = result.getString("ubicacion_emergencia")
                    val gravedadEmergencia = result.getString("gravedad_emergencia")
                    val tipoEmergencia = result.getString("tipo_emergencia")
                    val respuestaNotificacion = result.getString("respuesta_notificacion")
                    val estadoEmergencia = result.getString("estado_emergencia")

                    emergenciaAsignada = Emergencia(
                        idEmergencia,
                        ubicacionEmergencia,
                        descripcionEmergencia,
                        gravedadEmergencia,
                        tipoEmergencia,
                        respuestaNotificacion,
                        estadoEmergencia
                    )
                }
            }
        }

        return emergenciaAsignada
    }



    suspend fun obtenerIdBomberoPorUsuarioId(idUsuario: Int): Int? {
        var idBombero: Int? = null // Mantén la declaración aquí

        withContext(Dispatchers.IO) {
            try {
                val objConexion = ClaseConexion().cadenaConexion()
                println("Consultando el ID del bombero para el ID de usuario: $idUsuario")

                objConexion?.use { conexion ->
                    val statement = conexion.prepareStatement(
                        "SELECT id_bombero FROM Bomberos WHERE id_usuario = ?"
                    )
                    statement.setInt(1, idUsuario)
                    val result = statement.executeQuery()

                    if (result.next()) {
                        idBombero = result.getInt("id_bombero")
                        println("ID del bombero encontrado: $idBombero")
                    } else {
                        println("No se encontró un bombero para el ID de usuario: $idUsuario")
                    }
                }
            } catch (e: SQLException) {
                println("Error en la consulta: ${e.message}")
            }
        }


        return idBombero // Esto ahora devolverá el valor correcto
    }


    suspend fun obtenerRespuestaNotificacion(idEmergencia: Int): String? {
        var respuestaNotificacion: String? = null

        withContext(Dispatchers.IO) {
            try {
                val objConexion = ClaseConexion().cadenaConexion()
                println("Consultando respuesta_notificacion para el ID de emergencia: $idEmergencia")

                objConexion?.use { conexion ->
                    val statement = conexion.prepareStatement(
                        "SELECT respuesta_notificacion FROM Emergencias WHERE id_emergencia = ?"
                    )
                    statement.setInt(1, idEmergencia)
                    val result = statement.executeQuery()

                    if (result.next()) {
                        respuestaNotificacion = result.getString("respuesta_notificacion")
                        println("Respuesta de notificación encontrada: $respuestaNotificacion")
                    } else {
                        println("No se encontró respuesta de notificación para el ID de emergencia: $idEmergencia")
                    }
                }
            } catch (e: SQLException) {
                println("Error en la consulta: ${e.message}")
            }
        }

        return respuestaNotificacion
    }













}