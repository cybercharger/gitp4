package gitp4;

import gitp4.p4.P4Change;
import gitp4.p4.P4ChangeListInfo;
import gitp4.p4.P4FileInfo;
import gitp4.p4.cmd.P4Changes;
import gitp4.p4.cmd.P4Describe;
import gitp4.p4.cmd.P4Print;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class App {
    private static final Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        GitP4Bridge bridge = new GitP4Bridge();
        bridge.clone("//nucleus/SANDBOX/testgitp4");
//        UUID uuid = UUID.randomUUID();
//        logger.info("Execution id: " + uuid);
//        Path outputDir = Paths.get(".\\" + uuid);
//        try {
//            Files.createDirectory(outputDir);
//            List<P4Change> result = P4Changes.run("//nucleus/SANDBOX/testgitp4", null, null);
//            logger.info("P4 changes result \n" +
//                    String.join("\n", result.stream().map(P4Change::toString).collect(Collectors.toCollection(LinkedList::new))));
//
//            for (P4Change change : result) {
//                P4ChangeListInfo clInfo = P4Describe.run(change);
//                logger.info(String.format("p4 describe %s\n", change.getChangeList()) +
//                        String.join("\n", clInfo.getFiles().stream().map(P4FileInfo::toString).collect(Collectors.toCollection(LinkedList::new))));
//                for(P4FileInfo fileInfo : clInfo.getFiles()) {
//                    String p4File = String.format("%1$s#%2$d", fileInfo.getFile(), fileInfo.getRevision());
//                    String outputFileName = Paths.get(fileInfo.getFile()).getFileName().toString();
//                    P4Print.run(p4File, Paths.get(outputDir.toString(), clInfo.getChangelist(), outputFileName).toString());
//                }
//            }
//
//        } catch (Exception e) {
//            logger.error(e);
//        } finally {
//            try {
//                Files.deleteIfExists(outputDir);
//            } catch (IOException e) {
//                logger.error("Failed to delete directory " + outputDir, e);
//            }
//        }
    }
}
