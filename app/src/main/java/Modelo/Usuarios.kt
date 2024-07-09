package Modelo

import java.sql.Connection
import java.sql.ResultSet

class Usuarios() {
    fun obtenerNivelUsuario(nombreUsuario: String, contrasena: String): Int? {
        var nivelUsuario: Int? = null
        val objConexion = ClaseConexion().cadenaConexion()

        objConexion?.use { conexion ->
            val statement = conexion.prepareStatement("SELECT id_nivelusuario FROM Usuarios WHERE nombre_usuario = ? AND contrasena_usuario = ?")
            statement.setString(1, nombreUsuario)
            statement.setString(2, contrasena)

            val result = statement.executeQuery()
            if (result.next()) {
                nivelUsuario = result.getInt("id_nivelusuario")
            }
        }
        return nivelUsuario
    }



}