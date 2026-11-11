package ru.otus.homework.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import ru.otus.homework.form.driver.WebDriverFactory;
import ru.otus.homework.form.model.FormData;
import ru.otus.homework.form.pages.FormResultPage;
import ru.otus.homework.form.pages.LargeFormPage;

class LargeFormTest {

    private static final Logger log = LogManager.getLogger(LargeFormTest.class);

    @Test
    @DisplayName("Заполнение large_form: совпадение паролей, отправка и проверка данных на странице")
    void fillsFormValidatesPasswordAndShowsSubmittedData() {
        FormData data = FormData.fromSystemProperties();

        log.info("Проверяю совпадение пароля и подтверждения");
        data.assertPasswordsMatch();
        log.info("Пароль и подтверждение совпадают");

        WebDriver driver = WebDriverFactory.create();
        try {
            LargeFormPage form = new LargeFormPage(driver).open();
            FormResultPage result = form.fillAllFields(data).submit();

            assertThat(result.title()).isEqualTo("Form Submitted");

            Map<String, String> submitted = result.submittedValues();
            log.info("Сверяю значения на странице результата");

            assertThat(submitted)
                    .containsEntry("first_name", data.firstName())
                    .containsEntry("last_name", data.lastName())
                    .containsEntry("email", data.email())
                    .containsEntry("nickname", data.nickname())
                    .containsEntry("birthday", data.birthday())
                    .containsEntry("age", data.age())
                    .containsEntry("country", data.country())
                    .containsEntry("languages", join(data.languages()))
                    .containsEntry("bio", data.bio())
                    .containsEntry("contact", data.contact())
                    .containsEntry("interests", join(data.interests()))
                    .containsEntry("experience", data.experience())
                    .containsEntry("color", data.color().toLowerCase())
                    .containsEntry("terms", "accepted")
                    .containsEntry("form_id", "large-form");

            // Пароль на SUT маскируется звёздочками — проверяем, что поле есть и не пустое.
            assertThat(submitted.get("password")).isNotBlank().matches("\\*+");

            log.info("Тест успешно завершён: данные формы корректно отображены после Submit");
        } catch (RuntimeException | AssertionError error) {
            saveScreenshot(driver, "large-form-submit");
            throw error;
        } finally {
            driver.quit();
            WebDriverFactory.shutdownLocalService();
        }
    }

    @Test
    @DisplayName("Пароль и подтверждение не совпадают — отправка не выполняется")
    void rejectsMismatchedPasswordConfirmation() {
        FormData data = FormData.builder()
                .firstName("Ekaterina")
                .lastName("Velichko")
                .email("qa.user@example.com")
                .password("Secret123!")
                .passwordConfirm("OtherPassword!")
                .build();

        log.info("Ожидаю ошибку при несовпадении пароля и подтверждения");
        assertThatThrownBy(data::assertPasswordsMatch)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("не совпадают");
        log.info("Несовпадение паролей корректно обнаружено до заполнения формы");
    }

    private static String join(java.util.List<String> values) {
        return values.stream().collect(Collectors.joining(", "));
    }

    private void saveScreenshot(WebDriver driver, String name) {
        if (!(driver instanceof TakesScreenshot screenshot)) {
            return;
        }
        try {
            Path directory = Path.of("target", "screenshots");
            Files.createDirectories(directory);
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            Path file = directory.resolve(name + "-" + stamp + ".png");
            Files.write(file, screenshot.getScreenshotAs(OutputType.BYTES));
            log.warn("Скриншот сохранён: {}", file.toAbsolutePath());
        } catch (IOException | RuntimeException e) {
            log.warn("Не удалось сохранить скриншот: {}", e.getMessage());
        }
    }
}
