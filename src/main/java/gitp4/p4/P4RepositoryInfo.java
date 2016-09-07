package gitp4.p4;

import gitp4.p4.cmd.P4Where;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created by chriskang on 8/24/2016.
 */
public class P4RepositoryInfo {
    public static final String TRIPLE_DOTS = "...";
    private static final String VALID_PATH_HINT = "Supported formats are:\n" +
            "//path/to/your/repository/...\n" +
            "//path/to/your/repository/...#head\n" +
            "//path/to/your/repository/...@12345\n" +
            "//path/to/your/repository/...@12345,@23456\n" +
            "//path/to/your/repository/...@12345,#head";

    private static final Pattern[] rangePatterns = new Pattern[]{
            Pattern.compile("#head"),
            Pattern.compile("@\\d+"),
            Pattern.compile("@\\d+,@\\d+"),
            Pattern.compile("@\\d+,#head")
    };
    private final String path;
    private final String pathWithSubContents;
    private P4PathMapInfo pathMapInfo;

    private static String parsePath(String path) {
        if (StringUtils.isBlank(path)) throw new NullPointerException("path");
        if (!path.contains(TRIPLE_DOTS)) {
            throw new IllegalArgumentException(String.format("%1$s is not a supported format for p4 repo. %2$s",
                    path, VALID_PATH_HINT));
        }
        String[] sections = StringUtils.split(path, TRIPLE_DOTS);
        if (sections.length == 2) {
            Optional<Pattern> optional = Arrays.stream(rangePatterns).filter(cur -> cur.matcher(sections[1]).matches()).findFirst();
            if (!optional.isPresent()) {
                throw new IllegalArgumentException(String.format("%1$s is not a supported format for p4 repo. %2$s",
                        path, VALID_PATH_HINT));
            }
        } else if (sections.length != 1) {
            throw new IllegalArgumentException(String.format("%1$s is not a supported format for p4 repo. %2$s",
                    path, VALID_PATH_HINT));
        }

        return sections[0];
    }

    public P4RepositoryInfo(String path) {
        this.path = parsePath(path);
        this.pathWithSubContents = this.path + TRIPLE_DOTS;
    }

    public String getPath() {
        return path;
    }

    public String getPathWithSubContents() {
        return pathWithSubContents;
    }

    public P4PathMapInfo getPathMap() {
        if (pathMapInfo == null) {
            synchronized (this) {
                if (pathMapInfo == null) pathMapInfo = retrievePathMap();
            }
        }
        return pathMapInfo;
    }

    private P4PathMapInfo retrievePathMap() {
        List<String> input = P4Where.run(pathWithSubContents);
        if (input == null || input.isEmpty()) throw new NullPointerException("input");
        if (input.size() != 1) throw new IllegalArgumentException("input has more than 1 line");
        String[] sections = StringUtils.split(input.get(0), TRIPLE_DOTS);
        if (sections.length != 3) {
            throw new IllegalArgumentException("Not supported format: " + input.get(0));
        }
        return new P4PathMapInfo(sections[0].trim(), sections[1].trim(), sections[2].trim());
    }
}
