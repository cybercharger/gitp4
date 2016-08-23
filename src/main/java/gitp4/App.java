package gitp4;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class App {
    private static final Logger logger = Logger.getLogger(App.class);
    public static void main(String[] args) {
        try {
            String result = CommandRunner.runCommand("p4 changes //nucleus/SANDBOX/testgitp4/...@313591,#head", -1, TimeUnit.MILLISECONDS);
            logger.info(result);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
