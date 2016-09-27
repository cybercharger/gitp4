package gitp4;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by chriskang on 9/22/2016.
 */
public class Profile {
    public static final String CFG_FILE_NAME = "config";
    public static final String commitCommentsTemplate = "p4-%3$s: %1$s\n[p4 depot = %2$s change = %3$s from %4$s on %5$s]";
    public static final String submitHints = "p4 changelist %1$s has been created. Please re-tag last_p4_submit to %2$s after having it checked in";

    public static final String GIT_P4_SYNC_CMD_FMT = "%1$s...@%2$d,#head";
    public static final String lastSubmitTag = "last_p4_submit";

    public static final Path gitP4DirPath = Paths.get(".gitp4");
    public static final Path gitDirPath = Paths.get(".git");

    private final Path configFilePath;

    public Profile(String profileName, boolean existingCheck) {
        String fileName = StringUtils.isBlank(profileName) ?
                CFG_FILE_NAME :
                String.format("%1$s.%2$s", profileName, CFG_FILE_NAME);
        this.configFilePath = Paths.get(gitP4DirPath.toString(), fileName);
        if (existingCheck && !Files.exists(configFilePath)) {
            throw new GitP4Exception("Cannot load configuration " + configFilePath);
        }
    }

    public Path getConfigFilePath() {
        return configFilePath;
    }
}
