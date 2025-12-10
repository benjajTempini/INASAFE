# Plan de Pruebas: Aplicación INASAFE

## 1. Introducción

El propósito de este documento es definir la estrategia y el plan de testing para el prototipo de la aplicación móvil INASAFE. El objetivo es asegurar la calidad, funcionalidad, seguridad y rendimiento de la aplicación antes de su entrega final, identificando y documentando errores para su posterior corrección.

## 2. Tipos de Pruebas a Realizar

Se ejecutarán cuatro tipos de pruebas principales para garantizar una cobertura integral:

1.  **Pruebas Funcionales:** Se centrarán en verificar que cada función de la aplicación opera de acuerdo con sus especificaciones. Se probarán los flujos principales de la aplicación, como el registro, el inicio de sesión, el envío de alertas, la consulta de paraderos y la interacción con los chats.

2.  **Pruebas de Usabilidad:** Evaluará la facilidad de uso de la interfaz, la intuición en la navegación y la claridad de la información presentada. El objetivo es asegurar que la experiencia del usuario sea fluida y agradable.

3.  **Pruebas de Seguridad:** Se enfocarán en identificar y corregir vulnerabilidades que puedan comprometer los datos del usuario o la integridad de la aplicación. Se tomarán como referencia los estándares de OWASP Mobile Top 10 y la norma ISO 27001.

4.  **Pruebas de Rendimiento:** Medirán el comportamiento de la aplicación bajo ciertas condiciones, como el uso de memoria, el consumo de batería y la velocidad de respuesta, especialmente en funcionalidades críticas como el mapa en tiempo real.

## 3. Herramientas de Testing

*   **Testing Manual:** Ejecución de casos de prueba por parte del equipo de desarrollo.
*   **Android Studio Profiler:** Para medir el uso de CPU, memoria y red durante las pruebas de rendimiento.
*   **Firebase Console:** Para verificar la correcta creación de usuarios y el almacenamiento de datos (alertas, perfiles).
*   **Supabase Dashboard:** Para monitorear la inserción y recuperación de mensajes en la base de datos en tiempo real.
*   **Logcat de Android Studio:** Para la detección y análisis de errores y excepciones en tiempo de ejecución.

## 4. Casos de Prueba

| ID | Funcionalidad | Caso de Prueba | Pasos a Seguir | Resultado Esperado (Criterio de Aceptación) | Resultado Real | Estado |
| :-- | :--- | :--- | :--- | :--- | :--- | :--- |
| **FUNC-01** | Autenticación | Inicio de sesión con credenciales válidas. | 1. Abrir la app. 2. Ingresar un email y contraseña registrados. 3. Pulsar "Login". | El usuario es redirigido a la `MainActivity`. | | Pass/Fail |
| **FUNC-02** | Autenticación | Inicio de sesión con credenciales inválidas. | 1. Abrir la app. 2. Ingresar un email o contraseña incorrectos. 3. Pulsar "Login". | Se muestra un mensaje de error indicando "Fallo en la autenticación". | | Pass/Fail |
| **FUNC-03** | Registro | Registro de un nuevo usuario con datos válidos. | 1. Ir a la pantalla de registro. 2. Llenar nombre, email y contraseñas coincidentes. 3. Pulsar "Registrarse". | Se crea el usuario en Firebase, se guarda su nombre y rol, y es redirigido al Login. | | Pass/Fail |
| **FUNC-04** | Alertas | Envío de una alerta de pánico. | 1. Iniciar sesión. 2. En `MainActivity`, pulsar el "Botón de Pánico". | Se obtiene la ubicación GPS y se guarda una nueva alerta en Firebase con los datos correctos. | | Pass/Fail |
| **FUNC-05** | Mapa | Visualización de paraderos cercanos en el mapa. | 1. Abrir el mapa desde el menú principal. | El mapa se centra en la ubicación del usuario y muestra marcadores de los paraderos cercanos obtenidos de la API. | | Pass/Fail |
| **FUNC-06** | Chat | Envío y recepción de mensajes en tiempo real. | 1. Usuario A y Usuario B entran al mismo grupo de chat. 2. Usuario A envía un mensaje. | El mensaje enviado por el Usuario A aparece instantáneamente en la pantalla del Usuario B sin necesidad de recargar. | | Pass/Fail |
| **USAB-01** | Usabilidad | Flujo completo desde el mapa al chat. | 1. Abrir el mapa. 2. Tocar el marcador de un paradero. 3. Tocar "Ir al Grupo". | La navegación es fluida y lógica, llevando al usuario sin problemas desde el mapa hasta el chat del paradero seleccionado. | | Pass/Fail |
| **SEG-01** | Seguridad | **(Corregido)** Almacenamiento de claves de API. | 1. Analizar el código fuente de la app. | Las credenciales sensibles (Supabase URL y Key) NO deben estar escritas directamente en el código. Deben cargarse desde un lugar seguro. | **Antes:** Fallaba. **Después:** Pasa. Las claves se cargan desde `local.properties` vía `BuildConfig`. | Pass |
| **PERF-01** | Rendimiento | Consumo de memoria del mapa. | 1. Abrir el mapa. 2. Navegar y hacer zoom por el mapa durante 2 minutos. 3. Medir el uso de memoria con Android Studio Profiler. | El uso de memoria se mantiene estable, sin fugas (memory leaks) ni incrementos descontrolados. | | Pass/Fail |

---
*Este plan debe ser utilizado como base para la ejecución de pruebas. Los campos "Resultado Real" y "Estado" deben ser completados por el equipo durante la fase de ejecución.*