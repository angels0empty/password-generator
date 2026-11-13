package passwordgenerator;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Длина пароля: ");

        int length = Integer.parseInt(scanner.nextLine().trim());
        String password = new PasswordGenerator().generate(length);
        System.out.println("[+] " + password);
    }
}