package gitp4.p4.cmd;

import gitp4.CmdRunner;
import gitp4.p4.P4ChangeInfo;
import gitp4.p4.P4ChangeListInfo;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by chriskang on 8/23/2016.
 */
public class P4Describe {
    public static final String P4_CHANGES_CMD = "p4 describe -s %s";

    public static P4ChangeListInfo run(final String parameters, final String p4Depo) throws Exception {
        final String cmdParams = StringUtils.isBlank(parameters) ? "" : parameters;
        return CmdRunner.run(() -> String.format(P4_CHANGES_CMD, cmdParams),
                cmdRes -> new P4ChangeListInfo(cmdRes, p4Depo));
    }
}
