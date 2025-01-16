package notifier.impl

import io.ktor.http.*
import notifier.Executor
import notifier.api.Logger
import notifier.api.NotificationFilter
import notifier.api.Notifier
import notifier.api.SessionConfiguration
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.lang.Thread.sleep
import java.time.LocalDate

class DrivingLicenseExchangeExecutor(
    filter: NotificationFilter<List<LocalDate>>,
    notifier: Notifier<List<LocalDate>>,
    cooldown: Long,
    logger: Logger<List<LocalDate>>,
    private val sessionConfiguration: SessionConfiguration? = DrivingLicenseSessionConfiguration()
) : Executor<List<LocalDate>>(
    DescribeNonEUDrivingLicenseParser(),
    filter,
    notifier,
    cooldown,
    logger,
    DrivingLicenseSessionConfiguration()
) {
    override suspend fun getCookies(): List<Cookie> {
        sessionConfiguration ?: return emptyList()

        return DrivingLicenseSeleniumCookiesScrapper.getCookies(sessionConfiguration.cookieProviderUrl)
    }
}

private object DrivingLicenseSeleniumCookiesScrapper {

    fun getCookies(url: String): List<Cookie> { //FIXME call some async Selenium implementation
        val driver = ChromeDriver(ChromeOptions().addArguments("--headless"))

        val scrollAndClick = fun WebElement.() {
            (driver as JavascriptExecutor).executeScript("arguments[0].scrollIntoView(true);", this)
            this.click()
            sleep(2000) // 2 seconds
        }

        driver.get(url)

        driver.findElement(By.id("button-plus-9159")).scrollAndClick()
        driver.findElement(By.id("WeiterButton")).scrollAndClick()
        driver.findElement(By.cssSelector("input[value='Fahrerlaubnisse auswählen']")).scrollAndClick()

        val cookies = driver.manage().cookies.map { Cookie(it.name, it.value) }.toList()

        driver.quit()

        return cookies
    }
}
