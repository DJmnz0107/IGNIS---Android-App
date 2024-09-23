package Modelo

import java.sql.Connection
import java.sql.ResultSet

class Usuarios() {
    fun obtenerNivelUsuario(nombreUsuario: String, contrasena: String): Int? {
        // Variable para almacenar el nivel del usuario, inicialmente null.
        var nivelUsuario: Int? = null

        // Se establece una conexión a la base de datos mediante la clase de conexión.
        val objConexion = ClaseConexion().cadenaConexion()

        // Usamos la conexión dentro de un bloque 'use' para asegurarnos de que se cierre automáticamente.
        objConexion?.use { conexion ->
            // Preparamos una sentencia SQL para seleccionar el nivel del usuario según su nombre de usuario y contraseña.
            val statement = conexion.prepareStatement(
                "SELECT id_nivelusuario FROM Usuarios WHERE nombre_usuario = ? AND contrasena_usuario = ?"
            )
            // Asignamos los valores de nombre de usuario y contraseña a la consulta.
            statement.setString(1, nombreUsuario)
            statement.setString(2, contrasena)

            // Ejecutamos la consulta y obtenemos el resultado.
            val result = statement.executeQuery()

            // Si hay un resultado, obtenemos el nivel del usuario.
            if (result.next()) {
                nivelUsuario = result.getInt("id_nivelusuario")
            }
        }
        // Devolvemos el nivel del usuario, o null si no se encontró.
        return nivelUsuario
    }




}