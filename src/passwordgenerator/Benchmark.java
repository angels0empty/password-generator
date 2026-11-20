package passwordgenerator;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

    public static void run(File detailsFile, File summaryFile) throws Exception {
        SystemInfo.printToConsole();
        SystemInfo.writeFile(new File("system-info.txt"));
        try (PrintWriter details = new PrintWriter(detailsFile, StandardCharsets.UTF_8);
             PrintWriter summary = new PrintWriter(summaryFile, StandardCharsets.UTF_8)) {
            details.println("length;profile;run;milliseconds");
            summary.println("length;profile;min;max;avg;median");
            for (int length : LENGTHS) {
                for (int profile = 1; profile <= PROFILES; profile++) {
                    writeSeries(length, profile, details, summary);
                }
            }
        }
    }

    private static void writeSeries(int length, int profile,
                                    PrintWriter details, PrintWriter summary) {
        double[] times = new double[REPEATS];
        for (int run = 1; run <= REPEATS; run++) {
            long start = System.nanoTime();
            generateProfile(length, profile);
            double milliseconds = (System.nanoTime() - start) / 1_000_000.0;
            times[run - 1] = milliseconds;
            details.printf(Locale.US, "%d;%d;%d;%.3f%n", length, profile, run, milliseconds);
            System.out.printf(Locale.US,
                    "length=%d profile=%d run=%d -> %.3f ms%n",
                    length, profile, run, milliseconds);
        }
        double[] sorted = times.clone();
        Arrays.sort(sorted);
        double min = sorted[0];
        double max = sorted[sorted.length - 1];
        double avg = Arrays.stream(times).average().orElse(0);
        double median = sorted.length % 2 == 1
                ? sorted[sorted.length / 2]
                : (sorted[sorted.length / 2 - 1] + sorted[sorted.length / 2]) / 2.0;
        summary.printf(Locale.US, "%d;%d;%.3f;%.3f;%.3f;%.3f%n",
                length, profile, min, max, avg, median);
    }
}