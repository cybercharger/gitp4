package gitp4.p4.cmd;

import gitp4.CmdRunner;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chriskang on 8/29/2016.
 */
public class P4Change {
    private static final String CREATE_EMPTY_CL_CMD = "p4 change -i";
    private static final String changelistGroupId = "changelist";
    private static final Pattern pattern = Pattern.compile(String.format("Change (?<%s>\\d+) created\\.", changelistGroupId));

    public static String createEmptyChangeList(String description) {
        if (StringUtils.isBlank(description)) throw new NullPointerException("des");

        List<String> spec = CmdRunner.run(() -> "p4 change -o", cmdRes -> cmdRes);
        List<String> newSpec = new LinkedList<>();
        for (String line : spec) {
            if (line.contains("<enter description here>")) break;
            newSpec.add(line);
        }
        newSpec.add("\t" + description);

        final String cmd = CREATE_EMPTY_CL_CMD;
        return CmdRunner.run(() -> cmd,
                cmdRes -> {
                    if (cmdRes == null || cmdRes.size() != 1) {
                        throw new IllegalStateException(String.format("Invalid return of running '%s'", cmd));
                    }
                    Matcher matcher = pattern.matcher(cmdRes.get(0));
                    if (!matcher.matches()) {
                        throw new IllegalStateException(String.format("Invalid return of running '%1$s'\n%2$s", cmd, cmdRes.get(0)));
                    }
                    return matcher.group(changelistGroupId);
                }, StringUtils.join(newSpec, "\n"));
    }
}
