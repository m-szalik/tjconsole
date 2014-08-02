package org.jsoftware.tjconsole;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.cli.*;
import org.jsoftware.tjconsole.command.CmdDescription;
import org.jsoftware.tjconsole.command.CommandAction;
import org.jsoftware.tjconsole.command.definition.*;
import org.jsoftware.tjconsole.util.MyDateConverter;

import javax.management.ObjectName;
import java.io.*;
import java.util.*;
import java.util.prefs.BackingStoreException;

/**
 * Main application class
 *
 * @author szalik
 */
public class TJConsole {
    private ConsoleReader reader = new ConsoleReader();
    private final TJContext context;
    private Output output;
    private List<CommandDefinition> commandDefinitions;
    private String promptPattern;

    private TJConsole(Properties props) throws IOException, BackingStoreException {
        promptPattern = props.getProperty("prompt.pattern", "> ");
        context = new TJContext();
        commandDefinitions = new ArrayList<CommandDefinition>();
        List<CmdDescription> cmdDescriptions = new ArrayList<CmdDescription>();
        add(cmdDescriptions, new HelpCommandDefinition(cmdDescriptions));
        add(cmdDescriptions, new QuitCommandDefinition());
        add(cmdDescriptions, new ConnectCommandDefinition());
        add(cmdDescriptions, new UseCommandDefinition());
        add(cmdDescriptions, new GetAttributeCommandDefinition());
        add(cmdDescriptions, new SetAttributeCommandDefinition());
        add(cmdDescriptions, new DescribeCommandDefinition());
        add(cmdDescriptions, new InvokeOperationCommandDefinition());
        add(cmdDescriptions, new EnvCommandDefinition());
        for(CommandDefinition cd : commandDefinitions) {
            Completer completer = cd.getCompleter(context);
            if (completer != null) {
                reader.addCompleter(completer);
            }
        }
        updatePrompt();
        try {
            CommandDefinition cd = findCommandDefinition("use");
            if (cd instanceof Observable) {
                ((Observable) cd).addObserver(new UpdatePromptObserver());
            }
        } catch (Exception e) {
            // FIXME
        }
    }


    private void add(List<CmdDescription> cmdDescriptions, Object cc) {
        if (cc instanceof CommandDefinition) {
            CommandDefinition cd = (CommandDefinition) cc;
            commandDefinitions.add(cd);
            cmdDescriptions.add(cd.getDescription());
        }
        if (cc instanceof Completer) {
            reader.addCompleter((Completer) cc);
        }
    }


    public void waitForCommands() throws IOException, EndOfInputException{
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                throw new EndOfInputException(true);
            }
            try {
                CommandAction action = findCommandAction(line.trim());
                if (action != null) {
                    action.doAction(context, output);
                }
            } catch (ParseInputCommandNotFoundException ex) {
                output.outError("Command not found");
            } catch (ParseInputCommandCreationException ex) {
                output.outError("Cannot parse " + ex.getInput());
            } catch (Exception ex) { // command execution problem
                output.outError(ex.getLocalizedMessage());
                ex.printStackTrace(); // FIXME remove
            }
        }
    }


    private CommandDefinition findCommandDefinition(String input) throws ParseInputCommandNotFoundException, ParseInputCommandCreationException {
        CommandDefinition cmdDef = null;
        for (CommandDefinition cd : commandDefinitions) {
            if (cd.matches(input)) {
                cmdDef = cd;
                break;
            }
        }
        if (cmdDef == null) {
            if (input != null && input.trim().length() > 0) {
                throw new ParseInputCommandNotFoundException(input);
            }
            return null;
        } else {
            return cmdDef;
        }
    }




    private CommandAction findCommandAction(String input) throws ParseInputCommandNotFoundException, ParseInputCommandCreationException {
        CommandDefinition cmdDef = findCommandDefinition(input);
        if (cmdDef == null) {
            if (input != null && input.trim().length() > 0) {
                throw new ParseInputCommandNotFoundException(input);
            }
            return null;
        } else {
            try {
                return cmdDef.action(input);
            } catch (Exception e) {
                throw new ParseInputCommandCreationException(input, e);
            }
        }
    }


    @SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception {
        ConvertUtils.deregister(Date.class);
        ConvertUtils.register(new MyDateConverter(), Date.class);
        Properties props = new Properties();
        props.load(TJConsole.class.getResourceAsStream("/tjconsole.properties"));
        Options options = new Options();
        options.addOption(OptionBuilder.withDescription("Display this help and exit.").create('h'));
        options.addOption(OptionBuilder.withDescription("Connect to mBean server. (example --connect <jvm_pid> --connect <host>:<port>").hasArgs(1).create("connect"));
        options.addOption(OptionBuilder.withDescription("Use mBean.").withArgName("beanName").hasArgs(1).create("use"));
        options.addOption(OptionBuilder.withDescription("Use mBean.").withArgName("beanName").hasArgs(1).create("bean"));
        options.addOption(OptionBuilder.withDescription("Run script (javaScript or groovy) from file.").withArgName("file").hasArgs(1).create("script"));
        options.addOption(OptionBuilder.withDescription("Show local jvm java processes list and exit.").create("ps"));
        options.addOption(OptionBuilder.withDescription("Do not use colors for output.").create("xterm"));
        options.addOption(OptionBuilder.withDescription("Jmx authentication username").withArgName("username").hasArgs(1).create("username"));
        options.addOption(OptionBuilder.withDescription("Jmx authentication password").withArgName("password").hasArgs(1).create("password"));
        options.addOption(OptionBuilder.withDescription("Display this help and exit.").create('h'));


        TJConsole tjConsole = new TJConsole(props);
        boolean scriptMode = false;
        Output consoleOutput = null;
        List<CommandAction> actions = new LinkedList<CommandAction>();
        try {
            if (args.length > 0) {
                CommandLineParser parser = new GnuParser();
                CommandLine cli;
                try {
                    cli = parser.parse(options, args);
                    boolean colors = ! cli.hasOption("script") && ! cli.hasOption("no-colors");
                    consoleOutput = new Output(System.out, colors);
                    if (cli.hasOption("username")) {
                        actions.add(tjConsole.findCommandAction("set USERNAME " + cli.getOptionValue("username")));
                    }
                    if (cli.hasOption("password")) {
                        actions.add(tjConsole.findCommandAction("set PASSWORD " + cli.getOptionValue("password")));
                    }
                    if (cli.hasOption("connect")) {
                        String cliArg = cli.getOptionValue("connect");
                        actions.add(tjConsole.findCommandAction("connect " + cliArg));
                    }
                    if (cli.hasOption("bean")) {
                        actions.add(tjConsole.findCommandAction("use " + cli.getOptionValue("bean")));
                    }
                    if (cli.hasOption("use")) {
                        actions.add(tjConsole.findCommandAction("use " + cli.getOptionValue("use")));
                    }
                    if (cli.hasOption("script")) {
                        actions.add(tjConsole.findCommandAction("run " + cli.getOptionValue("script")));
                        scriptMode = true;
                    }
                    if (cli.hasOption("ps")) {
                        actions.clear();
                        actions.add(tjConsole.findCommandAction("ps"));
                        scriptMode = true;
                    }
                    if (cli.hasOption('h')) {
                        printHelp(options, System.out);
                        System.exit(0);
                    }
                } catch (ParseException exp) {
                    System.err.println("Parsing failed.  Reason: " + exp.getMessage());
                    printHelp(options, System.err);
                    System.exit(1);
                }
            } else {
                consoleOutput = new Output(System.out, true);
                scriptMode = false;
            }
            // init
            tjConsole.output = consoleOutput;
            if (! scriptMode) {
                System.err.println(props.getProperty("message.welcome", "Welcome to tJconsole"));
            }
            for(CommandAction action : actions) {
                action.doAction(tjConsole.context, tjConsole.output);
            }
            if (! scriptMode) {
                tjConsole.waitForCommands();
            }
        } catch(EndOfInputException ex) {
            if (! scriptMode && ex.isPrintExitMessage()) {
                consoleOutput.outApp("\nBye.");
            }
        } finally {
            if (consoleOutput != null) {
                consoleOutput.close();
            }
        }
    }


    private static void printHelp(Options options, PrintStream outStream) {
        PrintWriter out = new PrintWriter(outStream);
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(out, 80, "tjconsole", "TJConsole - text jconsole.", options, 3, 2, "", false);
        out.flush();
    }



    class UpdatePromptObserver implements Observer {
        @Override
        public void update(Observable o, Object arg) {
            if (o instanceof UseCommandDefinition) {
                updatePrompt();
            }
        }
    }

    private void updatePrompt() {
        ObjectName objectName = context.getObjectName();
        String str = promptPattern.replace("%b", objectName == null ? "" : objectName.getCanonicalName());
        reader.setPrompt(str);
    }

}
