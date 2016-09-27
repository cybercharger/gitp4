package gitp4.p4.cmd;

/**
 * Created by chriskang on 9/6/2016.
 */
public class P4Edit {
    public static void run(String file, String changelist) {
        P4Modify.run(P4Modify.ModifyAction.edit, file, changelist);
    }

    public static void batch(Iterable<? extends CharSequence> files, String changelist) throws Exception {
        P4Modify.batch(P4Modify.ModifyAction.edit, files, changelist);
    }
}
