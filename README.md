# Scan AntUpload

Primera versión Android para comprobar enlaces o identificadores proporcionados y descargar libros con autorización.

## Uso
Pega hasta 100 enlaces o identificadores, uno por línea; analiza y selecciona los resultados. Las descargas se entregan al gestor de Android y aparecen en Descargas y sus notificaciones. El filtro utiliza la extensión y no comprueba el contenido del archivo.

## Compilación
Usa Android Studio con JDK 17 y SDK 35, y Gradle 8.9. En GitHub Actions, el flujo Android APK genera un APK debug descargable como artefacto. No está firmado para distribución.

## Estado y límites
El análisis se hace secuencialmente y procesa hasta 100 entradas por lote, no genera combinaciones. La disponibilidad de AntUpload y sus políticas pueden cambiar. Las descargas no se han probado en dispositivos físicos; Android administra la cola y la reanudación según sus capacidades. La app no almacena historial después de cerrar ni valida la integridad del archivo. Al girar la pantalla los resultados se reinician.
