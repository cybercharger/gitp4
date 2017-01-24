package gitp4.p4;

import gitp4.GitP4Exception;
import gitp4.p4.cmd.P4Where;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * Created by chriskang on 8/24/2016.
 */
public class P4RepositoryInfo {
    public static final String SLASH = "/";
    public static final String TRIPLE_DOTS = "...";
    public static final String TRIPLE_DOTS_SPLIT_PTRN = "\\.\\.\\.";

    private static final String DEPOT_FILE_PREFIX = "... depotFile";
    private static final String CLIENT_FILE_PREFIX = "... clientFile";
    private static final String PATH_PREFIX = "... path";

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
            throw new GitP4Exception(String.format("%1$s is not a supported format for p4 repo. %2$s",
                    path, VALID_PATH_HINT));
        }
        String[] sections = path.split(TRIPLE_DOTS_SPLIT_PTRN);
        if (sections.length == 2) {
            Optional<Pattern> optional = Arrays.stream(rangePatterns).filter(cur -> cur.matcher(sections[1]).matches()).findFirst();
            if (!optional.isPresent()) {
                throw new GitP4Exception(String.format("%1$s is not a supported format for p4 repo. %2$s",
                        path, VALID_PATH_HINT));
            }
        } else if (sections.length != 1) {
            throw new GitP4Exception(String.format("%1$s is not a supported format for p4 repo. %2$s",
                    path, VALID_PATH_HINT));
        }
        if (!sections[0].endsWith(SLASH)) {
            throw new GitP4Exception(String.format("%1$s is not a supported format for p4 repo. %2$s",
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
        List<String> input = P4Where.runZtag(pathWithSubContents);
        if (input == null || input.isEmpty()) throw new NullPointerException("input");
        if (input.size() <= 3) {
            throw new IllegalArgumentException(String.format("%1$d lines of the input: %2$s", input.size(), StringUtils.join(input, "\n")));
        }

        BiFunction<List<String>, String, String> pick = (list, prefix) -> {
            String res = list.stream().filter(c -> c.startsWith(prefix)).map(c -> c.substring(prefix.length()).trim()).findFirst().orElse("");
            if (res.endsWith(TRIPLE_DOTS)) {
                res = res.substring(0, res.length() - TRIPLE_DOTS.length());
            }
            return res;
        };

        String depot = pick.apply(input, DEPOT_FILE_PREFIX);
        String client = pick.apply(input, CLIENT_FILE_PREFIX);
        String local = pick.apply(input, PATH_PREFIX);
        return new P4PathMapInfo(depot, client, local);
    }
}
