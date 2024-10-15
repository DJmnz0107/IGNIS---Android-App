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
import android.widget.Button
import androidx.appcompat.app.AlertDialog
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

    object datosBombero {
        var idBombero:Int? = -1
    }

    private lateinit var binding: ActivityBomberosBinding
    private lateinit var locationService: LocationServiceBomberos
    private var userId: Int = -1 // Variable para almacenar el ID del usuario
    private var respuestaNotificacion: String? = ""
    private var estadoEmergencia:String? = ""
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
                if (ubicacion != null && datosBombero.idBombero != null && idEmergencia != null) {
                    if (shouldSendUpdate(ubicacion)) {
                        actualizarUbicacionBombero(datosBombero.idBombero, ubicacion, idEmergencia)
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
                // Crear docRef usando el ID del bombero y el ID de la emergencia
                val docRef = db.collection("CamionesBomberos").document("ID_del_bombero_$idBombero" + "_emergencia_$idEmergencia")

                // Verifica que el ID de emergencia no sea nulo
                if (idEmergencia == null) {
                    Log.e("ActivityBomberos", "El ID de emergencia es nulo.")
                    return@launch
                }

                try {
                    // Verifica si el documento del bombero existe
                    val document = docRef.get().await()
                    if (document.exists()) {
                        // Actualiza la ubicación
                        docRef.update("ubicacion", com.google.firebase.firestore.GeoPoint(ubicacion.latitude, ubicacion.longitude))
                            .addOnSuccessListener {
                                Log.d("ActivityBomberos", "Ubicación actualizada exitosamente: $ubicacion")
                            }
                            .addOnFailureListener { e ->
                                Log.e("ActivityBomberos", "Error al actualizar la ubicación: $e")
                            }
                    } else {
                        // Si no existe el documento, llama a crearUbicacionBombero
                        crearUbicacionBombero(idBombero, ubicacion, idEmergencia)
                    }
                } catch (e: Exception) {
                    Log.e("ActivityBomberos", "Error al verificar el documento del bombero: ${e.message}")
                }

                delay(5000) // Intervalo ajustable
            }
        }
    }

    private fun mostrarDialogoEmergencia() {
        val dialogoView = layoutInflater.inflate(R.layout.dialog_emergencia, null)
        val dialogoBuilder = AlertDialog.Builder(this)
            .setView(dialogoView)
            .setCancelable(false) // Desactivar el cierre con el fondo

        val dialogo = dialogoBuilder.create()

        // Configurar el botón de cerrar
        dialogoView.findViewById<Button>(R.id.btnCerrar).setOnClickListener {
            dialogo.dismiss()
        }

        dialogo.show()
    }

    private fun mostrarDialogoNoEmergencia() {
        val dialogoView = layoutInflater.inflate(R.layout.dialog_no_emergencia, null)
        val dialogoBuilder = AlertDialog.Builder(this)
            .setView(dialogoView)
            .setCancelable(false) // Desactivar el cierre con el fondo

        val dialogo = dialogoBuilder.create()

        // Configurar el botón de cerrar
        dialogoView.findViewById<Button>(R.id.btnNoEmergenciaCerrar).setOnClickListener {
            dialogo.dismiss()
        }

        dialogo.show()
    }








    fun comprobarActualizarEmergencia() {
        // Asumiendo que estás dentro de un CoroutineScope
        if (userId != -1) {
            println("ID del usuario: $userId")

            // Llama a la función suspendida en el lifecycleScope
            lifecycleScope.launch {
                // Llama a la función para obtener el id del bombero
                datosBombero.idBombero = obtenerIdBomberoPorUsuarioId(userId)

                var id = datosBombero.idBombero

                println(datosBombero.idBombero)

                if (datosBombero.idBombero != null) {
                    println("ID del bombero encontrado: $id")

                    // Llama a la función para obtener la emergencia asignada
                    val emergencia = obtenerEmergenciaAsignada(id)

                    if (emergencia != null) {

                        idEmergencia = emergencia.id
                        respuestaNotificacion = obtenerRespuestaNotificacion(idEmergencia!!)
                        estadoEmergencia = obtenerEstadoEmergencia(idEmergencia!!)


                        println(respuestaNotificacion)
                        println(estadoEmergencia)

                        if(respuestaNotificacion == "En camino" && estadoEmergencia == "En proceso") {
                            mostrarDialogoEmergencia()
                            iniciarActualizacionUbicacion()
                        }
                        else {
                            mostrarDialogoNoEmergencia()
                            println("No se encontró una emergencia enviada : ${emergencia.respuestaNotificacion}")
                            println("O el estado de la emergencia es : ${emergencia.estadoEmergencia}")
                        }
                        // Procesa la emergencia aquí
                        println("Emergencia encontrada: ${emergencia.descripcionEmergencia}")
                    } else {
                        println("No se encontró emergencia asignada para el bombero con ID: $id")
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
        // Nuevo docRef único para cada combinación de bombero y emergencia
        val docRef = db.collection("CamionesBomberos").document("ID_del_bombero_$idBombero" + "_emergencia_$idEmergencia")

        db.runTransaction { transaction ->
            val existingDocument = transaction.get(docRef)

            if (existingDocument.exists()) {
                Log.d("ActivityBomberos", "Ya existe un documento con el ID del bombero $idBombero y emergencia $idEmergencia")
            } else {
                // Crea un nuevo documento
                val data = hashMapOf(
                    "ubicacion" to com.google.firebase.firestore.GeoPoint(ubicacion.latitude, ubicacion.longitude),
                    "idEmergencia" to idEmergencia
                )
                transaction.set(docRef, data)
                Log.d("ActivityBomberos", "Ubicación creada exitosamente: $ubicacion con ID de emergencia: $idEmergencia")
            }
        }.addOnSuccessListener {
            Log.d("ActivityBomberos", "Transacción completada con éxito.")
        }.addOnFailureListener { e ->
            Log.e("ActivityBomberos", "Error en la transacción: $e")
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
                  AND m.fecha_mision = (SELECT MAX(fecha_mision)
                                         FROM Misiones
                                         WHERE id_emergencia IN (SELECT id_emergencia
                                                                 FROM Misiones m2
                                                                 JOIN Misiones_Bomberos mb2 ON mb2.id_mision = m2.id_mision
                                                                 WHERE mb2.id_bombero = ?))
                ORDER BY m.fecha_mision DESC
                FETCH FIRST 1 ROW ONLY
                """
                )

                statement.setInt(1, idBombero)
                statement.setInt(2, idBombero) // Agregar el idBombero a la subconsulta

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

    suspend fun obtenerEstadoEmergencia(idEmergencia: Int): String? {
        return withContext(Dispatchers.IO) {
            var estadoEmergencia: String? = null

            try {
                val objConexion = ClaseConexion().cadenaConexion()
                println("Consultando estado_emergencia para el ID de emergencia: $idEmergencia")

                objConexion?.use { conexion ->
                    val statement = conexion.prepareStatement(
                        "SELECT estado_emergencia FROM Emergencias WHERE id_emergencia = ?"
                    )
                    statement.setInt(1, idEmergencia)
                    val result = statement.executeQuery()

                    if (result.next()) {
                        estadoEmergencia = result.getString("estado_emergencia") // Obtener el estado de la emergencia
                        println("Estado de la emergencia encontrado: $estadoEmergencia")
                    } else {
                        println("No se encontró el estado de la emergencia para el ID de emergencia: $idEmergencia")
                    }
                }
            } catch (e: SQLException) {
                println("Error en la consulta: ${e.message}")
            }

            estadoEmergencia // Devolver el valor aquí
        }
    }














}