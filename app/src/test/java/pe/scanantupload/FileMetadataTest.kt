package pe.scanantupload

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test

class FileMetadataTest {
    @Test fun readsFieldsFromObservedPageStructure() {
        val description = Jsoup.parse("""
            <div id="fileDescription">
              <p class="requested">You have requested the file:</p>
              <p><span>Name: </span>Cielo rojo sobre Glasgow - Alan Parks.epub</p>
              <p><span>Size: </span>0.30 MB</p>
              <p><span>Uploaded: </span>11/11/2026</p>
            </div>
        """.trimIndent()).selectFirst("#fileDescription")!!
        assertEquals("Cielo rojo sobre Glasgow - Alan Parks.epub", extractField(description, "Name"))
        assertEquals("0.30 MB", extractField(description, "Size"))
        assertEquals("11/11/2026", extractField(description, "Uploaded"))
        assertEquals("", extractField(description, "Missing"))
    }
}
