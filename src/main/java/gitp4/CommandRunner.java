package gitp4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by chriskang on 8/23/2016.
 */
public class CommandRunner {
    public static List<String> runCommand(String cmd) throws IOException, InterruptedException {
        return runCommand(cmd, -1, TimeUnit.MILLISECONDS);
    }

    public static List<String> runCommand(String cmd, long timeout, TimeUnit unit) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor(timeout, unit);
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        List<String> result = new LinkedList<>();
        for(String res = reader.readLine(); res != null; res =reader.readLine()) {
            result.add(res);
        }
        return result;
    }
}
