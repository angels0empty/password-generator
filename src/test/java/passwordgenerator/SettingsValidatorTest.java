package passwordgenerator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Тесты проверки пользовательских настроек. */
@DisplayName("SettingsValidator Test")
class SettingsValidatorTest {

    @ParameterizedTest
    @CsvSource({"' 16 ', 16", "8, 8", "64, 64"})
    @DisplayName("Parses Valid Length")
    void parsesValidLength(String value, int expectedLength) {
        // Execution
        int length = SettingsValidator.parseLength(value);

        // Assertion
        assertThat(length).isEqualTo(expectedLength);
    }

    @ParameterizedTest
    @MethodSource("invalidLengths")
    @DisplayName("Rejects Invalid Length")
    void rejectsInvalidLength(String value) {
        assertThatThrownBy(() -> SettingsValidator.parseLength(value))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** Некорректные длины: буквы, пустота, отсутствие значения. */
    static Stream<String> invalidLengths() {
        return Stream.of("abc", "", null);
    }

    @Test
    @DisplayName("Empty Digits Set Is Rejected When Digits Enabled")
    void emptyDigitsSetIsRejectedWhenDigitsEnabled() {
        // Execution
        String error = SettingsValidator.validate(true, false, true, true, true, "");

        // Assertion
        assertThat(error).isNotNull();
    }

    @Test
    @DisplayName("Empty Digits Set Is Allowed When Digits Disabled")
    void emptyDigitsSetIsAllowedWhenDigitsDisabled() {
        // Execution
        String error = SettingsValidator.validate(true, false, true, true, false, "");

        // Assertion
        assertThat(error).isNull();
    }

    @Test
    @DisplayName("Language Is Required When Letters Used")
    void languageIsRequiredWhenLettersUsed() {
        // Execution
        String error = SettingsValidator.validate(false, false, true, false, false, "");

        // Assertion
        assertThat(error).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("digitsOnlySettings")
    @DisplayName("Digits Work Without Language")
    void digitsWorkWithoutLanguage(boolean digitsEnabled, String digits) {
        // Execution
        String error = SettingsValidator.validate(false, false, false, false, digitsEnabled, digits);

        // Assertion
        assertThat(error).isNull();
    }

    /** Настройки "только цифры": галочка включена с набором и выключена совсем. */
    static Stream<Arguments> digitsOnlySettings() {
        return Stream.of(
                Arguments.of(true, "12"),
                Arguments.of(false, "")
        );
    }
}
