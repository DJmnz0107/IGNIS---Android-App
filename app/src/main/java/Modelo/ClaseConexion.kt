package Modelo

import java.sql.Connection
import java.sql.DriverManager

class ClaseConexion {

    fun cadenaConexion(): Connection? {
        try {

            val ip = "jdbc:oracle:thin:@192.168.0.5:1521:xe"
            val usuario = "NATAREN_DEVELOPER"
            val contrasena = "Nata123"

            val conexion = DriverManager.getConnection(ip, usuario, contrasena)
            return conexion

        }
        catch(e: Exception) {
            println("El error es: $e")
            return null
        }

    }

}