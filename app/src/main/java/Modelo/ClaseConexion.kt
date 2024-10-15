package Modelo

import java.sql.Connection
import java.sql.DriverManager

class ClaseConexion {

    fun cadenaConexion(): Connection? {
        try {
            val ip = "jdbc:oracle:thin:@172.23.160.1:1521:xe"
            val usuario = "IGNISULTIMATE" //Usuario de la base de datos
            val contrasena = "IGNISULTIMATE" //Contraseña de la conexion

            val conexion = DriverManager.getConnection(ip, usuario, contrasena)
            return conexion

        }
        catch(e: Exception) {
            println("El error es: $e")
            return null
        }

    }

}