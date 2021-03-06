package gitp4.p4;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chriskang on 8/23/2016.
 */
public class P4ChangeInfo implements Comparable<P4ChangeInfo> {
    private final String p4ChangeString;
    private final String changeList;
    private final String comments;
    private final String date;
    private final P4UserInfo p4UserInfo;

    private static final String changeListGroupId = "changelist";
    private static final String dateGroupId = "date";
    private static final String userGroupId = "p4UserInfo";
    private static final String commentsGroupId = "comments";

    private static final String p4ChangePatternStr = String.format(
            "Change (?<%1$s>\\d+) on (?<%2$s>\\d{4}/\\d{2}/\\d{2}) by (?<%3$s>\\S+) (?<%4$s>.+)",
            changeListGroupId, dateGroupId, userGroupId, commentsGroupId);
    private static final Pattern p4ChangePattern = Pattern.compile(p4ChangePatternStr);


    private P4ChangeInfo(String p4ChangeString) {
        Matcher matcher = p4ChangePattern.matcher(p4ChangeString);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format("%1$s doesn't match pattern %2$s", p4ChangeString, p4ChangePatternStr));
        }
        this.p4ChangeString = p4ChangeString;
        this.changeList = matcher.group(changeListGroupId);
        this.comments = matcher.group(commentsGroupId);
        this.date = matcher.group(dateGroupId);
        this.p4UserInfo = new P4UserInfo(matcher.group(userGroupId));
    }

    public String getChangeList() {
        return changeList;
    }

    public String getComments() {
        return comments;
    }

    public String getDate() {
        return date;
    }

    public P4UserInfo getP4UserInfo() {
        return p4UserInfo;
    }

    public static P4ChangeInfo create(String p4ChangeString) {
        return new P4ChangeInfo(p4ChangeString);
    }

    @Override
    public String toString() {
        return p4ChangeString;
    }

    @Override
    public int compareTo(P4ChangeInfo o) {
        if (o == null) return -1;
        return Integer.parseInt(this.changeList) - Integer.parseInt(o.getChangeList());
    }
}
