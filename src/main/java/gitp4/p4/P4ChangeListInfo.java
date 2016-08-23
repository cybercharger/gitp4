package gitp4.p4;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chriskang on 8/23/2016.
 */
public class P4ChangeListInfo {
    private final List<P4FileInfo> files;

    public P4ChangeListInfo(List<String> cmdRes) {
        if (cmdRes == null || cmdRes.isEmpty()) throw new IllegalArgumentException("cmdRes is null or empty");
        files = cmdRes.stream().filter(P4FileInfo::isValid).map(P4FileInfo::new).collect(Collectors.toCollection(LinkedList::new));
    }

    public List<P4FileInfo> getFiles() {
        return files;
    }
}