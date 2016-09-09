package gitp4;

import gitp4.p4.P4RepositoryInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by chriskang on 8/25/2016.
 */
public class GitP4Config {
    public static final String p4Repo = "gitp4.p4repo";
    public static final String viewMap = "gitp4.viewmap";
    public static final String lastSync = "gitp4.last.sync";
    public final static char COMMA = ',';

    static Properties load(Path filePath) throws IOException {
        InputStream is = Files.newInputStream(filePath);
        Properties config = new Properties();
        config.load(is);
        return config;
    }

    static void save(Properties config, Path filePath, String comments) throws IOException {
        if (config == null) throw new NullPointerException("config");
        OutputStream os = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        config.store(os, comments);
        os.flush();
        os.close();
    }

    static Set<String> getViewsWithRoot(Path filePath) throws IOException {
        return getViews(filePath, (viewString, config) -> {
            String root = new P4RepositoryInfo(config.getProperty(p4Repo)).getPath();
            return Arrays.stream(StringUtils.split(viewString, COMMA)).map(cur -> root + cur).collect(Collectors.toSet());
        });
    }

    static Set<String> getViews(Path filePath) throws IOException {

        return getViews(filePath, (viewString, config) -> Arrays.stream(StringUtils.split(viewString, COMMA)).collect(Collectors.toSet()));
    }

    private static Set<String> getViews(Path filePath, BiFunction<String, Properties, Set<String>> mapper) throws IOException {
        Properties config = load(filePath);
        String viewString = config.getProperty(viewMap);
        if (StringUtils.isBlank(viewString)) return Collections.emptySet();
        return mapper.apply(viewString, config);
    }
}
