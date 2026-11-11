# Scan AntUpload

Aplicación Android para comprobar enlaces e identificadores y descargar archivos autorizados.

## Uso
Pega hasta 100 enlaces o identificadores, uno por línea, o genera un lote hipotético de 100 identificadores de ocho caracteres. Pulsa Analizar enlaces y selecciona los resultados. La generación no garantiza que los identificadores existan. El botón Detener interrumpe el análisis entre solicitudes; también se detiene ante HTTP 403 o 429.

El filtro usa la extensión y no comprueba el contenido del archivo. Los resultados se conservan en el dispositivo hasta iniciar un análisis nuevo o pulsar Borrar resultados guardados. Descargar seleccionados envía las tareas al gestor de Android, que muestra sus propias notificaciones.

## Compilación
Android Studio con JDK 17, SDK 35 y Gradle 8.9. El flujo Android APK de GitHub Actions genera un APK debug descargable como artefacto.

## Límites
No hay ejecución de millones de combinaciones, búsqueda en segundo plano ni exportación. La app analiza secuencialmente hasta 100 entradas por lote. No garantiza reanudación de descargas, detección de contenido ni acceso continuo al sitio. Descargas y análisis no han sido probados en un dispositivo físico.
