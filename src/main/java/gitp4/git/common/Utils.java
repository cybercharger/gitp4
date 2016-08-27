package gitp4.git.common;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by ChrisKang on 8/27/2016.
 */
public class Utils {
    public static boolean isValidGitCommitId(String commitId) {
        return !StringUtils.isBlank(commitId) &&
                (Constants.fullCommitIdPattern.matcher(commitId).matches()
                        || Constants.abbrCommitIdPattern.matcher(commitId).matches());
    }
}
