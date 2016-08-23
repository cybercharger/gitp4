package gitp4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Created by chriskang on 8/23/2016.
 */
public class CommandRunner {
    public static String runCommand(String cmd, long timeout, TimeUnit unit) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("p4 changes //nucleus/SANDBOX/testgitp4/...@313591,#head");
        p.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder result = new StringBuilder();
        for(String res = reader.readLine(); res != null; res =reader.readLine()) {
            result.append(res).append("\n");
        }
        return result.toString();
    }
}
