package passwordgenerator;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            runDialog(new Scanner(System.in));
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

        String password = new PasswordGenerator().generate(length, language, lower, upper, digits, special);
        System.out.println("[+] " + password);
    }

    private static boolean yes(String value) {
        return value.trim().equalsIgnoreCase("y") || value.trim().equalsIgnoreCase("д");
    }
}
