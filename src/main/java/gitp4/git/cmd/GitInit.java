package gitp4.git.cmd;

import gitp4.CmdRunner;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by chriskang on 8/24/2016.
 */
public class GitInit {
    private static final String GIT_INIT_CMD = "git init %s";

    public static void run(final String parameters) throws Exception {
        final String cmdParams = StringUtils.isBlank(parameters) ? "" : parameters;
        CmdRunner.run(() -> String.format(GIT_INIT_CMD, cmdParams), (cmdRes) -> "");
    }
}
