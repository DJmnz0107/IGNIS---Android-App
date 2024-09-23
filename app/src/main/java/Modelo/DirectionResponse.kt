package Modelo

// Clase que representa la respuesta de la API de direcciones.
data class DirectionsResponse(
    val routes: List<Route>  // Lista de rutas que puede incluir la respuesta.
)

// Clase que representa una ruta específica.
data class Route(
    val legs: List<Leg>  // Lista de tramos (legs) que componen la ruta.
)

// Clase que representa un tramo de la ruta.
data class Leg(
    val distance: Distance,  // Distancia total del tramo.
    val duration: Duration   // Duración total del tramo.
)

// Clase que representa la distancia de un tramo.
data class Distance(
    val text: String,  // Representación textual de la distancia (e.g., "10 km").
    val value: Int     // Valor numérico de la distancia en metros.
)

// Clase que representa la duración de un tramo.
data class Duration(
    val text: String,  // Representación textual de la duración (e.g., "15 mins").
    val value: Int     // Valor numérico de la duración en segundos.
)

