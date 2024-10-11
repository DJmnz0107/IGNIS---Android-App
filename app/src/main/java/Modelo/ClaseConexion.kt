package Modelo

import java.sql.Connection
import java.sql.DriverManager

class ClaseConexion {

    fun cadenaConexion(): Connection? {
        try {
            val ip = "jdbc:oracle:thin:@192.168.0.4:1521:xe"
            val usuario = "IGNIS" //Usuario de la base de datos
            val contrasena = "ignis_db" //Contraseña de la conexion

            val conexion = DriverManager.getConnection(ip, usuario, contrasena)
            return conexion

        }
        catch(e: Exception) {
            println("El error es: $e")
            return null
        }

    }

}