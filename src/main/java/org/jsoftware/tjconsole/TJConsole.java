package org.jsoftware.tjconsole;

import jline.Completor;
import jline.ConsoleReader;
import org.apache.commons.cli.*;
import org.jsoftware.tjconsole.command.*;
import org.jsoftware.tjconsole.local.JvmPid;
import org.jsoftware.tjconsole.local.ProcessListManager;
import org.jsoftware.tjconsole.local.ProcessListManagerLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.prefs.BackingStoreException;

/**
 * Main application class
 *
 * @author szalik
 */
public class TJConsole {
    private final TJContext context;
    private ConsoleReader reader;
    private Output output;
    private boolean quit = false;
    private List<AbstractCommand> commands;

    private final HelpCommand helpCommand;
    private String promptPattern;

    private TJConsole(Output out) throws IOException, BackingStoreException {
        context = new TJContext();
        reader = new ConsoleReader();
        output = out;
        commands = new ArrayList<AbstractCommand>();

        helpCommand = new HelpCommand(context, output);
        add(helpCommand);
        add(new QuitCommand(context, output, this));
        add(new ConnectCommand(context, output));
        add(new BeanCommand(context, output));
        add(new GetAttributeCommand(context, output));
        add(new SetAttributeCommand(context, output));
        add(new InfoAttributeCommand(context, output));
        add(new OperationCommand(context, output));
    }


    public void quit() {
        quit = true;
    }

    private void add(Object cc) {
        if (cc instanceof AbstractCommand) {
            commands.add((AbstractCommand) cc);
            helpCommand.addCommand((AbstractCommand) cc);
        }
        if (cc instanceof Completor) {
            reader.addCompletor((Completor) cc);
        }
    }

    public void read() throws IOException {
        while (!quit) {
            printPrompt();
            String line = reader.readLine();
            if (line == null) {
                System.err.println("\nBye...");
                System.exit(0);
            }
            processCommand(line);
        }
    }

    private void printPrompt() {
        String prompt = promptPattern;
        prompt = prompt.replace("%b", context.getObjectName() == null ? "#NONE#" : context.getObjectName().toString());
        output.outPrompt(prompt);
    }


    private boolean processCommand(String line) {
        boolean r = false;
        AbstractCommand command = null;
        for (AbstractCommand ac : commands) {
            if (ac.matches(line)) {
                command = ac;
                break;
            }
        }
        if (command == null) {
            if (line != null && line.trim().length() > 0) {
                output.outError("Command not found!\n");
            }
        } else {
            try {
                command.action(line);
                r = true;
            } catch (Exception e) {
                output.outError("Command " + line + "error: " + e.getMessage() + "\n");
            }
        }
        return r;
    }

    @SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.load(TJConsole.class.getResourceAsStream("/tjconsole.properties"));
        Options options = new Options();
        options.addOption(OptionBuilder.withDescription("Display this help and exit.").create('h'));
        options.addOption(OptionBuilder.withDescription("Quiet - do not display info messages.").create('q'));
        options.addOption(OptionBuilder.withDescription("Connect to mBean server. (example -c "+ProcessListManagerLoader.LOCAL_PREFIX+"<PID> ==> -c "+ ProcessListManagerLoader.LOCAL_PREFIX+"2060").hasArgs(1).create('c'));
        options.addOption(OptionBuilder.withDescription("Connect to bean.").withArgName("beanName").hasArgs(1).create('b'));
        options.addOption(OptionBuilder.withDescription("Run script from file.").withArgName("file").hasArgs(1).create('f'));
        options.addOption(OptionBuilder.withDescription("Show local java processes and exit.").create('p'));

        Output consoleOutput = new Output();
        TJConsole console = new TJConsole(consoleOutput);
        console.promptPattern = props.getProperty("prompt.pattern", "> ");

        if (args.length > 0) {
            CommandLineParser parser = new GnuParser();
            CommandLine cli;
            try {
                cli = parser.parse(options, args);
                if (cli.hasOption('q')) {
                    consoleOutput.setDisplayInfo(false);
                }
                if (cli.hasOption('h')) {
                    printHelp(options, System.out);
                    System.exit(0);
                }
                if (cli.hasOption('c')) {
                    String cliArg = cli.getOptionValue('c');
                    new ConnectCommand(console.context, console.output).action("\\c " + cliArg);
                }
                if (cli.hasOption('b')) {
                    String cliArg = cli.getOptionValue('b');
                    new BeanCommand(console.context, console.output).action("\\b " + cliArg);
                }
                if (cli.hasOption('f')) {
                    String cliArg = cli.getOptionValue('f');
                    BufferedReader fileReader = new BufferedReader(new FileReader(new File(cliArg)));
                    String s;
                    while ((s = fileReader.readLine()) != null) {
                        s = s.trim();
                        if (s.startsWith("#") || s.length() == 0) continue;
                        if (!console.processCommand(s)) {
                            System.exit(1);
                        }
                    }
                    System.exit(0);
                }
                if (cli.hasOption('p')) {
                    ProcessListManager processListManager = ProcessListManagerLoader.getProcessListManager();
                    for (JvmPid jvm : processListManager.getLocalProcessList()) {
                        System.out.println(jvm.getPid() + "  " + jvm.getCommand());
                    }
                    System.exit(0);
                }
            } catch (ParseException exp) {
                System.err.println("Parsing failed.  Reason: " + exp.getMessage());
                printHelp(options, System.err);
            }
        }
        System.err.println(props.getProperty("message.welcome", "Welcome to tJconsole"));
        console.read();
        System.out.println("Quit.");
    }


    private static void printHelp(Options options, PrintStream outStream) {
        PrintWriter out = new PrintWriter(outStream);
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(out, 80, "tjconsole", "TJConsole - text jconsole.", options, 3, 2, "", false);
        out.flush();
    }

}
