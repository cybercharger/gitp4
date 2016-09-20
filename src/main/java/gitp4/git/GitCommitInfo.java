package gitp4.git;

import gitp4.Utils;

import java.util.List;

/**
 * Created by ChrisKang on 8/27/2016.
 */
public class GitCommitInfo {
    public static final String CMD_PARAM = "--pretty= --name-status";

    public GitCommitInfo(String commitId, List<String> cmdRes) {
        if (!Utils.isValidGitCommitId(commitId)) {
            throw new IllegalArgumentException("commitId is not a valid git commit id");
        }
        if (cmdRes == null || cmdRes.isEmpty()) throw new NullPointerException("cmdRes");
    }
}
