package passwordgenerator;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

/** Вкладка настройки, генерации, копирования и сохранения пароля. */
public class GeneratorView {
    private static final Logger LOG = LogManager.getLogger(GeneratorView.class);

    private final BorderPane root = new BorderPane();
    private final TextField lengthField = new TextField("16");
    private final CheckBox englishBox = new CheckBox("Английский");
    private final CheckBox russianBox = new CheckBox("Русский");
    private final CheckBox lowerBox = new CheckBox("Строчные");
    private final CheckBox upperBox = new CheckBox("Прописные");
    private final CheckBox digitsBox = new CheckBox("Цифры");
    private final TextField digitsField = new TextField("0123456789");
    private final CheckBox specialBox = new CheckBox("Спецсимволы");
    private final TextArea resultArea = new TextArea();
    private final Label statusLabel = new Label(" ");
    private final Button generateButton = new Button("Сгенерировать");
    private final Button copyButton = new Button("Копировать");
    private final Button saveButton = new Button("Сохранить");

    private final PasswordGenerator generator = new PasswordGenerator();
    private Task<GenerationResult> generationTask;
    private String password = "";

    /** Создаёт и настраивает содержимое вкладки. */
    public GeneratorView() {
        configureControls();
        root.setPadding(new Insets(10));
        root.setTop(createSettings());
        root.setCenter(resultArea);
        root.setBottom(statusLabel);
    }

    /**
     * Возвращает корневой элемент вкладки.
     *
     * @return корневой элемент вкладки
     */
    public Node getView() {
        return root;
    }

    /** Настраивает начальное состояние элементов управления. */
    private void configureControls() {
        englishBox.setSelected(true);
        lowerBox.setSelected(true);
        upperBox.setSelected(true);
        digitsBox.setSelected(true);
        specialBox.setSelected(true);
        copyButton.setDisable(true);
        saveButton.setDisable(true);
        digitsField.disableProperty().bind(digitsBox.selectedProperty().not());
        resultArea.setWrapText(true);
        resultArea.setFont(Font.font("Monospaced", 14));
        generateButton.setOnAction(event -> generatePassword());
        copyButton.setOnAction(event -> copyPassword());
        saveButton.setOnAction(event -> savePassword());
    }

    /** Создаёт панель параметров и кнопок. */
    private Node createSettings() {
        GridPane settings = new GridPane();
        settings.setHgap(8);
        settings.setVgap(8);
        settings.addRow(0, new Label("Длина"), lengthField);
        settings.addRow(1, new Label("Языки"), new HBox(8, englishBox, russianBox));
        settings.addRow(2, new Label("Регистры"), new HBox(8, lowerBox, upperBox));
        settings.addRow(3, new Label("Набор цифр"), new HBox(8, digitsBox, digitsField));
        settings.addRow(4, new Label("Дополнительно"), specialBox);

        HBox buttons = new HBox(8, generateButton, copyButton, saveButton);
        buttons.setPadding(new Insets(8, 0, 8, 0));
        BorderPane top = new BorderPane();
        top.setCenter(settings);
        top.setBottom(buttons);
        return top;
    }

    /** Проверяет введённые настройки и запускает фоновую генерацию. */
    private void generatePassword() {
        try {
            int length = SettingsValidator.parseLength(lengthField.getText());
            String error = SettingsValidator.validate(englishBox.isSelected(), russianBox.isSelected(),
                    lowerBox.isSelected(), upperBox.isSelected(), digitsBox.isSelected(), digitsField.getText());
            if (error != null) {
                throw new IllegalArgumentException(error);
            }
            startGeneration(length);
        } catch (Exception exception) {
            showError(exception);
        }
    }

    /** Создаёт и запускает задачу генерации. */
    private void startGeneration(int length) {
        String language = selectedLanguage();
        boolean lower = lowerBox.isSelected();
        boolean upper = upperBox.isSelected();
        String digits = selectedDigits();
        boolean special = specialBox.isSelected();
        generationTask = new Task<>() {
            /** {@inheritDoc} */
            @Override
            protected GenerationResult call() {
                return measureGeneration(length, language, lower, upper, digits, special);
            }
        };
        generationTask.setOnSucceeded(event -> showResult(generationTask.getValue()));
        generationTask.setOnFailed(event -> handleTaskError(generationTask.getException()));
        setGenerating(true);
        startDaemonThread(generationTask);
    }

    /** Измеряет время создания одного пароля. */
    private GenerationResult measureGeneration(int length, String language, boolean lower,
                                               boolean upper, String digits, boolean special) {
        long start = System.nanoTime();
        String result = generator.generate(length, language, lower, upper, digits, special);
        double milliseconds = (System.nanoTime() - start) / 1_000_000.0;
        return new GenerationResult(result, milliseconds);
    }

    /** Возвращает выбранный язык в формате генератора. */
    private String selectedLanguage() {
        if (englishBox.isSelected() && russianBox.isSelected()) {
            return "both";
        }
        if (russianBox.isSelected()) {
            return "ru";
        }
        return "en";
    }

    /** Возвращает набор цифр или пустую строку при отключённой опции. */
    private String selectedDigits() {
        return digitsBox.isSelected() ? digitsField.getText().trim() : "";
    }

    /** Запускает задачу в отдельном daemon-потоке. */
    private void startDaemonThread(Task<?> task) {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /** Отображает результат успешной генерации. */
    private void showResult(GenerationResult result) {
        password = result.password;
        resultArea.setText(password.length() <= 100_000
                ? password : "Пароль готов. Используйте копирование или сохранение.");
        resultArea.positionCaret(0);
        statusLabel.setText(String.format(Locale.US, "[+] %d символов | %.3f мс",
                password.length(), result.milliseconds));
        setGenerating(false);
        LOG.info("GUI generation: length={}, time={} ms", password.length(), result.milliseconds);
    }

    /** Обрабатывает ошибку фоновой задачи. */
    private void handleTaskError(Throwable exception) {
        setGenerating(false);
        showError(exception);
    }

    /** Переключает доступность кнопок во время генерации. */
    private void setGenerating(boolean generating) {
        generateButton.setDisable(generating);
        copyButton.setDisable(generating || password.isEmpty());
        saveButton.setDisable(generating || password.isEmpty());
    }

    /** Копирует пароль в системный буфер обмена. */
    private void copyPassword() {
        try {
            ClipboardContent content = new ClipboardContent();
            content.putString(password);
            Clipboard.getSystemClipboard().setContent(content);
            statusLabel.setText("[+] Скопировано");
        } catch (Exception exception) {
            showError(exception);
        }
    }

    /** Открывает диалог и сохраняет пароль в выбранный файл. */
    private void savePassword() {
        try {
            File file = chooseFile();
            if (file != null && confirmOverwrite(file)) {
                Files.writeString(file.toPath(), password, StandardCharsets.UTF_8);
                statusLabel.setText("[+] Сохранено");
            }
        } catch (Exception exception) {
            showError(exception);
        }
    }

    /** Показывает диалог выбора файла. */
    private File chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("password.txt");
        return chooser.showSaveDialog(root.getScene().getWindow());
    }

    /** Запрашивает подтверждение перезаписи существующего файла. */
    private boolean confirmOverwrite(File file) {
        if (!file.exists()) {
            return true;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Перезаписать файл?");
        alert.initOwner(root.getScene().getWindow());
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    /** Показывает ошибку без аварийного завершения приложения. */
    private void showError(Throwable exception) {
        String message = exception.getMessage() == null ? exception.toString() : exception.getMessage();
        statusLabel.setText("[-] " + message);
        LOG.error("Generator tab error: {}", message);
    }

    /** Результат генерации и его время. */
    private static class GenerationResult {
        private final String password;
        private final double milliseconds;

        /** Создаёт результат генерации. */
        private GenerationResult(String password, double milliseconds) {
            this.password = password;
            this.milliseconds = milliseconds;
        }
    }
}
