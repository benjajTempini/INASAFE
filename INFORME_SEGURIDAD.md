## Análisis y Corrección de Vulnerabilidad de Seguridad en INASAFE

### 1. Vulnerabilidad Detectada

Durante una revisión de seguridad del código fuente de la aplicación INASAFE, se detectó una vulnerabilidad crítica clasificada como **Almacenamiento Inseguro de Datos**.

**Descripción del Problema:** Las credenciales de acceso al backend de Supabase (específicamente la `supabaseUrl` y la `supabaseKey`) se encontraban codificadas directamente en el código fuente (hardcoded) dentro de la clase `InaSafeApplication.kt`.

```kotlin
// Código vulnerable ANTES de la corrección
supabaseClient = createSupabaseClient(
    supabaseUrl = "https://csljxjrfuyiaccolekqx.supabase.co",
    supabaseKey = "sb_publishable_48VxcrbggBRSr1sLN0ZDnw_0CG1n4U2"
) 
```

### 2. Principios y Normas de Seguridad Infringidas

Esta práctica infringe varios principios de seguridad reconocidos internacionalmente:

*   **OWASP Mobile Top 10 - M2: Insecure Data Storage:** Este es el riesgo más directo. Almacenar credenciales, claves de API o cualquier tipo de secreto directamente en el código de la aplicación es una de las prácticas más peligrosas. Si el código fuente se filtrara o la aplicación fuera sometida a ingeniería inversa, un atacante podría extraer estas credenciales con facilidad y obtener control total sobre la base de datos de Supabase, comprometiendo la privacidad de todos los datos de los usuarios (mensajes, perfiles, etc.).

*   **ISO/IEC 27001 (Anexo A.9.4 - Control de Acceso a la Información):** La norma ISO 27001 se centra en la gestión de la seguridad de la información. El control A.9.4 establece la necesidad de proteger el acceso a la información y a los sistemas. Al tener las credenciales expuestas en el código, se viola el principio de "mínimo privilegio" y se elimina cualquier control de acceso efectivo al backend, ya que la "contraseña" es pública para cualquiera que pueda leer el código.

### 3. Proceso de Corrección y Mejora

Para mitigar esta vulnerabilidad y alinear la aplicación con las buenas prácticas de seguridad, se implementó una solución robusta en varios pasos:

1.  **Externalización de Credenciales:** Las credenciales de Supabase se eliminaron del código fuente y se trasladaron al archivo `local.properties`. Este archivo está diseñado para almacenar información sensible específica de la máquina del desarrollador y, por defecto, está incluido en el archivo `.gitignore` del proyecto, lo que previene que sea subido a repositorios de control de versiones como GitHub.

    *Contenido añadido a `local.properties`:*
    ```properties
    supabase.url=https://[...].supabase.co
    supabase.key=[...]
    ```

2.  **Configuración Segura en Gradle:** Se modificó el archivo `app/build.gradle.kts` para que leyera de forma segura las propiedades desde `local.properties` durante el proceso de compilación.

3.  **Inyección de Credenciales en `BuildConfig`:** Se utilizó la funcionalidad `buildConfigField` de Gradle. Esta herramienta inyecta los valores leídos desde `local.properties` en una clase especial autogenerada por Android llamada `BuildConfig`. Esto permite que el código de la aplicación acceda a las credenciales de forma segura sin que estas estén visibles en el código fuente.

    *Código añadido a `app/build.gradle.kts`:*
    ```kotlin
    buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("supabase.url")}\"")
    buildConfigField("String", "SUPABASE_KEY", "\"${localProperties.getProperty("supabase.key")}\"")
    ```

4.  **Uso de Credenciales Seguras:** Finalmente, se actualizó la clase `InaSafeApplication.kt` para que inicialice el cliente de Supabase utilizando las variables del `BuildConfig` en lugar de las cadenas de texto codificadas.

    *Código corregido en `InaSafeApplication.kt`:*
    ```kotlin
    supabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    )
    ```

### 4. Resultado Final

Tras la corrección, el prototipo final de INASAFE es significativamente más seguro. Las credenciales sensibles ya no forman parte del código fuente, eliminando el riesgo de exposición y cumpliendo con las directrices de OWASP e ISO 27000. Este proceso demuestra una madurez en el ciclo de desarrollo, donde la seguridad no es una idea de último momento, sino una parte integral de la construcción de software de calidad.
