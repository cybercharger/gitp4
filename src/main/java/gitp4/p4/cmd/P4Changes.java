package gitp4.p4.cmd;

import gitp4.CmdRunner;
import gitp4.p4.P4Change;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chriskang on 8/23/2016.
 */
public class P4Changes {
    private static final String HEAD = "#head";
    private static final String SLASH = "/";
    private static final String P4_CHANGES_CMD = "p4 changes %1$s...%2$s";

    public static List<P4Change> run(String p4Repository, String to, String from) throws Exception {
        if (StringUtils.isBlank(p4Repository)) throw new IllegalArgumentException("p4Repository is blank");
        final String cmdP4Repository = p4Repository.endsWith(SLASH) ? p4Repository : p4Repository + SLASH;
        final String fromTo = parseFromTo(from, to);

        return CmdRunner.run(() -> String.format(P4_CHANGES_CMD, cmdP4Repository, fromTo),
                (cmdRes) -> {
                    LinkedList<P4Change> result = new LinkedList<>();
                    for (String line : cmdRes) {
                        P4Change change = P4Change.create(line);
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
        if (sb.length() > 0 && !StringUtils.isBlank(to)){
            sb.append(",");
            if (HEAD.equals(to)) sb.append(HEAD);
            else sb.append("@").append(from);
        }
        return sb.toString();
    }
}
