package ru.otus.homework.form.pages;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Страница результата после отправки large_form.
 */
public class FormResultPage {

    private static final Logger log = LogManager.getLogger(FormResultPage.class);

    private static final By TITLE = By.cssSelector(".example h3");
    private static final By TABLE = By.id("submitted-data");
    private static final By ROWS = By.cssSelector("#submitted-data tbody tr");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public FormResultPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public FormResultPage waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(TABLE));
        wait.until(ExpectedConditions.textToBe(TITLE, "Form Submitted"));
        log.info("Страница результата загружена");
        return this;
    }

    public String title() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(TITLE)).getText().trim();
    }

    public Map<String, String> submittedValues() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(TABLE));
        Map<String, String> values = new LinkedHashMap<>();
        for (WebElement row : driver.findElements(ROWS)) {
            var cells = row.findElements(By.tagName("td"));
            if (cells.size() >= 2) {
                values.put(cells.get(0).getText().trim(), cells.get(1).getText().trim());
            }
        }
        log.info("Получены отправленные данные: {}", values);
        return values;
    }

    public String valueOf(String field) {
        String value = submittedValues().get(field);
        if (value == null) {
            throw new AssertionError("На странице результата нет поля: " + field);
        }
        return value;
    }
}
