package passwordgenerator;

import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;


// ref https://www.besthub.dev/articles/getting-started-with-oshi-a-java-library-for-cross-platform-system-information-fa2bf3abf493
public final class SystemInfo {
    private SystemInfo() {
    }

    public static void printToConsole() {
        write(new PrintWriter(System.out, true));
    }

    public static void writeFile(File file) throws Exception {
        try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
            write(writer);
        }
    }

    private static void write(PrintWriter writer) {
        oshi.SystemInfo info = new oshi.SystemInfo();
        OperatingSystem os = info.getOperatingSystem();
        CentralProcessor processor = info.getHardware().getProcessor();
        GlobalMemory memory = info.getHardware().getMemory();
        ComputerSystem computer = info.getHardware().getComputerSystem();

        writer.println("OS: " + os);
        writer.println("Manufacturer: " + computer.getManufacturer());
        writer.println("Model: " + computer.getModel());
        writer.println("CPU: " + processor.getProcessorIdentifier().getName());
        writer.println("Logical processors: " + processor.getLogicalProcessorCount());
        writer.println("RAM bytes: " + memory.getTotal());
        writer.println("Java: " + System.getProperty("java.version"));
        writer.flush();
    }
}
