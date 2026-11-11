package pe.scanantupload

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.widget.*
import org.jsoup.Jsoup
import java.net.URI
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private lateinit var input: EditText
    private lateinit var status: TextView
    private lateinit var list: LinearLayout
    private lateinit var analyze: Button
    private lateinit var onlyBooks: CheckBox
    private val results = mutableListOf<Book>()

    data class Book(val title: String, val size: String, val date: String, val download: String, val page: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(12))
            setBackgroundColor(Color.rgb(247, 248, 253))
        }
        val title = TextView(this).apply {
            text = "Scan AntUpload"; textSize = 26f
            setTextColor(Color.rgb(28, 37, 74)); setTypeface(null, Typeface.BOLD)
        }
        root.addView(title)
        root.addView(TextView(this).apply {
            text = "Analiza enlaces de libros y organiza tus descargas"
            setTextColor(Color.DKGRAY); textSize = 14f; setPadding(0, dp(4), 0, dp(16))
        })
        input = EditText(this).apply {
            hint = "Pega enlaces o identificadores, uno por línea"
            minLines = 3; maxLines = 6; gravity = Gravity.TOP
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
        root.addView(input, LinearLayout.LayoutParams(-1, -2))
        onlyBooks = CheckBox(this).apply { text = "Mostrar solo EPUB, PDF y MOBI"; isChecked = true }
        root.addView(onlyBooks)
        analyze = Button(this).apply { text = "Analizar enlaces"; setOnClickListener { scan() } }
        root.addView(analyze, LinearLayout.LayoutParams(-1, -2))
        root.addView(Button(this).apply { text = "Descargar seleccionados"; setOnClickListener { downloadSelected() } })
        status = TextView(this).apply { text = "Introduce hasta 100 enlaces o identificadores."; setPadding(0, dp(8), 0, dp(8)) }
        root.addView(status)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(list) }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun pageFor(line: String): String? {
        val value = line.trim()
        if (value.matches(Regex("^[A-Za-z0-9]{1,64}$"))) return "https://www.antupload.com/file/$value/"
        return try {
            val uri = URI(value)
            if (uri.scheme != "https" || uri.host?.lowercase() !in setOf("antupload.com", "www.antupload.com") ||
                uri.userInfo != null || uri.port != -1 || !uri.path.matches(Regex("^/file/[A-Za-z0-9]{1,64}/?$")) ||
                uri.query != null || uri.fragment != null) null
            else "https://www.antupload.com" + uri.path
        } catch (_: Exception) { null }
    }
    private fun scan() {
        val lines = input.text.toString().lines().map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (lines.isEmpty() || lines.size > 100) { status.text = "Introduce entre 1 y 100 enlaces o identificadores."; return }
        val filter = onlyBooks.isChecked
        results.clear(); list.removeAllViews(); analyze.isEnabled = false
        status.text = "Analizando 0/${lines.size}..."
        worker.execute {
            var failures = 0
            lines.forEachIndexed { index, line ->
                try {
                    val page = pageFor(line) ?: throw IllegalArgumentException("Enlace no válido")
                    val response = Jsoup.connect(page).timeout(15000).followRedirects(false).execute()
                    if (response.statusCode() != 200) throw IllegalStateException("Respuesta inesperada")
                    val doc = response.parse()
                    val desc = doc.selectFirst("#fileDescription") ?: throw IllegalStateException("Archivo no encontrado o página no reconocida")
                    fun field(label: String): String = desc.select("p").firstOrNull { it.selectFirst("span")?.text()?.trim()?.equals("$label:", true) == true }
                        ?.ownText()?.trim().orEmpty()
                    val title = field("Name")
                    val href = doc.selectFirst("#fileDownload a#downloadB")?.attr("abs:href").orEmpty()
                    val link = URI(href)
                    if (title.isBlank() || link.scheme != "https" ||
                        link.host?.lowercase() !in setOf("antupload.com", "www.antupload.com") ||
                        !link.path.startsWith("/filed/") || link.userInfo != null || link.port != -1) throw IllegalStateException("Datos de descarga no reconocidos")
                    val book = Book(title, field("Size"), field("Uploaded"), href, page)
                    if (!filter || title.substringAfterLast('.', "").lowercase() in setOf("epub", "pdf", "mobi")) {
                        runOnUiThread { if (!isFinishing && !isDestroyed) { results.add(book); addBook(book) } }
                    }
                } catch (_: Exception) { failures++ }
                val count = index + 1; val errors = failures
                runOnUiThread { if (!isFinishing && !isDestroyed) status.text = "Analizados: $count/${lines.size} · Errores o no disponibles: $errors" }
                if (index < lines.lastIndex) Thread.sleep(500)
            }
            runOnUiThread { if (!isFinishing && !isDestroyed) analyze.isEnabled = true }
        }
    }
    private fun addBook(book: Book) {
        val box = CheckBox(this).apply {
            text = "${book.title}\n${book.size} · Subido: ${book.date}"
            textSize = 15f; setPadding(dp(4), dp(8), dp(4), dp(8))
            tag = book; isChecked = true
        }
        list.addView(box)
    }
    private fun downloadSelected() {
        var count = 0
        for (i in 0 until list.childCount) {
            val box = list.getChildAt(i) as? CheckBox ?: continue
            if (!box.isChecked) continue
            val book = box.tag as Book
            val safeName = book.title.replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"), "_").take(180)
            try {
                val request = DownloadManager.Request(Uri.parse(book.download))
                    .setTitle(safeName)
                    .setDescription("Descarga desde AntUpload")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName)
                    .addRequestHeader("Referer", book.page)
                (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
                box.isChecked = false
                count++
            } catch (_: Exception) { Toast.makeText(this, "No se pudo iniciar: ${book.title}", Toast.LENGTH_LONG).show() }
        }
        Toast.makeText(this, if (count > 0) "$count descarga(s) enviadas a Android" else "Selecciona al menos un libro", Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() { worker.shutdownNow(); super.onDestroy() }
}
