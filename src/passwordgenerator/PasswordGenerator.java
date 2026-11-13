package passwordgenerator;

import java.security.SecureRandom;

public class PasswordGenerator {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";

    private final SecureRandom random = new SecureRandom();

    public String generate(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Длина должна быть положительной!");
        }
        StringBuilder password = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            password.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return password.toString();
    }
}
