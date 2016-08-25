package gitp4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * Created by chriskang on 8/25/2016.
 */
public class GitP4Config {
    public static final String p4Repo = "gitp4.p4repo";
    public static final String lastSync = "gitp4.last.sync";

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


}
