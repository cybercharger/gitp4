package gitp4;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by chriskang on 9/27/2016.
 */
public class TempFileManager {
    private final static String TMP_DIR = "tmp";
    private final static Path tmpDirPath = Paths.get(Profile.gitP4DirPath.toString(), TMP_DIR);


    private TempFileManager() throws IOException {
        if (!Files.exists(tmpDirPath)) {
            Files.createDirectories(tmpDirPath);
        }
    }

    private static TempFileManager instance = null;

    public static TempFileManager getInstance() throws IOException {
        if (instance == null) {
            synchronized (TempFileManager.class) {
                if (instance == null) instance = new TempFileManager();
            }
        }
        return instance;
    }

    public Path writeTempFile(String fileName, byte[] content) throws IOException {
        if (content == null) throw new NullPointerException("content");
        Path path = Paths.get(tmpDirPath.toString(), fileName);
        Files.write(path, content, StandardOpenOption.CREATE);
        return path;
    }

    public Path writeTempFile(String fileName, Iterable<? extends CharSequence> lines, Charset charset) throws IOException {
        if (lines == null) throw new NullPointerException("content");
        Path path = Paths.get(tmpDirPath.toString(), fileName);
        Files.write(path, lines, charset, StandardOpenOption.CREATE);
        return path;
    }

    public boolean deleteTempFile(Path filePath) throws IOException {
        return filePath == null || Files.deleteIfExists(filePath);
    }
}
