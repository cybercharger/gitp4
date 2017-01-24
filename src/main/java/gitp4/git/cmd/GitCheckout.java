package gitp4.git.cmd;

import java.util.List;

/**
 * Created by chriskang on 8/26/2016.
 */
public class GitCheckout {
    public static void run(final List<String> parameters) {
        if (parameters == null || parameters.isEmpty()) throw new NullPointerException("parameters");
        GitOperation.run(GitOperation.Operation.Checkout, parameters);
    }
}
