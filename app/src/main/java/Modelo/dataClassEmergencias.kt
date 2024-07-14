package Modelo

data class dataClassEmergencias(
    val id:Int,
    val ubicacionEmergencia:String,
    val descripcionEmergencia:String,
    val gravedadEmergencia:String,
    val tipoEmergencia:String,
    val respuestaNotificacion:String,
    var estadoEmergencia:String

)
