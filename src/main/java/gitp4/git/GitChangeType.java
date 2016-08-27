package gitp4.git;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Created by ChrisKang on 8/27/2016.
 */
public enum GitChangeType {
    Add("A"),
    Delete("D"),
    Modify("M"),
    Rename("R\\d+");

    private final Pattern pattern;
    GitChangeType(String patternString) {
        pattern = Pattern.compile(patternString);
    }

    public boolean matches(String input) {
        return !StringUtils.isBlank(input) && this.pattern.matcher(input).matches();
    }

    public static GitChangeType parse(String stringValue) {
        for(GitChangeType type : GitChangeType.values()) {
            if (type.matches(stringValue)) return type;
        }
        throw new IllegalArgumentException("Cannot parse GitChangeType from string " + stringValue);
    }
}
