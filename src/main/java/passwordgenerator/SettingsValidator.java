package passwordgenerator;

/** Проверяет значения, введённые пользователем. */
public final class SettingsValidator {
    private SettingsValidator() {
    }

    /**
     * Преобразует текстовую длину в целое число.
     *
     * @param value введенная длина
     * @return длина пароля
     * @throws IllegalArgumentException если значение не является числом
     */
    public static int parseLength(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException | NullPointerException exception) {
            throw new IllegalArgumentException("Неверная длина", exception);
        }
    }

    /**
     * Проверяет связь языков, регистров и набора цифр.
     *
     * @param english выбран английский язык
     * @param russian выбран русский язык
     * @param lower используются строчные буквы
     * @param upper используются прописные буквы
     * @param digitsEnabled включены цифры
     * @param digits набор цифр
     * @return текст ошибки или {@code null}, если настройки корректны
     */
    public static String validate(boolean english, boolean russian, boolean lower, boolean upper,
                                  boolean digitsEnabled, String digits) {
        if ((lower || upper) && !english && !russian) {
            return "Выберите язык";
        }
        if (digitsEnabled && (digits == null || digits.trim().isEmpty())) {
            return "Пустой набор цифр";
        }
        return null;
    }
}
