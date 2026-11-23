package passwordgenerator;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Создает случайные пароли по заданным параметрам. */
public class PasswordGenerator {
    static final String EN_LOWER = "abcdefghijklmnopqrstuvwxyz";
    static final String EN_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final String RU_LOWER = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    static final String RU_UPPER = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
    static final String SPECIAL = "!@#$%^&*()_+-=[]{};:,.<>?/";
    static final int MAX_LENGTH = 1_000_000;

    private final SecureRandom random = new SecureRandom();

    /**
     * Генерирует пароль, гарантируя наличие всех выбранных групп символов.
     *
     * @param length длина пароля
     * @param language язык: en, ru или both
     * @param lower использовать строчные буквы
     * @param upper использовать прописные буквы
     * @param digits разрешенные цифры
     * @param special использовать специальные символы
     * @return сгенерированный пароль
     */
    public String generate(int length, String language, boolean lower, boolean upper,
                           String digits, boolean special) {
        String normalizedDigits = normalizeDigits(digits);
        String selectedLanguage = language == null ? "" : language.trim().toLowerCase();
        validateInput(length, selectedLanguage, lower || upper, normalizedDigits);
        List<String> groups = createGroups(selectedLanguage, lower, upper, normalizedDigits, special);
        String alphabet = groups.stream().collect(Collectors.joining());
        validateAlphabet(length, alphabet, groups.size());

        StringBuilder password = new StringBuilder(length);
        groups.forEach(group -> password.append(randomChar(group)));
        IntStream.range(groups.size(), length).forEach(index -> password.append(randomChar(alphabet)));
        return shuffle(password.toString());
    }

    /** Проверяет исходные значения параметров. */
    private void validateInput(int length, String language, boolean letters, String digits) {
        if (length < 1 || length > MAX_LENGTH) {
            throw new IllegalArgumentException("Длина: 1.." + MAX_LENGTH);
        }
        if (letters && !List.of("en", "ru", "both").contains(language)) {
            throw new IllegalArgumentException("Язык: en/ru/both");
        }
        if (!digits.matches("[0-9]*")) {
            throw new IllegalArgumentException("Только цифры 0-9");
        }
    }

    /** Удаляет повторяющиеся цифры при помощи Stream API. */
    private String normalizeDigits(String digits) {
        if (digits == null) {
            return "";
        }
        return digits.chars().distinct().collect(
                StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    /** Формирует список выбранных групп символов. */
    private List<String> createGroups(String language, boolean lower, boolean upper,
                                      String digits, boolean special) {
        List<String> groups = new ArrayList<>();
        addLetterGroups(groups, language, lower, upper);
        if (!digits.isEmpty()) {
            groups.add(digits);
        }
        if (special) {
            groups.add(SPECIAL);
        }
        return groups;
    }

    /** Добавляет выбранные языки и регистры в список групп. */
    private void addLetterGroups(List<String> groups, String language, boolean lower, boolean upper) {
        if (lower && (language.equals("en") || language.equals("both"))) {
            groups.add(EN_LOWER);
        }
        if (upper && (language.equals("en") || language.equals("both"))) {
            groups.add(EN_UPPER);
        }
        if (lower && (language.equals("ru") || language.equals("both"))) {
            groups.add(RU_LOWER);
        }
        if (upper && (language.equals("ru") || language.equals("both"))) {
            groups.add(RU_UPPER);
        }
    }

    /** Проверяет итоговый алфавит и минимально возможную длину. */
    private void validateAlphabet(int length, String alphabet, int requiredSize) {
        if (alphabet.isEmpty()) {
            throw new IllegalArgumentException("Пустой алфавит");
        }
        if (length < requiredSize) {
            throw new IllegalArgumentException("Слишком короткая длина");
        }
    }

    /** Возвращает случайный символ переданного алфавита. */
    private char randomChar(String alphabet) {
        return alphabet.charAt(random.nextInt(alphabet.length()));
    }

    /** Перемешивает символы алгоритмом Фишера - Йетса. */
    private String shuffle(String password) {
        char[] result = password.toCharArray();
        for (int index = result.length - 1; index > 0; index--) {
            int other = random.nextInt(index + 1);
            char temporary = result[index];
            result[index] = result[other];
            result[other] = temporary;
        }
        return new String(result);
    }
}
