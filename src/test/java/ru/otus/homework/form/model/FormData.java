package ru.otus.homework.form.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Данные для заполнения large_form.
 * Логин и пароль передаются параметрами при старте ({@code -Dlogin}, {@code -Dpassword}),
 * либо через переменные окружения / {@code credentials.properties}.
 */
public final class FormData {

    private static final Logger log = LogManager.getLogger(FormData.class);

    /** Значения по умолчанию для запуска из IDE, если параметры не переданы. */
    private static final String DEFAULT_LOGIN = "qa.user";
    private static final String DEFAULT_PASSWORD = "Secret123!";

    /**
     * После Submit на странице показывается {@code value} опции, не видимый текст.
     * Ключ — код или название, значение — атрибут value из {@code <select id="country">}.
     */
    private static final Map<String, String> COUNTRY_VALUES = Map.ofEntries(
            Map.entry("ar", "ar"),
            Map.entry("argentina", "ar"),
            Map.entry("de", "de"),
            Map.entry("germany", "de"),
            Map.entry("in", "in"),
            Map.entry("india", "in"),
            Map.entry("us", "us"),
            Map.entry("united states", "us")
    );

    private static final Properties FILE_CREDENTIALS = loadCredentialsFile();

    private final String firstName;
    private final String lastName;
    private final String email;
    private final String password;
    private final String passwordConfirm;
    private final String nickname;
    private final String birthday;
    private final String age;
    private final String country;
    private final List<String> languages;
    private final String bio;
    private final String contact;
    private final List<String> interests;
    private final String experience;
    private final String color;
    private final boolean acceptTerms;

    private FormData(Builder builder) {
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.password = builder.password;
        this.passwordConfirm = builder.passwordConfirm;
        this.nickname = builder.nickname;
        this.birthday = builder.birthday;
        this.age = builder.age;
        this.country = builder.country;
        this.languages = List.copyOf(builder.languages);
        this.bio = builder.bio;
        this.contact = builder.contact;
        this.interests = List.copyOf(builder.interests);
        this.experience = builder.experience;
        this.color = builder.color;
        this.acceptTerms = builder.acceptTerms;
    }

    public static FormData fromSystemProperties() {
        String login = resolveCredential("login", "LOGIN", DEFAULT_LOGIN);
        String password = resolveCredential("password", "PASSWORD", DEFAULT_PASSWORD);
        String passwordConfirm = firstNonBlank(
                System.getProperty("passwordConfirm"),
                System.getenv("PASSWORD_CONFIRM"),
                FILE_CREDENTIALS.getProperty("passwordConfirm"),
                password
        );

        String email = login.contains("@") ? login : login + "@example.com";

        return builder()
                .firstName("Ekaterina")
                .lastName("Velichko")
                .email(email)
                .password(password)
                .passwordConfirm(passwordConfirm)
                .nickname("Katya")
                .birthday("1995-03-12")
                .age("29")
                // На форме только: ar / de / in / us. После Submit приходит код (de), не "Germany"
                .country(normalizeCountry("de"))
                .languages(List.of("English", "German"))
                .bio("QA automation engineer. Homework DZ5.")
                .contact("email")
                .interests(List.of("testing", "automation"))
                .experience("10")
                .color("#00aa44")
                .acceptTerms(true)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Приводит название или код страны к value из выпадающего списка
     * (именно оно попадает в таблицу после Submit).
     */
    public static String normalizeCountry(String country) {
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Страна не задана");
        }
        String key = country.trim().toLowerCase(Locale.ROOT);
        String value = COUNTRY_VALUES.get(key);
        if (value == null) {
            throw new IllegalArgumentException(
                    "Неизвестная страна '" + country + "'. Допустимо: ar/de/in/us "
                            + "или Argentina/Germany/India/United States"
            );
        }
        return value;
    }

    public void assertPasswordsMatch() {
        if (!Objects.equals(password, passwordConfirm)) {
            throw new IllegalArgumentException(
                    "Пароль и подтверждение пароля не совпадают: password=["
                            + password + "], passwordConfirm=[" + passwordConfirm + "]"
            );
        }
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public String email() {
        return email;
    }

    public String password() {
        return password;
    }

    public String passwordConfirm() {
        return passwordConfirm;
    }

    public String nickname() {
        return nickname;
    }

    public String birthday() {
        return birthday;
    }

    public String age() {
        return age;
    }

    public String country() {
        return country;
    }

    public List<String> languages() {
        return languages;
    }

    public String bio() {
        return bio;
    }

    public String contact() {
        return contact;
    }

    public List<String> interests() {
        return interests;
    }

    public String experience() {
        return experience;
    }

    public String color() {
        return color;
    }

    public boolean acceptTerms() {
        return acceptTerms;
    }

    /**
     * Приоритет: {@code -D} → переменная окружения → credentials.properties → значение по умолчанию.
     */
    private static String resolveCredential(String systemProperty, String envName, String defaultValue) {
        String fromSystem = System.getProperty(systemProperty);
        if (fromSystem != null && !fromSystem.isBlank()) {
            log.info("Параметр {} взят из -D{}", systemProperty, systemProperty);
            return fromSystem.trim();
        }

        String fromEnv = System.getenv(envName);
        if (fromEnv != null && !fromEnv.isBlank()) {
            log.info("Параметр {} взят из переменной окружения {}", systemProperty, envName);
            return fromEnv.trim();
        }

        String fromFile = FILE_CREDENTIALS.getProperty(systemProperty);
        if (fromFile != null && !fromFile.isBlank()) {
            log.info("Параметр {} взят из credentials.properties", systemProperty);
            return fromFile.trim();
        }

        log.info(
                "Параметр {} не передан — используется значение по умолчанию. "
                        + "Для сдачи/проверки задайте -D{}=... или VM options в IDEA",
                systemProperty,
                systemProperty
        );
        return defaultValue;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static Properties loadCredentialsFile() {
        Properties properties = new Properties();
        try (InputStream in = FormData.class.getClassLoader().getResourceAsStream("credentials.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            log.warn("Не удалось прочитать credentials.properties: {}", e.getMessage());
        }
        return properties;
    }

    public static final class Builder {
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private String passwordConfirm;
        private String nickname;
        private String birthday;
        private String age;
        private String country;
        private List<String> languages = List.of();
        private String bio;
        private String contact;
        private List<String> interests = List.of();
        private String experience;
        private String color;
        private boolean acceptTerms;

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder passwordConfirm(String passwordConfirm) {
            this.passwordConfirm = passwordConfirm;
            return this;
        }

        public Builder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder birthday(String birthday) {
            this.birthday = birthday;
            return this;
        }

        public Builder age(String age) {
            this.age = age;
            return this;
        }

        public Builder country(String country) {
            this.country = normalizeCountry(country);
            return this;
        }

        public Builder languages(List<String> languages) {
            this.languages = languages;
            return this;
        }

        public Builder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public Builder contact(String contact) {
            this.contact = contact;
            return this;
        }

        public Builder interests(List<String> interests) {
            this.interests = interests;
            return this;
        }

        public Builder experience(String experience) {
            this.experience = experience;
            return this;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public Builder acceptTerms(boolean acceptTerms) {
            this.acceptTerms = acceptTerms;
            return this;
        }

        public FormData build() {
            return new FormData(this);
        }
    }
}
