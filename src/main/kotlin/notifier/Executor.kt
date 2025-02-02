package notifier

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import notifier.api.*

open class Executor<T>(
    private val parser: SiteParser<T>,
    private val filter: NotificationFilter<T>,
    private val notifier: Notifier<T>,
    private val cooldown: Long,
    private val logger: Logger<T>,
    private val sessionConfiguration: SessionConfiguration? = null
) {
    private val client = HttpClient()

    fun run() {
        var prevBody: String? = null
        var prevData: T? = null

        while (true) {
            try {
                val body = runBlocking {
                    client.get(parser.url) {
                        setCookie()
                    }.bodyAsText()
                }

                logger.onBodyReceived(body)

                if (body == prevBody) continue
                logger.onBodyChanged(prevBody, body)
                prevBody = body

                val data = parser.parse(body)
                if (data == prevData) continue
                logger.onDataChanged(prevData, data)

                if (filter.shouldNotify(prevData, data))
                    notifier.notify(prevData, data)

                prevData = data
            } catch (e: Exception) {
                logger.onError(e)
            } finally {
                Thread.sleep(cooldown)
            }
        }
    }

    protected open suspend fun getCookies(): List<Cookie> {
        sessionConfiguration ?: return emptyList()

        val response = client.get(sessionConfiguration.cookieProviderUrl)
        return response.setCookie()
    }

    private suspend fun HttpRequestBuilder.setCookie() {
        sessionConfiguration ?: return

        val cookie = getCookies()
            .findLast { it.name == sessionConfiguration.cookieName }
        checkNotNull(cookie) { "Specified session configuration does not specify correct session" }
        cookie(cookie.name, cookie.value)
    }
}
