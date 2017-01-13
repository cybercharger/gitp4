package gitp4.p4.cmd;

import gitp4.CmdRunner;
import gitp4.Utils;
import gitp4.p4.P4ChangeListInfo;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by chriskang on 8/23/2016.
 */
public class P4Describe {
    public static final String P4_CHANGES_CMD = Utils.getArgFormat("p4 describe -s %s");

    public static P4ChangeListInfo run(final String parameters, final String p4Depot) {
        final String cmdParams = StringUtils.isBlank(parameters) ? "" : parameters;
        return CmdRunner.getP4CmdRunner().run(() -> Utils.convertToArgArray(String.format(P4_CHANGES_CMD, cmdParams)),
                cmdRes -> new P4ChangeListInfo(cmdRes, p4Depot));
    }
}
