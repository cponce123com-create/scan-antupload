package pe.scanantupload

import org.jsoup.nodes.Element

internal fun extractField(description: Element, label: String): String =
    description.select("p").firstOrNull {
        it.selectFirst("span")?.text()?.trim()?.equals(label + ":", ignoreCase = true) == true
    }?.ownText()?.trim().orEmpty()
