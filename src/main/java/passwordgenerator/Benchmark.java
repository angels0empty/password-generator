package passwordgenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/** Проводит измерения скорости генерации и сохраняет их результаты. */
public class Benchmark {
    static final int[] LENGTHS = {10_000, 50_000, 100_000, 250_000, 500_000, 750_000, 1_000_000};
    static final int PROFILES = 5;
    static final int REPEATS = 10;
    private static final Logger LOG = LogManager.getLogger(Benchmark.class);
    private static final PasswordGenerator GENERATOR = new PasswordGenerator();

    /**
     * Запускает бенчмарк: все длины, все профили, {@link #REPEATS} запусков.
     *
     * @param detailsFile файл отдельных измерений
     * @param summaryFile файл итоговой статистики
     * @param listener обработчик завершённой серии
     * @return результаты всех серий
     * @throws Exception ошибка записи или прерывание бенчмарка
     */
    public static List<Result> run(File detailsFile, File summaryFile,
                                   Consumer<Result> listener) throws Exception {
        return run(detailsFile, summaryFile, LENGTHS, REPEATS, listener);
    }

    /**
     * Запускает бенчмарк с указанными длинами и числом повторов.
     *
     * @param detailsFile файл отдельных измерений
     * @param summaryFile файл итоговой статистики
     * @param lengths исследуемые длины
     * @param repeats количество измерений в серии
     * @param listener обработчик завершённой серии
     * @return результаты всех серий
     * @throws Exception ошибка записи или прерывание бенчмарка
     */
    public static List<Result> run(File detailsFile, File summaryFile, int[] lengths,
                                   int repeats, Consumer<Result> listener) throws Exception {
        validateSeries(lengths, repeats);
        SystemInfo.writeFile(systemInfoFile(summaryFile));
        List<Result> results = new ArrayList<>();

        try (PrintWriter details = createWriter(detailsFile);
             PrintWriter summary = createWriter(summaryFile)) {
            writeHeaders(details, summary);
            for (int length : lengths) {
                for (int profile = 1; profile <= PROFILES; profile++) {
                    Result result = runSeries(length, profile, repeats, details, summary);
                    results.add(result);
                    notifyListener(listener, result);
                }
            }
        }
        return results;
    }

    /** Проверяет параметры бенчмарка. */
    private static void validateSeries(int[] lengths, int repeats) {
        if (lengths == null || lengths.length == 0 || repeats < 1) {
            throw new IllegalArgumentException("Неверные параметры бенчмарка");
        }
    }

    /** Создаёт UTF-8 writer для файла с резульатамти. */
    private static PrintWriter createWriter(File file) throws Exception {
        return new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8));
    }

    /** Пишет хедеры CSV-файлов. */
    private static void writeHeaders(PrintWriter details, PrintWriter summary) {
        details.println("length;profile;run;ms");
        summary.println("length;profile;min;max;average;median;chars_per_second");
    }

    /** Выполняет прогрев и одну серию измерений. */
    private static Result runSeries(int length, int profile, int repeats,
                                    PrintWriter details, PrintWriter summary) throws Exception {
        checkStop();
        generateProfile(length, profile);
        double[] times = new double[repeats];
        for (int run = 0; run < repeats; run++) {
            checkStop();
            times[run] = measure(length, profile);
            details.printf(Locale.US, "%d;P%d;%d;%.3f%n", length, profile, run + 1, times[run]);
        }

        Result result = calculate(length, profile, times);
        writeSummary(summary, result);
        details.flush();
        LOG.info("Run: length={}, profile=P{}, average={} ms", length, profile,
                String.format(Locale.US, "%.3f", result.average));
        return result;
    }

    /** Записывает строку итоговой статистики серии. */
    private static void writeSummary(PrintWriter summary, Result result) {
        summary.printf(Locale.US, "%d;P%d;%.3f;%.3f;%.3f;%.3f;%.2f%n",
                result.length, result.profile, result.min, result.max,
                result.average, result.median, result.speed);
        summary.flush();
    }

    /** Измеряет один запуск генерации без вывода результата. */
    private static double measure(int length, int profile) {
        long start = System.nanoTime();
        generateProfile(length, profile);
        return (System.nanoTime() - start) / 1_000_000.0;
    }

    /** Передаёт результат серии обработчику интерфейса. */
    private static void notifyListener(Consumer<Result> listener, Result result) {
        if (listener != null) {
            listener.accept(result);
        }
    }

    /**
     * Рассчитывает статистику по временам запусков.
     *
     * @param length длина пароля
     * @param profile профиль сложности
     * @param times времена запусков в миллисекундах
     * @return итоговая статистика
     */
    static Result calculate(int length, int profile, double[] times) {
        double[] sorted = times.clone();
        Arrays.sort(sorted);
        double average = Arrays.stream(times).sum() / times.length;
        double median = median(sorted);
        return new Result(length, profile, sorted[0], sorted[sorted.length - 1],
                average, median, length / (average / 1000.0));
    }

    /** Находит медиану отсортированного массива. */
    private static double median(double[] sorted) {
        int middle = sorted.length / 2;
        if (sorted.length % 2 == 0) {
            return (sorted[middle - 1] + sorted[middle]) / 2;
        }
        return sorted[middle];
    }

    /** Генерирует пароль для одного из пяти профилей сложности. */
    static String generateProfile(int length, int profile) {
        return switch (profile) {
            case 1 -> GENERATOR.generate(length, "en", false, false, "0123456789", false);
            case 2 -> GENERATOR.generate(length, "en", true, false, "", false);
            case 3 -> GENERATOR.generate(length, "en", true, true, "0123456789", false);
            case 4 -> GENERATOR.generate(length, "both", true, true, "0123456789", false);
            case 5 -> GENERATOR.generate(length, "both", true, true, "0123456789", true);
            default -> throw new IllegalArgumentException("Неверный профиль");
        };
    }

    /** Возвращает путь к файлу сведений о системе. */
    private static File systemInfoFile(File summaryFile) {
        return new File(summaryFile.getAbsoluteFile().getParentFile(), "system-info.txt");
    }

    /** Проверяет запрос на отмену бенчмарка. */
    private static void checkStop() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Бенчмарк отменён");
        }
    }

    /** Результат одной серии измерений. */
    public static class Result {
        /** Длина пароля в серии. */
        public final int length;
        /** Индекс профиля сложности. */
        public final int profile;
        /** Минимальное время генерации, мс. */
        public final double min;
        /** Максимальное время генерации, мс. */
        public final double max;
        /** Среднее время генерации, мс. */
        public final double average;
        /** Медианное время генерации, мс. */
        public final double median;
        /** Скорость генерации, символов в секунду. */
        public final double speed;

        /**
         * Создаёт объект со статистикой серии.
         *
         * @param length длина пароля
         * @param profile индекс профиля сложности
         * @param min минимальное время серии
         * @param max максимальное время серии
         * @param average среднее время серии
         * @param median медианное время серии
         * @param speed скорость генерации в символах в секунду
         */
        public Result(int length, int profile, double min, double max,
                      double average, double median, double speed) {
            this.length = length;
            this.profile = profile;
            this.min = min;
            this.max = max;
            this.average = average;
            this.median = median;
            this.speed = speed;
        }
    }
}
