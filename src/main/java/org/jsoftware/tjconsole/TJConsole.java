package org.jsoftware.tjconsole;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.cli.*;
import org.jsoftware.tjconsole.command.CmdDescription;
import org.jsoftware.tjconsole.command.CommandAction;
import org.jsoftware.tjconsole.command.definition.*;
import org.jsoftware.tjconsole.console.EndOfInputException;
import org.jsoftware.tjconsole.console.Output;
import org.jsoftware.tjconsole.console.ParseInputCommandCreationException;
import org.jsoftware.tjconsole.console.ParseInputCommandNotFoundException;
import org.jsoftware.tjconsole.util.MyDateConverter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.BackingStoreException;

/**
 * Main application class
 *
 * @author szalik
 */
public class TJConsole {
    private final ConsoleReader reader;
    private final List<CommandDefinition> commandDefinitions;
    private final TJContext context;
    private final Properties properties;
    private Output output;


    TJConsole(Properties props) throws BackingStoreException, IOException {
        this.reader = new ConsoleReader();
        this.properties = props;
        this.context = new TJContext();
        this.context.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                UpdateEnvironmentEvent event = (UpdateEnvironmentEvent) arg;
                if (event.getKey().equals("DATE_FORMAT")) {
                    String df = (String) event.getCurrent();
                    SimpleDateFormat sdf = new SimpleDateFormat(df);
                    MyDateConverter.getInstance().setCustom(sdf);
                }
            }
        });
        this.context.setEnvironmentVariable("DATE_FORMAT", "yyyy-MM-dd'T'HH:mm:ss", false);
        envToSystemProperty(this.context, "javax.net.ssl.trustStorePassword", "TRUST_STORE_PASSWORD", "");
        envToSystemProperty(this.context, "javax.net.ssl.trustStore", "TRUST_STORE", System.getProperty("user.home") + File.separator + ".trustStore");
        this.commandDefinitions = new ArrayList<CommandDefinition>();
        List<CmdDescription> cmdDescriptions = new ArrayList<CmdDescription>();
        add(cmdDescriptions, new QuitCommandDefinition());
        add(cmdDescriptions, new ConnectCommandDefinition());
        add(cmdDescriptions, new UseCommandDefinition());
        add(cmdDescriptions, new GetAttributeCommandDefinition());
        add(cmdDescriptions, new SetAttributeCommandDefinition());
        add(cmdDescriptions, new DescribeCommandDefinition());
        add(cmdDescriptions, new InvokeOperationCommandDefinition());
        add(cmdDescriptions, new EnvCommandDefinition());
        add(cmdDescriptions, new InfoCommandDefinition());
        add(cmdDescriptions, new PsCommandDefinition());
        add(cmdDescriptions, new HelpCommandDefinition(cmdDescriptions));
        for (CommandDefinition cd : this.commandDefinitions) {
            Completer completer = cd.getCompleter(this.context);
            if (completer != null) {
                this.reader.addCompleter(completer);
            }
        }

    }

    public TJContext getContext() {
        return context;
    }

    private void envToSystemProperty(TJContext context, final String systemProperty, final String envKey, String defaultValue) {
        String sysProp = System.getProperty(systemProperty);
        if (sysProp == null) {
            System.setProperty(systemProperty, defaultValue);
            sysProp = defaultValue;
        }
        context.setEnvironmentVariable(envKey, sysProp, false);
        context.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                UpdateEnvironmentEvent event = (UpdateEnvironmentEvent) arg;
                if (event.getKey().equals(envKey)) {
                    System.setProperty(systemProperty, event.getCurrent().toString());
                }
            }
        });
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

    public CommandAction executeCommand(String command, Output output) throws Exception {
        CommandAction action = findCommandAction(command);
        if (action != null) {
            action.doAction(context, output);
        }
        return action;
    }

    public void waitForCommands() throws IOException, EndOfInputException {
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            CommandAction action = null;
            try {
                action = executeCommand(line.trim(), output);
            } catch (EndOfInputException ex) {
                throw ex;
            } catch (ParseInputCommandNotFoundException ex) {
                output.outError("Command not found");
                context.fail(action, 2);
            } catch (ParseInputCommandCreationException ex) {
                output.outError("Cannot parse " + ex.getInput());
                context.fail(action, 3);
            } catch (Exception ex) { // command execution problem
                output.outError(ex.getLocalizedMessage());
                context.fail(action, 99);
            }
        }
        throw new EndOfInputException();
    }


    private CommandDefinition findCommandDefinition(String input) throws ParseInputCommandNotFoundException {
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


    /**
     * Invoked from TJConsoleLauncher
     */
    @SuppressWarnings({"static-access", "unused"})
    public static void start(String[] args) throws Exception {
        ConvertUtils.deregister(Date.class);
        ConvertUtils.register(MyDateConverter.getInstance(), Date.class);
        Properties props = new Properties();
        InputStream propsInputStream = null;
        try {
            propsInputStream = TJConsole.class.getResourceAsStream("/tjconsole.properties");
            props.load(propsInputStream);
        } finally {
            if (propsInputStream != null) {
                propsInputStream.close();
            }
        }
        Options options = new Options();
        options.addOption(OptionBuilder.withDescription("Display this help and exit.").create('h'));
        options.addOption(OptionBuilder.withDescription("Connect to mBean server. (example --connect <jvm_pid> --connect <host>:<port>").hasArgs(1).create("connect"));
        options.addOption(OptionBuilder.withDescription("Use mBean.").withArgName("beanName").hasArgs(1).create("use"));
        options.addOption(OptionBuilder.withDescription("Use mBean.").withArgName("beanName").hasArgs(1).create("bean"));
        options.addOption(OptionBuilder.withDescription("Show local jvm java processes list and exit.").create("ps"));
        options.addOption(OptionBuilder.withDescription("Do not use colors for output.").create("xterm"));
        options.addOption(OptionBuilder.withDescription("Jmx authentication username").withArgName("username").hasArgs(1).create("username"));
        options.addOption(OptionBuilder.withDescription("Jmx authentication password").withArgName("password").hasArgs(1).create("password"));
        options.addOption(OptionBuilder.withDescription("Command to be executed (multiple occurrences allowed)").withArgName("command").hasArgs(Option.UNLIMITED_VALUES).create("cmd"));
        options.addOption(OptionBuilder.withDescription("Display this help and exit.").create('h'));


        TJConsole tjConsole = new TJConsole(props);
        boolean scriptMode = false;
        Output consoleOutput = null;
        List<CommandAction> actions = new LinkedList<CommandAction>();
        List<String> inputToExecute = new LinkedList<String>();
        try {
            if (args.length > 0) {
                CommandLineParser parser = new GnuParser();
                CommandLine cli;
                try {
                    cli = parser.parse(options, args);
                    boolean colors = !cli.hasOption("script") && !cli.hasOption("xterm");
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
                    if (cli.hasOption("cmd")) {
                        for(String cmdOp : cli.getOptionValues("cmd")) {
                            inputToExecute.add(cmdOp.trim());
                            System.err.println(">> " + cmdOp);
                        }
                        scriptMode = true;
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
            if (!scriptMode) {
                tjConsole.initInteractiveMode();
            } else {
                consoleOutput.setFilterInfo(true);
            }
            for (CommandAction action : actions) {
                try {
                    action.doAction(tjConsole.context, tjConsole.output);
                } catch (Exception ex) {
                    tjConsole.context.fail(action, 99);
                    throw ex;
                }
            }
            for(String cmdInput : inputToExecute) {
                CommandAction commandAction = tjConsole.findCommandAction(cmdInput);
                commandAction.doAction(tjConsole.context, tjConsole.output);
            }
            if (! scriptMode) {
                tjConsole.waitForCommands();
            }
        } catch (EndOfInputException ex) {
            if (! scriptMode) {
                consoleOutput.println("\nBye.");
            }
        } finally {
            if (consoleOutput != null) {
                consoleOutput.close();
                tjConsole.reader.shutdown();
            }
        }
        int exitCode = tjConsole.context.getExitCode();
        System.exit(exitCode);
    }

    private void initInteractiveMode() {
        output.println(properties.getProperty("message.welcome", "Welcome to tJconsole"));
        reader.setPrompt(properties.getProperty("prompt.pattern", "> "));
    }


    private static void printHelp(Options options, PrintStream outStream) {
        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(outStream, "UTF-8"));
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp(out, 80, "tjconsole", "TJConsole - text jconsole.", options, 3, 2, "", false);
            out.flush();
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

}
