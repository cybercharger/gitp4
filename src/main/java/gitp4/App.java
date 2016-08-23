package gitp4;

import gitp4.p4.P4Change;
import gitp4.p4.P4ChangeListInfo;
import gitp4.p4.P4FileInfo;
import gitp4.p4.cmd.P4Changes;
import gitp4.p4.cmd.P4Describe;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    private static final Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) {
        try {
            List<P4Change> result = P4Changes.run(313591, "//nucleus/SANDBOX/testgitp4");
            logger.info("P4 changes result \n" +
                    String.join("\n", result.stream().map(P4Change::toString).collect(Collectors.toCollection(LinkedList::new))));

            for(P4Change change : result) {
                P4ChangeListInfo clinfo = P4Describe.run(change);
                logger.info(String.format("p4 describe %s\n", change.getChangeList()) +
                String.join("\n", clinfo.getFiles().stream().map(P4FileInfo::toString).collect(Collectors.toCollection(LinkedList::new))));

            }
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
