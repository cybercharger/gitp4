package gitp4.p4;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by chriskang on 8/23/2016.
 */
public class P4ChangeListInfo {
    private static final String changelistGroupId = "changelist";
    private static final String userGroupId = "p4UserInfo";
    private static final String dateGroupId = "date";
    private static final String changePatternStr = String.format(
            "Change\\s(?<%1$s>\\d+)\\s+by\\s+(?<%2$s>.+)\\s+on\\s+(?<%3$s>.+)$",
            changelistGroupId, userGroupId, dateGroupId);
    private static Pattern changePattern = Pattern.compile(changePatternStr);

    private static final int commentStartLine = 1;
    private static final String commentEndTag = "Affected files ...";


    private final List<P4FileInfo> files;
    private final String changelist;
    private final String timestamp;
    private final P4UserInfo p4UserInfo;
    private final String fullComments;

    public P4ChangeListInfo(List<String> cmdRes, String p4Depot) {
        if (cmdRes == null || cmdRes.isEmpty()) throw new IllegalArgumentException("cmdRes is null or empty");
        Matcher matcher = changePattern.matcher(cmdRes.get(0));
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Cannot parse changelist info from " + cmdRes.get(0));
        }
        changelist = matcher.group(changelistGroupId);
        timestamp = matcher.group(dateGroupId);
        p4UserInfo = new P4UserInfo(matcher.group(userGroupId));
        files = StringUtils.isBlank(p4Depot) ?
                Collections.emptyList() :
                cmdRes.stream()
                        .map(cur -> P4FileInfo.create(cur, p4Depot))
                        .filter(cur -> cur != null)
                        .collect(Collectors.toCollection(LinkedList::new));

        List<String> comments = new LinkedList<>();
        for (int i = commentStartLine; i < cmdRes.size(); ++i) {
            String line = cmdRes.get(i);
            if (line.contains(commentEndTag)) break;
            if (StringUtils.isBlank(line)) continue;
            comments.add(line);
        }
        fullComments = StringUtils.join(comments, "\n");
    }

    public List<P4FileInfo> getFiles() {
        return files;
    }

    public String getChangelist() {
        return changelist;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public P4UserInfo getP4UserInfo() {
        return p4UserInfo;
    }

    public String getFullComments() {
        return fullComments;
    }
}