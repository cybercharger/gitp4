package gitp4.git.cmd;

import gitp4.CmdRunner;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by chriskang on 8/24/2016.
 */
public class GitCommit {
    private static final String GIT_COMMIT_CMD = "git commit -m\"%s\"";

    public static void run(String comments) throws Exception {
        if (StringUtils.isBlank(comments)) throw new NullPointerException("comments");
        CmdRunner.run(() -> String.format(GIT_COMMIT_CMD, comments), (cmdRes) -> "");
    }
}
