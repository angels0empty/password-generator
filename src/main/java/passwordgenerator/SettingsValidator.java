package passwordgenerator;

public final class SettingsValidator {
    private SettingsValidator() {
    }

    public static int parseLength(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException | NullPointerException exception) {
            throw new IllegalArgumentException("Неверная длина", exception);
        }
    }

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
