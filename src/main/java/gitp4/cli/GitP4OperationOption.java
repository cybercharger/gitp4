package gitp4.cli;

import gitp4.GitP4Exception;
import gitp4.TempFileManager;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Created by chriskang on 9/9/2016.
 */
public abstract class GitP4OperationOption {
    protected static final String HELP_ARG = "help";
    protected static final String PROFILE_ARG = "profile";
    protected static final String MAX_THREADS_ARG = "max-threads";
    protected static final int DEF_MAX_THREADS = 10;
    protected static final String PAGE_SIZE_ARG = "page-size";
    protected static final int DEF_PAGE_SIZE = 20;
    protected static final String P4_SYNC_DELAY_ARG = "p4-sync-delay";
    protected static final int DEF_P4_SYNC_DELAY = 50;
    protected static final String KEEP_TMP_FILES_ARG = "keep-tmp";

    private final Options helpOptions = new Options();
    protected final Options options = new Options();
    protected CommandLine line;

    private final String[] args;
    private final String cmd;

    public GitP4OperationOption(String cmd, String[] args) {
        if (StringUtils.isBlank(cmd)) throw new NullPointerException("cmd");
        this.cmd = cmd;
        this.args = args;
        Option helpOption = Option.builder("h").argName(HELP_ARG).longOpt(HELP_ARG).desc("Show this help").required().build();
        helpOptions.addOption(helpOption);

        options.addOption(Option.builder("p")
                .argName(PROFILE_ARG)
                .longOpt(PROFILE_ARG)
                .hasArg()
                .desc("Profile name")
                .build());

        options.addOption(Option.builder()
                .argName(MAX_THREADS_ARG)
                .longOpt(MAX_THREADS_ARG)
                .hasArg()
                .type(int.class)
                .desc("Max threads number while copying files")
                .build());

        options.addOption(Option.builder()
                .argName(PAGE_SIZE_ARG)
                .longOpt(PAGE_SIZE_ARG)
                .hasArg()
                .desc("Page size for paging operation such as git add/rm, p4 add/edit/delete on multiple files")
                .build());

        options.addOption(Option.builder()
                .argName(P4_SYNC_DELAY_ARG)
                .longOpt(P4_SYNC_DELAY_ARG)
                .hasArg()
                .desc("Milliseconds of delay after p4 sync")
                .build());

        options.addOption(Option.builder()
                .argName(KEEP_TMP_FILES_ARG)
                .longOpt(KEEP_TMP_FILES_ARG)
                .desc("keep all temp files, which can be found @ .gitp4/tmp/")
                .build());
    }

    final public Options getOptions() {
        return this.options;
    }

    final public boolean parse() {
        CommandLineParser parser = new DefaultParser();
        try {
            line = parser.parse(helpOptions, args);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(cmd, options);
            return false;
        } catch (ParseException e) {
            try {
                line = parser.parse(options, args, false);
                baseOnParse();
            } catch (ParseException exp) {
                Logger.getLogger(this.getClass()).error("Parsing failed: " + exp.getMessage());
                throw new GitP4Exception(exp.getMessage());
            }
        }

        return true;
    }

    public String getProfile() {
        if (line == null) throw new IllegalStateException("Call parse first");
        return line.hasOption(PROFILE_ARG) ? line.getOptionValue(PROFILE_ARG) : "";
    }

    public int getMaxThreads() {
        return getPositiveIntValue(MAX_THREADS_ARG, DEF_MAX_THREADS);
    }

    public int getPageSize() {
        return getPositiveIntValue(PAGE_SIZE_ARG, DEF_PAGE_SIZE);
    }

    public int getP4SyncDelay() {
        return getPositiveIntValue(P4_SYNC_DELAY_ARG, DEF_P4_SYNC_DELAY);
    }

    private int getPositiveIntValue(String optionName, int defValue) {
        if (line == null) throw new IllegalStateException("Call parse first");
        if (!line.hasOption(optionName)) return defValue;
        int ret = Integer.parseInt(line.getOptionValue(optionName));
        if (ret <= 0) throw new GitP4Exception(String.format("%s must be greater than 0", optionName));
        return ret;
    }

    private void baseOnParse() throws ParseException {
        if (line != null && line.getOptionValue(KEEP_TMP_FILES_ARG) != null) {
            System.setProperty(TempFileManager.KEEP_TMP_FLAG, Boolean.TRUE.toString());
        }
        onParse();
    }

    protected void onParse() throws ParseException {
    }

}
