package gitp4.p4;

/**
 * Created by chriskang on 8/24/2016.
 */
public enum P4Operation {
    add,
    edit,
    delete,
    integrate,
    branch,
    unknown;

    private static final String MOVE_SLASH = "move/";

    public static P4Operation parse(String s) {
        s = s.startsWith(MOVE_SLASH) ? s.substring(MOVE_SLASH.length()) : s;
        return P4Operation.valueOf(s);
    }
}
