package Modelo

data class RespuestaEstaciones(
    val results: List<PlaceResult>
)

data class PlaceResult(
    val name: String,
    val geometry: Geometry
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)