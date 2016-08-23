package gitp4.p4.cmd;

import gitp4.p4.P4Change;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chriskang on 8/23/2016.
 */
public class P4Changes {
    public static final String P4_CHANGES_CMD = "p4 changes %1$s...@%2$d,#head";

    public static List<P4Change> run(int lastChangeListNumber, String p4Repository) throws Exception {
        if (lastChangeListNumber < 0) throw new IllegalArgumentException("lastChangeListNumber is negative");
        if (StringUtils.isBlank(p4Repository)) throw new IllegalArgumentException("p4Repository is blank");
        if (!p4Repository.endsWith("/")) p4Repository = p4Repository + "/";
        String finalP4Repository = p4Repository;
        return CmdRunner.run(() -> String.format(P4_CHANGES_CMD, finalP4Repository, lastChangeListNumber),
                (cmdRes) -> cmdRes.stream().map(P4Change::create).collect(Collectors.toCollection(LinkedList::new)));
    }
}
