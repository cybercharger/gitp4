package gitp4.p4;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by chriskang on 8/24/2016.
 */
public class P4RepositoryInfo {
    private static final String SLASH = "/";

    public String getPath() {
        return path;
    }

    private final String path;

    public P4RepositoryInfo(String path) {
        if (StringUtils.isBlank(path)) throw new NullPointerException("path");
        this.path = path.endsWith(SLASH) ? path : path + SLASH;
    }
}
