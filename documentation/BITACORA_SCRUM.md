# Bitácora de Trabajo SCRUM: Proyecto INASAFE

## Product Backlog

El Product Backlog contiene una lista priorizada de todas las funcionalidades (Historias de Usuario) deseadas para la aplicación.

| ID | Historia de Usuario (Como... quiero... para...) | Prioridad |
| :-- | :--- | :--- |
| PBI-01 | Como **usuario nuevo**, quiero **registrarme en la app con mi nombre y correo** para tener una identidad personal. | Must Have |
| PBI-02 | Como **usuario registrado**, quiero **iniciar sesión de forma segura** para acceder a las funcionalidades de la app. | Must Have |
| PBI-03 | Como **usuario**, quiero **que mi sesión se mantenga iniciada** para no tener que introducir mis credenciales cada vez. | Should Have |
| PBI-04 | Como **usuario en peligro**, quiero **enviar una alerta de pánico con mi ubicación real** para notificar mi situación. | Must Have |
| PBI-05 | Como **usuario**, quiero **visualizar un mapa en tiempo real** para ver mi ubicación, los paraderos y las alertas activas. | Must Have |
| PBI-06 | Como **usuario**, quiero **consultar los paraderos de micro más cercanos** para planificar mi viaje. | Must Have |
| PBI-07 | Como **usuario**, quiero **ver los tiempos de llegada de las micros** en un paradero específico. | Must Have |
| PBI-08 | Como **usuario preocupado por la seguridad**, quiero **saber cuánto tardo en llegar al paradero** para minimizar el tiempo de espera. | Should Have |
| PBI-09 | Como **usuario frecuente**, quiero **marcar mis micros como favoritas** para filtrarlas y acceder a su información rápidamente. | Could Have |
| PBI-10 | Como **usuario**, quiero **unirme a grupos de chat basados en paraderos cercanos** para comunicarme con otras personas en la zona. | Must Have |
| PBI-11 | Como **usuario en un chat**, quiero **enviar y recibir mensajes en tiempo real** para una comunicación fluida. | Must Have |
| PBI-12 | Como **desarrollador**, quiero **proteger las credenciales de las APIs (Supabase)** para evitar accesos no autorizados. | Must Have |

## Sprint Planning

### Sprint 1: "Core Funcional y Seguridad"

*   **Objetivo del Sprint:** Construir las funcionalidades básicas de la aplicación y establecer una base segura.

| ID Historia | Tareas a Realizar |
| :--- | :--- |
| PBI-01, PBI-02 | Implementar pantallas y lógica de Registro y Login con Firebase Authentication. Guardar nombre de usuario. |
| PBI-04, PBI-05 | Integrar mapa (OSMDroid) y servicio de ubicación. Implementar botón de pánico que guarde alertas en Firebase con coordenadas reales. |
| PBI-06, PBI-07 | Integrar API de buses con Retrofit. Crear modelos de datos. Mostrar paraderos y tiempos de llegada. |
| PBI-12 | **(Tarea de Seguridad)** Mover credenciales de API fuera del código fuente, utilizando `local.properties` y `BuildConfig`. |

### Sprint 2: "Experiencia de Usuario y Funcionalidad Social"

*   **Objetivo del Sprint:** Mejorar la experiencia del usuario y añadir la capa social con los grupos de chat.

| ID Historia | Tareas a Realizar |
| :--- | :--- |
| PBI-03 | Implementar la persistencia de sesión en la `LoginActivity`. Añadir botón de Logout. |
| PBI-08 | Calcular y mostrar el tiempo de caminata y la "hora de salida" recomendada en la vista de llegadas. |
| PBI-09 | Implementar sistema de favoritos con `SharedPreferences`. Añadir botón de favorito y lógica de filtrado. |
| PBI-10, PBI-11 | Integrar Supabase para la base de datos y el servicio de Realtime. Crear la pantalla de grupos dinámicos y la pantalla de chat. |
| PBI-05 (Mejora) | Conectar el flujo del mapa y la lista de paraderos con el chat, permitiendo al usuario navegar de forma intuitiva. |