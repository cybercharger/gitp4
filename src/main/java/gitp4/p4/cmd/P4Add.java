package gitp4.p4.cmd;


/**
 * Created by chriskang on 9/6/2016.
 */
public class P4Add {
    public static void run(String file, String changelist) {
        P4Modify.run(P4Modify.ModifyAction.add, file, changelist);
    }

    public static void batch(Iterable<? extends CharSequence> files, String changlist) throws Exception {
        P4Modify.batch(P4Modify.ModifyAction.add, files, changlist);
    }
}
