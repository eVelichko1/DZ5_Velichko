package ru.otus.homework.form.model;

import java.util.List;
import java.util.Objects;

/**
 * Данные для заполнения large_form.
 * Логин и пароль приходят из системных свойств при старте теста.
 */
public final class FormData {

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
        String login = requiredProperty("login");
        String password = requiredProperty("password");
        String passwordConfirm = System.getProperty("passwordConfirm");
        if (passwordConfirm == null || passwordConfirm.isBlank()) {
            passwordConfirm = password;
        }

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
                .country("de")
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

    private static String requiredProperty(String name) {
        String value = System.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Не задан обязательный параметр -D" + name
                            + ". Пример: mvn test -Dlogin=user -Dpassword=Secret123"
            );
        }
        return value.trim();
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
            this.country = country;
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
