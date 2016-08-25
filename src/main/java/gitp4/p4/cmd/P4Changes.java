package gitp4.p4.cmd;

import gitp4.CmdRunner;
import gitp4.p4.P4ChangeInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by chriskang on 8/23/2016.
 */
public class P4Changes {
    private static final String HEAD = "#head";
    private static final String SLASH = "/";
    private static final String P4_CHANGES_CMD = "p4 changes %s";

    public static List<P4ChangeInfo> run(String parameters) throws Exception {
        String cmdParams = StringUtils.isBlank(parameters) ? "" : parameters;

        return CmdRunner.run(() -> String.format(P4_CHANGES_CMD, cmdParams),
                (cmdRes) -> {
                    LinkedList<P4ChangeInfo> result = new LinkedList<>();
                    for (String line : cmdRes) {
                        P4ChangeInfo change = P4ChangeInfo.create(line);
                        result.add(0, change);
                    }
                    return result;
                });
    }

    private static String parseFromTo(String from, String to) {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isBlank(from)) {
            if (HEAD.equals(from)) sb.append(HEAD);
            else sb.append("@").append(from);
        }
        if (sb.length() > 0 && !StringUtils.isBlank(to)) {
            sb.append(",");
            if (HEAD.equals(to)) sb.append(HEAD);
            else sb.append("@").append(to);
        }
        return sb.toString();
    }
}
