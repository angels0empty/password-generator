package passwordgenerator;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Отдельная точка входа для запуска приложения из JAR-файла. */
public final class Launcher {
    static {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
    }

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
