package Modelo

import java.sql.Date

data class dataClassMisiones(
    var idMision: Int, var descripcionMision: String, var fechaMision: Date, var idEmergencia: Int
)
