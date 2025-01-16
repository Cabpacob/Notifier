package notifier.impl

import notifier.api.SiteParser
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DescribeNonEUDrivingLicenseParser : SiteParser<List<LocalDate>> {
    override val url: String
        get() = "https://termin.bremen.de/termine/suggest"


    override fun parse(body: String): List<LocalDate> {
        return Jsoup
            .parseBodyFragment(body)
            .body()
            .getElementById("sugg_accordion")
            ?.children()
            .let { it ?: return emptyList() }
            .asSequence()
            .filter { it.nodeName() == "h3" }
            .map { it.text() }
            .map { date -> date.dropWhile { it != ' ' }.drop(1) }
            .map {
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                LocalDate.parse(it, formatter)
            }
            .toList()
    }
}
