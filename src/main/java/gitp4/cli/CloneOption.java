package gitp4.cli;

import gitp4.GitP4Config;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chriskang on 9/9/2016.
 */
public class CloneOption extends GitP4OperationOption {
    private final static String CLONE_ARG = "clone-string";
    private final static String VIEW_ARG = "view-map";

    public CloneOption(String[] args) {
        super("clone", args);
        Option cloneString = new Option("c", CLONE_ARG, true, "root p4 repository path");
        cloneString.setArgName(CLONE_ARG);
        cloneString.setRequired(true);
        super.options.addOption(cloneString);
        Option view = new Option("v", VIEW_ARG, true, "comma separated list of sub directories, NO SPACE");
        view.setArgName(VIEW_ARG);
        view.setRequired(false);
        super.options.addOption(view);
    }

    public String getCloneString() {
        if (line == null) throw new NullPointerException("line");
        return line.getOptionValue(CLONE_ARG);
    }

    public List<String> getViewMap() {
        if (line == null) throw new NullPointerException("line");
        if (!line.hasOption(VIEW_ARG)) return Collections.emptyList();
        String viewString = line.getOptionValue(VIEW_ARG);
        if (viewString == null) throw new IllegalArgumentException(String.format("arg is not proved for %s", VIEW_ARG));

        return Arrays.stream(StringUtils.split(viewString, GitP4Config.COMMA)).map(String::trim).collect(Collectors.toList());
    }

    public String getViewString() {
        return line.hasOption(VIEW_ARG) ? line.getOptionValue(VIEW_ARG) : "";
    }
}
