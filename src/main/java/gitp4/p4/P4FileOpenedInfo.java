package gitp4.p4;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chriskang on 8/29/2016.
 */
public class P4FileOpenedInfo {
    private static final String fileGroupId = "file";
    private static final Pattern pattern = Pattern.compile(String.format("(?<%s>.+)\\#\\d+\\s+\\-\\s+.+", fileGroupId));

    private final String file;

    private P4FileOpenedInfo(String file) {
         this.file = file;
    }

    public static P4FileOpenedInfo create(String cmdRes) {
        if (StringUtils.isBlank(cmdRes)) throw new NullPointerException("cmdRes");
        Matcher matcher = pattern.matcher(cmdRes);
        return matcher.matches() ? new P4FileOpenedInfo(matcher.group(fileGroupId)) : null;
    }

    public String getFile() {
        return file;
    }

    @Override
    public String toString() {
        return this.file;
    }
}
