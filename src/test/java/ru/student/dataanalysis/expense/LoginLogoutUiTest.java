package ru.student.dataanalysis.expense;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import ru.student.dataanalysis.expense.service.AppUserService;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:practice8_ui;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
class LoginLogoutUiTest {

    @LocalServerPort private int port;
    @Autowired private AppUserService userService;

    @Test
    void userCanSignInAndSignOutInBrowser() {
        String login = "ui-" + UUID.randomUUID();
        String password = "test-password";
        userService.addUser(login, password);

        if (System.getProperty("webdriver.chrome.driver") == null) {
            WebDriverManager.chromedriver().setup();
        }
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(options);

        try {
            String baseUrl = "http://localhost:" + port;
            String homeUrl = baseUrl + "/custom/expenses/view/list";
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            driver.get(baseUrl + "/login");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(login);
            driver.findElement(By.name("password")).sendKeys(password);
            driver.findElement(By.cssSelector("form[action='/login'] [type='submit']")).click();

            wait.until(ExpectedConditions.urlToBe(homeUrl));
            assertEquals("Список трат", driver.findElement(By.tagName("h1")).getText());
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("form[action='/logout'] [type='submit']"))).click();

            wait.until(ExpectedConditions.urlToBe(baseUrl + "/login?logout"));
            assertEquals("Вход", driver.findElement(By.tagName("h1")).getText());

            driver.get(homeUrl);
            wait.until(ExpectedConditions.urlToBe(baseUrl + "/login"));
        } finally {
            driver.quit();
        }
    }
}
