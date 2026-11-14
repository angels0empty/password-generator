package passwordgenerator;

import java.security.SecureRandom;

public class PasswordGenerator {
    private static final String EN_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String EN_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String RU_LOWER = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    private static final String RU_UPPER = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{};:,.<>?/";

    private final SecureRandom random = new SecureRandom();

    public String generate(int length, String language, boolean lower, boolean upper,
                           String digits, boolean special) {
        String alphabet = buildAlphabet(language, lower, upper, digits, special);
        if (alphabet.isEmpty()) {
            throw new IllegalArgumentException("Пустой алфавит");
        }
        StringBuilder password = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            password.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return password.toString();
    }

    private String buildAlphabet(String language, boolean lower, boolean upper,
                                 String digits, boolean special) {
        StringBuilder alphabet = new StringBuilder();
        String selected = language == null ? "" : language.trim().toLowerCase();
        if (lower && (selected.equals("en") || selected.equals("both"))) {
            alphabet.append(EN_LOWER);
        }
        if (upper && (selected.equals("en") || selected.equals("both"))) {
            alphabet.append(EN_UPPER);
        }
        if (lower && (selected.equals("ru") || selected.equals("both"))) {
            alphabet.append(RU_LOWER);
        }
        if (upper && (selected.equals("ru") || selected.equals("both"))) {
            alphabet.append(RU_UPPER);
        }
        if (digits != null && !digits.isEmpty()) {
            alphabet.append(digits);
        }
        if (special) {
            alphabet.append(SPECIAL);
        }
        return alphabet.toString();
    }
}
