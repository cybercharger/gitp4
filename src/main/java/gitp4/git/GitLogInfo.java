package gitp4.git;

import gitp4.git.common.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chriskang on 8/26/2016.
 */
public class GitLogInfo {
    public static final String CMD_PARAM = "--pretty=oneline";
    private static final String commitGroupId = "commit";
    private static final String commentGroupId = "comments";
    private static final Pattern pattern = Pattern.compile(String.format(
            "(?<%1$s>%2$s)\\s+(?<%3$s>.+)", commitGroupId, Constants.FULL_COMMIT_ID_PTRN, commentGroupId));

    private final String commit;
    private final String comment;

    public GitLogInfo(String cmdRes) {
        if (StringUtils.isBlank(cmdRes)) throw new NullPointerException("cmdRes");
        Matcher matcher = pattern.matcher(cmdRes);
        if (!matcher.matches()) throw new IllegalArgumentException("Invalid git log output: " + cmdRes);
        commit = matcher.group(commitGroupId);
        comment = matcher.group(commentGroupId);
    }

    public String getCommit() {
        return commit;
    }

    public String getComment() {
        return comment;
    }
}
