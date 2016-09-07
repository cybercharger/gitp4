package gitp4.p4.cmd;

import gitp4.CmdRunner;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by chriskang on 9/7/2016.
 */
public class P4Where {
    private static String CMD_FMT = "p4 where %s";

    public static List<String> run(String input) {
        if (StringUtils.isBlank(input)) throw new NullPointerException("input");
        return CmdRunner.run(() -> String.format(CMD_FMT, input), cmdRes -> cmdRes);
    }
}
