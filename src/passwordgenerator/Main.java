package passwordgenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class Main {
    private static final Logger LOG = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            if (args.length > 0 && args[0].equalsIgnoreCase("test")) {
                LOG.info("Benchmark mode");
                Benchmark.run(new File("benchmark.csv"), new File("benchmark-summary.csv"));
                System.out.println("[+] benchmark.csv, benchmark-summary.csv, system-info.txt");
            } else {
                LOG.info("Console mode");
                ConsoleApp.run();
            }
        } catch (Exception exception) {
            LOG.error("Startup error: {}", exception.getMessage());
            System.out.println("[-] " + exception.getMessage());
        }
    }
}
