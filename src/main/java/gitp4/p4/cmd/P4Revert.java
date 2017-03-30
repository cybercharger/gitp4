package gitp4.p4.cmd;

import gitp4.CmdRunner;
import gitp4.Utils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by ChrisKang on 3/30/2017.
 */
public class P4Revert {
    public static final String P4_REVERT_A_CMD = Utils.getArgFormat("p4 revert -a -c %s");

    public static void revertUnchanged(final String changelist) {
        if (StringUtils.isBlank(changelist)) throw new NullPointerException("changelist");
        CmdRunner.getP4CmdRunner().run(() -> Utils.convertToArgArray(String.format(P4_REVERT_A_CMD, changelist)), cmdRes -> "");
    }
}
