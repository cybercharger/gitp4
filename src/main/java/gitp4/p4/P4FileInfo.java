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
            "\\.{3}+\\s(?<%1$s>//.+)#(?<%2$s>\\d+) (?<%3$s>\\S+)",
            fileGroupId, revisionGroupId, operationGroupId);
    private static final Pattern pattern = Pattern.compile(patternString);

    private static final String MOVE_SLASH = "move/";

    private final String file;
    private final int revision;
    private final P4Operation operation;

    public static P4FileInfo create(String infoString, String p4Depo) {
        if (StringUtils.isBlank(infoString) || StringUtils.isBlank(p4Depo)) return null;
        Matcher matcher = pattern.matcher(infoString);
        if (!matcher.matches()) return null;
        String file = matcher.group(fileGroupId);
        if (!file.startsWith(p4Depo)) return null;
        int revision = Integer.parseInt(matcher.group(revisionGroupId));
        P4Operation operation = parseP4Operation(matcher.group(operationGroupId));
        return new P4FileInfo(file, revision, operation);
    }

    private P4FileInfo(String file, int revision, P4Operation operation) {
        this.file = file;
        this.revision = revision;
        this.operation = operation;
    }

    public String getFile() {
        return file;
    }

    public int getRevision() {
        return revision;
    }

    public P4Operation getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return String.format("%1$s, rev: %2$d, op: %3$s", file, revision, operation);
    }

    private static P4Operation parseP4Operation(String s) {
        s = s.startsWith(MOVE_SLASH) ? s.substring(MOVE_SLASH.length()) : s;
        return P4Operation.valueOf(s);
    }
}
