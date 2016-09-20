package gitp4.git;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by ChrisKang on 8/27/2016.
 */
public class GitFileInfo {
    public static final String CMD_PARAM = "--pretty= --name-status";
    private final GitChangeType changeType;
    private final String oldFile;
    private final String newFile;

    public GitFileInfo(String cmdRes) {
        if (StringUtils.isBlank(cmdRes)) throw new NullPointerException("cmdRes");
        String[] res = cmdRes.split("\\s+");
        changeType = GitChangeType.parse(res[0].trim());
        if (GitChangeType.Rename.equals(changeType)) {
            oldFile = res[1].trim();
            newFile = res[2].trim();
        } else {
            oldFile = newFile = res[1].trim();
        }
    }

    public GitChangeType getChangeType() {
        return changeType;
    }

    public String getOldFile() {
        return oldFile;
    }

    public String getNewFile() {
        return newFile;
    }

    @Override
    public String toString() {
        return GitChangeType.Rename.equals(changeType) ?
                String.format("%1$s: %2$s -> %3$s", changeType, oldFile, newFile) :
                String.format("%1$s: %2$s", changeType, oldFile);
    }
}
