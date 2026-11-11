package ru.otus.homework.form.pages;

import java.time.Duration;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.otus.homework.form.model.FormData;

/**
 * Страница https://the-internet.herokuapp.com/large_form
 */
public class LargeFormPage {

    public static final String URL = "https://the-internet.herokuapp.com/large_form";

    private static final Logger log = LogManager.getLogger(LargeFormPage.class);

    private static final By FIRST_NAME = By.id("first-name");
    private static final By LAST_NAME = By.id("last-name");
    private static final By EMAIL = By.name("email");
    private static final By PASSWORD = By.name("password");
    private static final By NICKNAME = By.name("nickname");
    private static final By BIRTHDAY = By.id("birthday");
    private static final By AGE = By.name("age");
    private static final By COUNTRY = By.id("country");
    private static final By LANGUAGES = By.name("languages[]");
    private static final By BIO = By.id("bio");
    private static final By EXPERIENCE = By.id("experience");
    private static final By COLOR = By.name("color");
    private static final By TERMS = By.id("terms");
    private static final By SUBMIT = By.id("submit");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public LargeFormPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public LargeFormPage open() {
        log.info("Открываю форму: {}", URL);
        driver.get(URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(FIRST_NAME));
        return this;
    }

    public LargeFormPage fillAllFields(FormData data) {
        log.info("Заполняю все поля формы для email={}", data.email());

        type(FIRST_NAME, data.firstName());
        type(LAST_NAME, data.lastName());
        type(EMAIL, data.email());
        type(PASSWORD, data.password());
        type(NICKNAME, data.nickname());
        setInputValue(BIRTHDAY, data.birthday());
        type(AGE, data.age());

        selectCountry(data.country());

        Select languages = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(LANGUAGES)));
        languages.deselectAll();
        for (String language : data.languages()) {
            languages.selectByVisibleText(language);
        }

        type(BIO, data.bio());

        WebElement contactRadio = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[name='contact'][value='" + data.contact() + "']")
        ));
        scrollIntoView(contactRadio);
        contactRadio.click();

        List<WebElement> interestBoxes = driver.findElements(By.cssSelector("input.interest"));
        for (WebElement box : interestBoxes) {
            String value = box.getAttribute("value");
            boolean shouldBeSelected = data.interests().contains(value);
            if (box.isSelected() != shouldBeSelected) {
                scrollIntoView(box);
                box.click();
            }
        }

        setRangeValue(data.experience());
        setColor(data.color());

        WebElement terms = wait.until(ExpectedConditions.elementToBeClickable(TERMS));
        if (data.acceptTerms() != terms.isSelected()) {
            scrollIntoView(terms);
            terms.click();
        }

        log.info("Все поля формы заполнены");
        return this;
    }

    public FormResultPage submit() {
        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(SUBMIT));
        scrollIntoView(submit);
        log.info("Нажимаю Submit");
        submit.click();
        return new FormResultPage(driver).waitUntilLoaded();
    }

    private void type(By locator, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        scrollIntoView(element);
        element.clear();
        element.sendKeys(value);
    }

    /**
     * Доступные value на SUT: {@code ar}, {@code de}, {@code in}, {@code us}.
     * Сначала value, иначе видимый текст (Argentina, Germany, India, United States).
     */
    private void selectCountry(String country) {
        Select select = new Select(wait.until(ExpectedConditions.elementToBeClickable(COUNTRY)));
        try {
            select.selectByValue(country);
        } catch (org.openqa.selenium.NoSuchElementException ignored) {
            select.selectByVisibleText(country);
        }
    }

    private void setRangeValue(String value) {
        setInputValue(EXPERIENCE, value);
    }

    private void setColor(String color) {
        setInputValue(COLOR, color);
    }

    /**
     * Устанавливает value через JS — надёжнее для date/range/color, чем sendKeys.
     */
    private void setInputValue(By locator, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        scrollIntoView(element);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];"
                        + "arguments[0].dispatchEvent(new Event('input', {bubbles:true}));"
                        + "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                element,
                value
        );
    }

    private void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'center'});",
                element
        );
    }
}
