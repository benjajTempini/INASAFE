package com.example.inasafe

data class BusArrival(
    val route: String,
    val time: String, // e.g., "5 min", "Llegando"
    val status: String // "A tiempo", "Retrasado"
)

data class BusStop(
    val id: String,
    val name: String,
    val location: String,
    val distance: String,
    val arrivals: List<BusArrival>
)

object BusStopRepository {
    fun getNearbyStops(): List<BusStop> {
        return listOf(
            BusStop(
                "PB2025",
                "Parada 3 / Norte Sur - Dorsal",
                "Autopista Central / Dorsal",
                " 333 m —  5 min caminando",
                listOf(
                    BusArrival("B17", "3 min", "A tiempo"),
                    BusArrival("101", "5 min", "A tiempo"),
                    BusArrival("107", "8 min", "Retrasado"),
                    BusArrival("107C", "12 min", "A tiempo")
                )
            ),
            BusStop(
                "PB422",
                "Parada 2 / Norte Sur - Dorsal",
                "Autopista Central / Dorsal",
                " 373 m — 6 min caminando",
                listOf(
                    BusArrival("303", "2 min", "A tiempo"),
                    BusArrival("307", "7 min", "A tiempo"),
                    BusArrival("314", "15 min", "Retrasado")
                )
            ),
            BusStop(
                "PB1471",
                "Bajos de Jiménez / Esq. 14 de la Fama",
                "Bajos de Jiménez",
                " 485 m — 7 min caminando",
                listOf(
                    BusArrival("B26", "Llegando", "A tiempo")
                )
            ),
            BusStop(
                "PB423",
                "Parada / INACAP – Renca",
                "Frente a INACAP",
                " 324 m — 5 min caminando",
                listOf(
                    BusArrival("303", "Llegando", "A tiempo"),
                    BusArrival("307", "6 min", "A tiempo"),
                    BusArrival("314", "10 min", "Retrasado")
                )
            )
        )
    }
}