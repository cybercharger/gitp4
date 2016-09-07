package gitp4.git.cmd;

import gitp4.CmdRunner;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * Created by chriskang on 8/26/2016.
 */
public class GitTag {
    private static final String GIT_TAG_CMD = "git tag -a %1$s -m\"%2$s\"";

    public static void run(final String tagName, final String comments) {
        if (StringUtils.isBlank(tagName)) throw new NullPointerException("tagName");
        if (StringUtils.isBlank(comments)) throw new NullPointerException("comments");
        CmdRunner.run(() -> String.format(GIT_TAG_CMD, tagName, comments),
                (cmdRes) -> {
                    if (cmdRes != null && !cmdRes.isEmpty()) {
                        Logger.getLogger(GitTag.class).error("Failed to create tag " + tagName);
                        throw new RuntimeException(StringUtils.join(cmdRes, "\n"));
                    }
                    return "";
                });
    }
}
