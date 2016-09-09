package gitp4;

/**
 * Created by chriskang on 9/9/2016.
 */
public class GitP4Exception extends RuntimeException {
    public GitP4Exception() {
    }

    public GitP4Exception(String message) {
        super(message);
    }

    public GitP4Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public GitP4Exception(Throwable cause) {
        super(cause);
    }

    public GitP4Exception(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
