package passwordgenerator;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            if (args.length > 0 && args[0].equalsIgnoreCase("test")) {
                Benchmark.run(new File("benchmark.csv"));
                System.out.println("[+] benchmark.csv, system-info.txt");
            } else {
                runDialog(new Scanner(System.in));
            }
        } catch (Exception exception) {
            System.out.println("[-] " + exception.getMessage());
        }
    }

    private static void runDialog(Scanner scanner) {
        System.out.print("Длина: ");
        int length = SettingsValidator.parseLength(scanner.nextLine());
        System.out.print("Язык (en/ru/both): ");
        String language = scanner.nextLine();
        System.out.print("Строчные (y/n): ");
        boolean lower = yes(scanner.nextLine());
        System.out.print("Прописные (y/n): ");
        boolean upper = yes(scanner.nextLine());
        System.out.print("Цифры (пусто - нет): ");
        String digits = scanner.nextLine().trim();
        System.out.print("Спецсимволы (y/n): ");
        boolean special = yes(scanner.nextLine());

        long start = System.nanoTime();
        String password = new PasswordGenerator().generate(length, language, lower, upper, digits, special);
        double milliseconds = (System.nanoTime() - start) / 1_000_000.0;
        printResult(password, milliseconds);
    }

    private static boolean yes(String value) {
        return value.trim().equalsIgnoreCase("y") || value.trim().equalsIgnoreCase("д");
    }

    private static void printResult(String password, double milliseconds) {
        if (password.length() <= 200) {
            System.out.println("[+] " + password);
        } else {
            copyToClipboard(password);
            System.out.println("[+] " + password.substring(0, 32)
                    + "... (обрезано; полный пароль в буфере обмена)");
        }
        System.out.printf(Locale.US, "[+] %.3f мс%n", milliseconds);
    }

    private static void copyToClipboard(String text) {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(text), null);
        } catch (HeadlessException exception) {
            System.out.println("[!] Буфер обмена недоступен без графической среды");
        } catch (IllegalStateException exception) {
            System.out.println("[!] Буфер обмена занят, попробуйте ещё раз");
        }
    }
}
