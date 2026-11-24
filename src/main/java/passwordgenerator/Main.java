package passwordgenerator;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Arrays;

/** Основной класс JavaFX-приложения. */
public class Main extends Application {
    private static final Logger LOG = LogManager.getLogger(Main.class);

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        Tab generatorTab = new Tab("Генератор", new GeneratorView().getView());
        Tab benchmarkTab = new Tab("Замеры", new BenchmarkView().getView());
        generatorTab.setClosable(false);
        benchmarkTab.setClosable(false);

        stage.setScene(new Scene(new TabPane(generatorTab, benchmarkTab), 850, 650));
        stage.setTitle("Генератор паролей");
        stage.show();
        LOG.info("GUI started");
    }

    /**
     * Выбирает графический, консольный или экспериментальный режим.
     *
     * @param args console - консоль, test - эксперимент, без аргументов - JavaFX
     */
    public static void main(String[] args) {
        try {
            if (hasArgument(args, "console")) {
                ConsoleApp.run();
            } else if (hasArgument(args, "test")) {
                runBenchmarkConsole();
            } else if (args.length > 0) {
                LOG.warn("Unknown arguments: {}", Arrays.toString(args));
                System.out.println("[-] Аргументы: console | test");
            } else {
                runGuiOrFallbackToConsole();
            }
        } catch (Exception exception) {
            LOG.error("Application startup error", exception);
            System.out.println("[-] " + exception.getMessage());
        }
    }

    /** Пробует открыть окно. Если графического окружения нет - продолжаем в консольном режимею */
    private static void runGuiOrFallbackToConsole() throws Exception {
        try {
            launch(new String[0]);
        } catch (RuntimeException exception) {
            LOG.warn("GUI is not available, falling back to console mode", exception);
            System.out.println("[-] Графический интерфейс недоступен, переключаюсь в консольный режим. Для бенчмарка используйте аргумент test");
            ConsoleApp.run();
        }
    }

    /** Проверяет наличие аргумента командной строки. */
    private static boolean hasArgument(String[] args, String value) {
        return args.length > 0 && args[0].equalsIgnoreCase(value);
    }

    /** Запускает эксперимент из консоли */
    private static void runBenchmarkConsole() throws Exception {
        Benchmark.run(new File("benchmark.csv"), new File("benchmark-summary.csv"), null);
        System.out.println("[+] benchmark.csv, benchmark-summary.csv, system-info.txt");
    }
}
