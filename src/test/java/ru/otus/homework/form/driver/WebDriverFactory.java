package ru.otus.homework.form.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Фабрика {@link RemoteWebDriver}.
 * <p>
 * По умолчанию поднимает локальный ChromeDriver (Selenium Standalone-режим сессии)
 * через WebDriverManager — скачивать драйвер вручную не нужно.
 * <p>
 * Для Selenium Grid / внешнего Standalone передайте URL:
 * {@code -Dselenium.remote.url=http://localhost:4444/wd/hub}
 */
public final class WebDriverFactory {

    private static final Logger log = LogManager.getLogger(WebDriverFactory.class);

    private static ChromeDriverService localService;

    private WebDriverFactory() {
    }

    public static WebDriver create() {
        ChromeOptions options = chromeOptions();
        String remoteUrl = System.getProperty("selenium.remote.url", "").trim();

        RemoteWebDriver driver;
        if (!remoteUrl.isEmpty()) {
            log.info("RemoteWebDriver → Selenium Grid/Standalone: {}", remoteUrl);
            driver = new RemoteWebDriver(toUrl(remoteUrl), options);
        } else {
            log.info("RemoteWebDriver → локальный ChromeDriver (WebDriverManager)");
            driver = createLocalRemoteDriver(options);
        }

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(40));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(20));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        log.info("Сессия WebDriver создана: {}", driver.getSessionId());
        return driver;
    }

    /**
     * Останавливает локальный ChromeDriverService, если он был запущен фабрикой.
     */
    public static void shutdownLocalService() {
        if (localService != null) {
            log.info("Остановка локального ChromeDriverService");
            localService.stop();
            localService = null;
        }
    }

    private static RemoteWebDriver createLocalRemoteDriver(ChromeOptions options) {
        WebDriverManager.chromedriver().setup();
        localService = new ChromeDriverService.Builder()
                .usingAnyFreePort()
                .build();
        try {
            localService.start();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось запустить ChromeDriverService", e);
        }
        log.info("Локальный ChromeDriverService: {}", localService.getUrl());
        return new RemoteWebDriver(localService.getUrl(), options);
    }

    private static ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless=new",
                "--window-size=1920,1080",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--no-first-run",
                "--no-default-browser-check",
                "--disable-search-engine-choice-screen",
                "--lang=ru"
        );
        return options;
    }

    private static java.net.URL toUrl(String remoteUrl) {
        try {
            return URI.create(remoteUrl).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Некорректный selenium.remote.url: " + remoteUrl, e);
        }
    }
}
