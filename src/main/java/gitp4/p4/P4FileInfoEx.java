package gitp4.p4;

/**
 * Created by chriskang on 9/1/2016.
 */
public class P4FileInfoEx {
    private final String depotFile;
    private final String clientFile;
    private final P4Operation operation;
    private final int revision;

    public P4FileInfoEx(String depotFile, String clientFile, P4Operation operation, int revision) {
        this.depotFile = depotFile;
            this.clientFile = clientFile;
        this.operation = operation;
        this.revision = revision;
    }

    public String getDepotFile() {
        return depotFile;
    }

    public String getClientFile() {
        return clientFile;
    }

    public P4Operation getOperation() {
        return operation;
    }

    public int getRevision() {
        return revision;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof P4FileInfoEx)) return false;
        P4FileInfoEx other = (P4FileInfoEx) obj;
        return this.depotFile.equals(other.depotFile) &&
                this.clientFile.equals(other.clientFile) &&
                this.operation.equals(other.operation) &&
                this.revision == other.revision;
    }

    @Override
    public String toString() {
        return String.format("depotFile: %1$s\nclientFile: %2$s\noperation: %3$s\nrevision: %4$d\n",
                depotFile, clientFile, operation, revision);
    }}
