package gitp4.p4.cmd;

import gitp4.CmdRunner;
import gitp4.p4.P4Change;
import gitp4.p4.P4ChangeListInfo;

/**
 * Created by chriskang on 8/23/2016.
 */
public class P4Describe {
    public static final String P4_CHANGES_CMD = "p4 describe %s";

    public static P4ChangeListInfo run(final P4Change p4Change) throws Exception {
        return CmdRunner.run(() -> String.format(P4_CHANGES_CMD, p4Change.getChangeList()), P4ChangeListInfo::new);
    }
}
