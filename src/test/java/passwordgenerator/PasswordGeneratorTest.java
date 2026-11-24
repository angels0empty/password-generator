package passwordgenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

/** Тесты алгоритма генерации паролей. */
@DisplayName("PasswordGenerator Test")
class PasswordGeneratorTest {
    private PasswordGenerator generator;

    @BeforeEach
    void setup() {
        generator = new PasswordGenerator();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("Generates Requested Length For Every Profile")
    void generatesRequestedLengthForEveryProfile(int profile) {
        // Setup
        int length = 100;

        // Execution
        String password = Benchmark.generateProfile(length, profile);

        // Assertion
        assertThat(password).hasSize(length);
    }

    @ParameterizedTest
    @CsvSource({
            "'en', false, false, '111333', '[13]+'",
            "'', false, false, '7', '7+'"
    })
    @DisplayName("Uses Only Selected Digits")
    void usesOnlySelectedDigits(String language, boolean lower, boolean upper, String digits, String pattern) {
        // Execution
        String password = generator.generate(100, language, lower, upper, digits, false);

        // Assertion
        assertThat(password).matches(pattern);
    }

    @Test
    @DisplayName("Contains All Required Groups")
    void containsAllRequiredGroups() {
        // Execution
        String password = generator.generate(100, "both", true, true, "0123456789", true);

        // Assertion
        assertAll(
                () -> assertThat(hasAny(password, PasswordGenerator.EN_LOWER)).isTrue(),
                () -> assertThat(hasAny(password, PasswordGenerator.EN_UPPER)).isTrue(),
                () -> assertThat(hasAny(password, PasswordGenerator.RU_LOWER)).isTrue(),
                () -> assertThat(hasAny(password, PasswordGenerator.RU_UPPER)).isTrue(),
                () -> assertThat(hasAny(password, "0123456789")).isTrue(),
                () -> assertThat(hasAny(password, PasswordGenerator.SPECIAL)).isTrue()
        );
    }

    @Test
    @DisplayName("Handles Large Length And Repeated Generation")
    void handlesLargeLengthAndRepeatedGeneration() {
        // Execution
        String first = generator.generate(1_000_000, "en", true, false, "", false);
        String second = generator.generate(100, "en", true, false, "", false);
        String third = generator.generate(100, "en", true, false, "", false);

        // Assertion
        assertThat(first).hasSize(1_000_000);
        assertThat(second).isNotEqualTo(third);
    }

    @Test
    @DisplayName("Zero Length Is Rejected")
    void zeroLengthIsRejected() {
        assertRejectedSettings(0, "en", true, false, "", false);
    }

    @Test
    @DisplayName("Too Large Length Is Rejected")
    void tooLargeLengthIsRejected() {
        assertRejectedSettings(1_000_001, "en", true, false, "", false);
    }

    @Test
    @DisplayName("Unknown Language Is Rejected")
    void unknownLanguageIsRejected() {
        assertRejectedSettings(10, "xx", true, false, "", false);
    }

    @Test
    @DisplayName("Non Digit Symbols In Digits Set Are Rejected")
    void nonDigitSymbolsAreRejected() {
        assertRejectedSettings(10, "en", true, true, "12a", false);
    }

    @Test
    @DisplayName("Empty Alphabet Is Rejected")
    void emptyAlphabetIsRejected() {
        assertRejectedSettings(10, "en", false, false, "", false);
    }

    @Test
    @DisplayName("Too Short Length For Required Groups Is Rejected")
    void tooShortLengthIsRejected() {
        assertRejectedSettings(5, "both", true, true, "0", true);
    }

    /** Каждый плохой набор настроек роняет генератор с IllegalArgumentException. */
    private void assertRejectedSettings(int length, String language, boolean lower, boolean upper,
                                        String digits, boolean special) {
        assertThatThrownBy(() -> generator.generate(length, language, lower, upper, digits, special))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** Проверяет присутствие хотя бы одного символа из группы. */
    private boolean hasAny(String text, String chars) {
        return chars.chars().anyMatch(character -> text.indexOf(character) >= 0);
    }
}
