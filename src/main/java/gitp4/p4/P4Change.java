package gitp4.p4;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chriskang on 8/23/2016.
 */
public class P4Change {
    private final String p4ChangeString;
    private final String changeList;
    private final String comments;
    private final String date;
    private final String user;

    private static final String changeListGroupId = "changelist";
    private static final String dateGroupId = "date";
    private static final String userGroupId = "user";
    private static final String commentsGroupId = "comments";

    private static final String p4ChangePatternStr = String.format(
            "Change (?<%1$s>\\d+) on (?<%2$s>\\d{4}/\\d{2}/\\d{2}) by (?<%3$s>\\S+) (?<%4$s>.+)",
            changeListGroupId, dateGroupId, userGroupId, commentsGroupId);
    private static final Pattern p4ChangePattern = Pattern.compile(p4ChangePatternStr);


    private P4Change(String p4ChangeString) {
        Matcher matcher = p4ChangePattern.matcher(p4ChangeString);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format("%1$s doesn't match pattern %2$s", p4ChangeString, p4ChangePatternStr));
        }
        this.p4ChangeString = p4ChangeString;
        this.changeList = matcher.group(changeListGroupId);
        this.comments = matcher.group(commentsGroupId);
        this.date = matcher.group(dateGroupId);
        this.user = matcher.group(userGroupId);
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

    public String getUser() {
        return user;
    }

    public static P4Change create(String p4ChangeString) {
        return new P4Change(p4ChangeString);
    }

    @Override
    public String toString() {
        return p4ChangeString;
    }
}
