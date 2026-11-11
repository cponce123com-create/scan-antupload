# Scan AntUpload

Aplicación Android para analizar enlaces e identificadores y descargar archivos autorizados.

## Uso
El escáner automático genera lotes de 100 identificadores alfanuméricos de ocho caracteres y los consulta con hasta cinco solicitudes simultáneas. Continúa con el siguiente lote hasta que se pulse Detener, se destruya la actividad o el servidor responda 403 o 429. La generación aleatoria no garantiza encontrar archivos ni evita repetir identificadores ya probados.

La velocidad real se muestra en consultas por segundo y depende del servidor y la conexión.

También se pueden pegar hasta 100 enlaces o identificadores para analizarlos manualmente. Los resultados encontrados que coinciden con el filtro se conservan entre lotes y al cerrar la app, hasta que el usuario los borre. El filtro comprueba solamente la extensión. Las descargas seleccionadas se envían al gestor de Android.

## Compilación
Android Studio con JDK 17, SDK 35 y Gradle 8.9. El flujo Android APK de GitHub Actions genera un APK debug como artefacto.

## Límites
El escaneo funciona mientras la actividad esté activa, no en segundo plano. No conserva una lista de todas las combinaciones probadas, no evita repeticiones aleatorias y no garantiza reanudación de descargas ni validez del contenido. La disponibilidad del servidor puede cambiar. Requiere pruebas en un dispositivo real.
