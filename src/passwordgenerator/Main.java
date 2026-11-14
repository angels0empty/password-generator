package passwordgenerator;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Длина: ");
        int length = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Язык (en/ru/both): ");
        String language = scanner.nextLine();
        System.out.print("Строчные (y/n): ");
        boolean lower = scanner.nextLine().trim().equalsIgnoreCase("y");
        System.out.print("Прописные (y/n): ");
        boolean upper = scanner.nextLine().trim().equalsIgnoreCase("y");
        System.out.print("Цифры (пусто - нет): ");
        String digits = scanner.nextLine().trim();
        System.out.print("Спецсимволы (y/n): ");
        boolean special = scanner.nextLine().trim().equalsIgnoreCase("y");

        String password = new PasswordGenerator().generate(length, language, lower, upper, digits, special);
        System.out.println("[+] " + password);
    }
}
