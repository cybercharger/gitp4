package gitp4.p4;

import gitp4.GitP4Exception;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by chriskang on 9/1/2016.
 */
public class P4FileStatInfo {
    private static final String FILE_SEC_START_TAG = "... depotFile ";
    private static final String DES_SEC_START_TAG = "... desc ";

    private static final String DEPOT_FILE_TAG = FILE_SEC_START_TAG;
    private static final String CLIENT_FILE_TAG = "... clientFile ";
    private static final String ACTION_TAG = "... headAction ";
    private static final String REVISION_TAG = "... headRev ";
    private static final String CHANGELIST_TAG = "... headChange ";

    public static final P4FileStatInfo EMPTY = new P4FileStatInfo(Collections.emptyList(), "EMPTY");

    public static P4FileStatInfo create(List<String> cmdRes) {
        if (cmdRes == null) throw new NullPointerException("cmdRes");
        if (cmdRes.isEmpty()) return EMPTY;
        List<List<String>> sections = split(cmdRes);
        List<P4FileInfoEx> files = new LinkedList<>();
        String desc = "";
        for (List<String> s : sections) {
            if (s.get(0).startsWith(FILE_SEC_START_TAG)) {
                files.add(parseFile(s));
            } else {
                desc = parseDesc(s);
            }
        }
        return new P4FileStatInfo(files, desc == null ? "" : desc);
    }

    private static P4FileInfoEx parseFile(List<String> section) {
        String depotFile = null, clientFile = null, revision = null, operation = null, cl = null;
        for (String line : section) {
            if (line.startsWith(DEPOT_FILE_TAG)) depotFile = line.substring(DEPOT_FILE_TAG.length());
            else if (line.startsWith(ACTION_TAG)) operation = line.substring(ACTION_TAG.length());
            else if (line.startsWith(CLIENT_FILE_TAG)) clientFile = line.substring(CLIENT_FILE_TAG.length());
            else if (line.startsWith(REVISION_TAG)) revision = line.substring(REVISION_TAG.length());
            else if (line.startsWith(CHANGELIST_TAG)) cl = line.substring(CHANGELIST_TAG.length());
        }
        if (depotFile == null || revision == null || operation == null || cl == null) {
            throw new IllegalStateException("Failed to parse file info from\n" + StringUtils.join(section, "\n"));
        }
        if (clientFile == null) {
            throw new GitP4Exception("Cannot find client file info, please check whether files are properly mapped to local client");
        }
        return new P4FileInfoEx(depotFile, clientFile, P4Operation.parse(operation), Integer.parseInt(revision), Integer.parseInt(cl));
    }

    private static String parseDesc(List<String> desc) {
        String res = StringUtils.join(desc, "\n");
        return res.startsWith(DES_SEC_START_TAG) ? res.substring(DES_SEC_START_TAG.length()) : res;
    }

    private static List<List<String>> split(List<String> cmdRes) {
        List<List<String>> result = new LinkedList<>();
        List<String> section = null;
        Iterator<String> it = cmdRes.iterator();
        for (; it.hasNext(); ) {
            String cur = it.next().replace("\r\n", "\n").trim();
            if (cur.startsWith(DES_SEC_START_TAG)) {
                section = new LinkedList<>();
                result.add(section);
                section.add(cur);
                break; //break to allow empty lines
            } else if (cur.startsWith(FILE_SEC_START_TAG)) {
                section = new LinkedList<>();
                result.add(section);
                section.add(cur);
            } else {
                if ("".equals(cur)) continue;
                if (section == null) {
                    throw new IllegalStateException(String.format("Failed to split: %s", StringUtils.join(cmdRes, "\n")));
                }
                section.add(cur.trim());
            }
        }
        while (it.hasNext()) {
            if (section == null) {
                throw new IllegalStateException(String.format("Failed to split: %s", StringUtils.join(cmdRes, "\n")));
            }
            section.add(it.next());
        }
        return result;
    }


    private final List<P4FileInfoEx> files;
    private final String description;

    private P4FileStatInfo(List<P4FileInfoEx> files, String description) {
        this.files = Collections.unmodifiableList(files);
        this.description = description;
    }

    public List<P4FileInfoEx> getFiles() {
        return files;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof P4FileStatInfo)) return false;
        P4FileStatInfo other = (P4FileStatInfo) obj;
        if (!this.description.equals(other.description)) return false;
        if (this.files.size() != other.files.size()) return false;
        for (P4FileInfoEx file : this.files) {
            if (!other.files.contains(file)) return false;
        }
        return true;
    }
}
