package gitp4.git.cmd;

import gitp4.CmdRunner;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by chriskang on 9/21/2016.
 */
public class GitRevList {
    private static final String FIRST_COMMIT_CMD = " git rev-list --max-parents=0 HEAD";

    public static String getFirstCommit() {
        return CmdRunner.getGitCmdRunner().run(() -> FIRST_COMMIT_CMD,
                cmdRes -> {
                    if (cmdRes == null || cmdRes.size() != 1) {
                        throw new IllegalStateException(String.format("Error response from %1$s:\n%2$s",
                                FIRST_COMMIT_CMD, cmdRes == null ? "<null>" : StringUtils.join(cmdRes, "\n")));
                    }
                    return cmdRes.get(0);
                });
    }
}
