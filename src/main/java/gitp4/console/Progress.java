package gitp4.console;

/**
 * Created by chriskang on 8/30/2016.
 */
public class Progress {
    private final int total;
    private int current = 0;
    private int lastLen = 0;

    private static final String FORMAT = "%1$d%% [%2$d/%3$d]";

    public Progress(int total) {
        if (total <= 0) throw new IllegalArgumentException("total <= 0");
        this.total = total;
    }

    public void show() {
        synchronized (this) {
            System.out.print(getBackspaces(lastLen));
            int percentage = (int) ((((double) current) / total) * 100 + 0.5);
            percentage = percentage == 100 && current < total ? 99 : percentage;
            String progress = String.format(FORMAT, percentage, current, total);
            lastLen = progress.length();
            System.out.print(progress);
        }
    }

    public void progress(int progress) {
        synchronized (this) {
            current += progress;
            current = current > total ? total : (current < 0) ? 0 : current;
            show();
            if (current == total) done();
        }
    }

    private void done() {
        synchronized (this) {
            System.out.print(getBackspaces(lastLen));
            System.out.println();
        }
    }

    private static String getBackspaces(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; ++i, sb.append("\b")) ;
        return sb.toString();
    }
}
