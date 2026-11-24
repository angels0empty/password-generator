package passwordgenerator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Тесты бенчмарка: статистика, CSV-файлы и остановка по отмене. */
@DisplayName("Benchmark Test")
class BenchmarkTest {
    @TempDir
    Path tempDirectory;

    @Test
    @DisplayName("Calculates Series Statistics")
    void calculatesStatistics() {
        // Execution
        Benchmark.Result result = Benchmark.calculate(100, 1, new double[]{4, 1, 2, 3});

        // Assertion
        assertThat(result.min).isEqualTo(1.0);
        assertThat(result.max).isEqualTo(4.0);
        assertThat(result.average).isEqualTo(2.5);
        assertThat(result.median).isEqualTo(2.5);
        assertThat(result.speed).isEqualTo(40_000.0);
    }

    @Test
    @DisplayName("Creates Reports And System Info")
    void createsReportsAndSystemInfo() throws Exception {
        // Setup
        File details = tempDirectory.resolve("details.csv").toFile();
        File summary = tempDirectory.resolve("summary.csv").toFile();

        // Execution
        List<Benchmark.Result> results = Benchmark.run(details, summary, new int[]{100}, 2, null);

        // Assertion
        assertThat(results).hasSize(5);
        assertThat(Files.readAllLines(details.toPath())).hasSize(11);
        assertThat(Files.readAllLines(summary.toPath())).hasSize(6);

        String systemInfo = Files.readString(tempDirectory.resolve("system-info.txt"));
        assertThat(systemInfo)
                .contains("OS:")
                .contains("CPU:")
                .contains("RAM bytes:")
                .contains("Java:");
    }

    @Test
    @DisplayName("Rejects Invalid Series Parameters")
    void rejectsInvalidSeriesParameters() {
        // Setup
        File details = tempDirectory.resolve("details.csv").toFile();
        File summary = tempDirectory.resolve("summary.csv").toFile();

        // Assertion
        assertThatThrownBy(() -> Benchmark.run(details, summary, new int[]{100}, 0, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Stops After Interruption")
    void stopsAfterInterruption() throws Exception {
        // Setup
        AtomicBoolean stopped = new AtomicBoolean();
        Thread thread = new Thread(() -> runCancelledBenchmark(stopped));
        thread.start();
        Thread.sleep(200);

        // Execution
        thread.interrupt();
        thread.join(10_000);

        // Assertion
        assertThat(thread.isAlive()).isFalse();
        assertThat(stopped).isTrue();
    }

    /** Проверка работы отмены */
    private void runCancelledBenchmark(AtomicBoolean stopped) {
        try {
            Benchmark.run(tempDirectory.resolve("cancel.csv").toFile(),
                    tempDirectory.resolve("cancel-summary.csv").toFile(),
                    new int[]{1_000_000}, 10, null);
        } catch (Exception exception) {
            stopped.set(true);
        }
    }
}
