package gitp4.git.cmd;

import gitp4.CmdRunner;
import gitp4.Utils;
import gitp4.git.GitCommitInfo;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by ChrisKang on 8/27/2016.
 */
public class GitShow {
    private static final String GIT_SHOW_CMD = Utils.getArgFormat("git show %1$s %2$s");

    public GitCommitInfo run(final String commitId) {
        if (StringUtils.isBlank(commitId)) throw new NullPointerException("commitId");
        return CmdRunner.getGitCmdRunner().run(() -> Utils.convertToArgArray(String.format(GIT_SHOW_CMD, commitId, GitCommitInfo.CMD_PARAM)),
                cmdRes -> new GitCommitInfo(commitId, cmdRes));
    }
}
