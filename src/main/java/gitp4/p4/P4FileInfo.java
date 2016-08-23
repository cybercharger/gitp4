package gitp4.p4;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chriskang on 8/23/2016.
 */
public class P4FileInfo {
    private static final String fileGroupId = "file";
    private static final String revisionGroupId = "revision";
    private static final String operationGroupId = "operation";

    private static final String patternString = String.format(
            ".+\\s(?<%1$s>//.+)#(?<%2$s>\\d+) (?<%3$s>\\S+)",
            fileGroupId, revisionGroupId, operationGroupId);
    private static final Pattern pattern = Pattern.compile(patternString);

    private final String file;
    private final int revision;
    private final String operation;

    public static boolean isValid(String infoString) {
        return pattern.matcher(infoString).matches();
    }

    public P4FileInfo(String infoString) {
        if (StringUtils.isBlank(infoString)) throw new NullPointerException("infoString");
        Matcher matcher = pattern.matcher(infoString);
        if (!matcher.matches()) throw new IllegalArgumentException(String.format("invalid p4 file info %s", infoString));
        file = matcher.group(fileGroupId);
        revision = Integer.parseInt(matcher.group(revisionGroupId));
        operation = matcher.group(operationGroupId);
    }

    public String getFile() {
        return file;
    }

    public int getRevision() {
        return revision;
    }

    public String getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return String.format("%1$s, rev: %2$d, op: %3$s", file, revision, operation);
    }
}
