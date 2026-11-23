package passwordgenerator;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Locale;

/** Вкладка запуска бенчмарка и отображения статистики. */
public class BenchmarkView {
    private static final Logger LOG = LogManager.getLogger(BenchmarkView.class);
    private static final int SERIES_COUNT = Benchmark.LENGTHS.length * Benchmark.PROFILES;

    private final BorderPane root = new BorderPane();
    private final TableView<Benchmark.Result> table = new TableView<>();
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label statusLabel = new Label(" ");
    private final Button startButton = new Button("Запустить");
    private final Button cancelButton = new Button("Отмена");
    private Task<List<Benchmark.Result>> benchmarkTask;

    /** Создаёт и настраивает содержимое вкладки. */
    public BenchmarkView() {
        configureControls();
        root.setPadding(new Insets(10));
        root.setTop(createTopPanel());
        root.setCenter(createTable());
        root.setBottom(createBottomPanel());
    }

    /**
     * Возвращает корневой элемент вкладки.
     *
     * @return корневой элемент вкладки
     */
    public Node getView() {
        return root;
    }

    /** Настраивает кнопки и таблицу. */
    private void configureControls() {
        cancelButton.setDisable(true);
        startButton.setOnAction(event -> startBenchmark());
        cancelButton.setOnAction(event -> cancelBenchmark());
        progressBar.setMaxWidth(Double.MAX_VALUE);
    }

    /** Создаёт верхнюю панель с описанием и кнопками. */
    private Node createTopPanel() {
        Label info = new Label(Benchmark.LENGTHS.length + " длин, " + Benchmark.PROFILES
                + " профилей, " + Benchmark.REPEATS + " запусков");
        HBox buttons = new HBox(8, startButton, cancelButton);
        BorderPane top = new BorderPane();
        top.setTop(info);
        top.setBottom(buttons);
        return top;
    }

    /** Создаёт таблицу итоговой статистики. */
    private TableView<Benchmark.Result> createTable() {
        table.getColumns().add(textColumn("Длина", "length"));
        table.getColumns().add(textColumn("Профиль", "profile"));
        table.getColumns().add(textColumn("Мин", "min"));
        table.getColumns().add(textColumn("Макс", "max"));
        table.getColumns().add(textColumn("Среднее", "average"));
        table.getColumns().add(textColumn("Медиана", "median"));
        table.getColumns().add(textColumn("Сим/с", "speed"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    /** Создаёт одну текстовую колонку для поля результата. */
    private TableColumn<Benchmark.Result, String> textColumn(String title, String field) {
        TableColumn<Benchmark.Result, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatField(data.getValue(), field)));
        return column;
    }

    /** Форматирует выбранное поле результата для таблицы. */
    private String formatField(Benchmark.Result result, String field) {
        return switch (field) {
            case "length" -> String.valueOf(result.length);
            case "profile" -> "P" + result.profile;
            case "min" -> String.format(Locale.US, "%.3f", result.min);
            case "max" -> String.format(Locale.US, "%.3f", result.max);
            case "average" -> String.format(Locale.US, "%.3f", result.average);
            case "median" -> String.format(Locale.US, "%.3f", result.median);
            default -> String.format(Locale.US, "%.2f", result.speed);
        };
    }

    /** Создаёт нижнюю панель прогресса и состояния. */
    private Node createBottomPanel() {
        BorderPane bottom = new BorderPane();
        bottom.setCenter(progressBar);
        bottom.setBottom(statusLabel);
        return bottom;
    }

    /** Запускает бенчмарк в фоне. */
    private void startBenchmark() {
        table.getItems().clear();
        progressBar.setProgress(0);
        statusLabel.setText("...");
        benchmarkTask = createTask();
        benchmarkTask.setOnSucceeded(event -> finishSuccess());
        benchmarkTask.setOnCancelled(event -> finishCancelled());
        benchmarkTask.setOnFailed(event -> finishError(benchmarkTask.getException()));
        setRunning(true);
        Thread thread = new Thread(benchmarkTask);
        thread.setDaemon(true);
        thread.start();
        LOG.info("Benchmark started from GUI");
    }

    /** Создаёт задачу полного бенчмарка. */
    private Task<List<Benchmark.Result>> createTask() {
        return new Task<>() {
            /** {@inheritDoc} */
            @Override
            protected List<Benchmark.Result> call() throws Exception {
                return Benchmark.run(new File("benchmark.csv"), new File("benchmark-summary.csv"),
                        result -> Platform.runLater(() -> addResult(result)));
            }
        };
    }

    /** Добавляет результат серии в таблицу и обновляет прогресс. */
    private void addResult(Benchmark.Result result) {
        table.getItems().add(result);
        progressBar.setProgress((double) table.getItems().size() / SERIES_COUNT);
    }

    /** Отображает успешное завершение бенчмарка. */
    private void finishSuccess() {
        setRunning(false);
        statusLabel.setText("[+] benchmark.csv, benchmark-summary.csv, system-info.txt");
        LOG.info("Benchmark finished");
    }

    /** Отображает отмену бенчмарка. */
    private void finishCancelled() {
        setRunning(false);
        statusLabel.setText("[-] Отменено");
        LOG.info("Benchmark cancelled");
    }

    /** Отображает ошибку бенчмарка. */
    private void finishError(Throwable exception) {
        setRunning(false);
        showError(exception);
    }

    /** Переключает доступность кнопок бенчмарка. */
    private void setRunning(boolean running) {
        startButton.setDisable(running);
        cancelButton.setDisable(!running);
    }

    /** Запрашивает отмену только задачи бенчмарка. */
    private void cancelBenchmark() {
        if (benchmarkTask != null) {
            benchmarkTask.cancel(true);
        }
    }

    /** Показывает ошибку без аварийного завершения приложения. */
    private void showError(Throwable exception) {
        String message = exception.getMessage() == null ? exception.toString() : exception.getMessage();
        statusLabel.setText("[-] " + message);
        LOG.error("Benchmark error", exception);
    }
}
