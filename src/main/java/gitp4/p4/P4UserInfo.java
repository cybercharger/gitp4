package gitp4.p4;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by chriskang on 8/24/2016.
 */
public class P4UserInfo {
    public static final char DELIMITER = '@';
    private final String user;
    private final String workspace;
    private final String userInfo;

    public P4UserInfo(String userInfo) {
        if (StringUtils.isBlank(userInfo)) throw new NullPointerException("userInfo");
        String[] info = StringUtils.split(userInfo, DELIMITER);
        if (info.length != 2) throw new IllegalArgumentException(userInfo);
        this.userInfo = userInfo;
        this.user = info[0];
        this.workspace = info[1];
    }

    public String getUser() {
        return user;
    }

    public String getWorkspace() {
        return workspace;
    }

    @Override
    public String toString() {
        return userInfo;
    }
}
