package Modelo

import java.sql.Connection
import java.sql.DriverManager

class ClaseConexion {

    fun cadenaConexion(): Connection? {
        try {
            val ip = "jdbc:oracle:thin:@25.50.120.6:1521:xe"
            val usuario = "STEVEN_DISPO"
            val contrasena = "2007"

            val conexion = DriverManager.getConnection(ip, usuario, contrasena)
            return conexion

        }
        catch(e: Exception) {
            println("El error es: $e")
            return null
        }

    }

}