package gitp4.git;

import gitp4.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Created by ChrisKang on 8/27/2016.
 */
public class GitFileInfo {
    public static final String CMD_PARAM = Utils.getArgFormat("--pretty= --name-status --no-renames");
    private final GitChangeType changeType;
    private final String file;

    public GitFileInfo(String cmdRes) {
        if (StringUtils.isBlank(cmdRes)) throw new NullPointerException("cmdRes");
        String[] res = cmdRes.split("\\s+");
        changeType = GitChangeType.parse(res[0].trim());
        if (GitChangeType.Rename.equals(changeType)) {
            throw new IllegalStateException("Git operation 'rename' is not supported");
        }
        file = cmdRes.substring(res[0].length() + 1).trim();
        if (res.length > 2) {
            Logger.getLogger(GitFileInfo.class).warn("Double check path containing white space: " + cmdRes);
        }
    }

    public GitChangeType getChangeType() {
        return changeType;
    }

    public String getFile() {
        return file;
    }


    @Override
    public String toString() {
        return String.format("%1$s: %2$s", changeType, file);
    }
}
