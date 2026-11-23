package passwordgenerator;

/** Отдельная точка входа для запуска приложения из JAR-файла. */
public final class Launcher {
    private Launcher() {
    }

    /**
     * Передаёт аргументы командной строки основному классу.
     *
     * @param args аргументы запуска
     */
    public static void main(String[] args) {
        Main.main(args);
    }
}
