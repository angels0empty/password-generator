package passwordgenerator;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class Benchmark {
    static final int[] LENGTHS = {10_000, 50_000, 100_000, 250_000, 500_000, 750_000, 1_000_000};
    static final int PROFILES = 5;
    static final int REPEATS = 10;

    private Benchmark() {
    }

    static String generateProfile(int length, int profile) {
        PasswordGenerator generator = new PasswordGenerator();
        return switch (profile) {
            case 1 -> generator.generate(length, "", false, false, "0123456789", false);
            case 2 -> generator.generate(length, "en", true, false, "", false);
            case 3 -> generator.generate(length, "en", true, true, "0123456789", false);
            case 4 -> generator.generate(length, "both", true, true, "0123456789", false);
            case 5 -> generator.generate(length, "both", true, true, "0123456789", true);
            default -> throw new IllegalArgumentException("Неизвестный профиль: " + profile);
        };
    }

    public static void run(File detailsFile) throws Exception {
        try (PrintWriter details = new PrintWriter(detailsFile, StandardCharsets.UTF_8)) {
            details.println("length;profile;run;milliseconds");
            for (int length : LENGTHS) {
                for (int profile = 1; profile <= PROFILES; profile++) {
                    for (int run = 1; run <= REPEATS; run++) {
                        long start = System.nanoTime();
                        generateProfile(length, profile);
                        double milliseconds = (System.nanoTime() - start) / 1_000_000.0;
                        details.printf(Locale.US, "%d;%d;%d;%.3f%n", length, profile, run, milliseconds);
                        System.out.printf(Locale.US,
                                "length=%d profile=%d run=%d -> %.3f ms%n",
                                length, profile, run, milliseconds);
                    }
                }
            }
        }
    }
}
