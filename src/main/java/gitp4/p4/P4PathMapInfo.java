package gitp4.p4;

/**
 * Created by chriskang on 9/7/2016.
 */
public class P4PathMapInfo {
    private final String depotPath;
    private final String clientPath;
    private final String localPath;

    public P4PathMapInfo(String depotPath, String clientPath, String localPath) {
        this.depotPath = depotPath;
        this.clientPath = clientPath;
        this.localPath = localPath;
    }

    public String getDepotPath() {
        return depotPath;
    }

    public String getClientPath() {
        return clientPath;
    }

    public String getLocalPath() {
        return localPath;
    }
}
