package com.gruszecm.tjconsole;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.prefs.BackingStoreException;

import jline.Completor;
import jline.ConsoleReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.gruszecm.tjconsole.command.AbstractCommand;
import com.gruszecm.tjconsole.command.BeanCommand;
import com.gruszecm.tjconsole.command.ConnectCommand;
import com.gruszecm.tjconsole.command.GetAttributeCommand;
import com.gruszecm.tjconsole.command.HelpCommand;
import com.gruszecm.tjconsole.command.InfoAttributeCommand;
import com.gruszecm.tjconsole.command.OperationCommaand;
import com.gruszecm.tjconsole.command.QuitCommand;
import com.gruszecm.tjconsole.command.SetAttributeCommand;


public class TJConsole {
	private TJContext context;
	private ConsoleReader reader;
	private Output output;
	private boolean quit = false;
	private List<AbstractCommand> commands;
	
	private HelpCommand helpCommand;
	private String propmptPattern;
	
	public TJConsole(Output out) throws IOException, BackingStoreException {
		context = new TJContext();
		reader = new ConsoleReader ();
		reader.setDebug (new PrintWriter (new FileWriter ("writer.debug", true)));
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
		add(new OperationCommaand(context, output));
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
		while(! quit) {
			printPropmpt();
			String line = reader.readLine();
			processCommand(line);
		}
	}
	
	private void printPropmpt() {
		String prompt = propmptPattern;
		prompt = prompt.replace("%b", context.getObjectName() == null ? "#NONE#" : context.getObjectName().toString());
		output.outPrompt(prompt);
	}


	private boolean processCommand(String line) {
		boolean r = false;
		AbstractCommand command = null;
		for(AbstractCommand ac : commands) {
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
				output.outError("Command " + line + "error: " + e.getMessage()+"\n");
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
		options.addOption(OptionBuilder.withDescription("Connect to mBean server. (example -c LOCAL:<PID> ==> -c LOCAL:2060").hasArgs(1).create('c'));
		options.addOption(OptionBuilder.withDescription("Connect to bean.").withArgName("beanName").hasArgs(1).create('b'));
		options.addOption(OptionBuilder.withDescription("Run script from file.").withArgName("file").hasArgs(1).create('f'));
		options.addOption(OptionBuilder.withDescription("Show local java processes and exit.").create('p'));
		
		Output consoleOutput = new Output();
		com.gruszecm.tjconsole.TJConsole console = new com.gruszecm.tjconsole.TJConsole(consoleOutput);
		console.propmptPattern = props.getProperty("propmpt.pattern", "> ");
		
		if (args.length > 0) {
			CommandLineParser parser = new GnuParser();
			CommandLine cli = null;
		    try {
		        cli = parser.parse( options, args );
		    }
		    catch( ParseException exp ) {
		        System.err.println( "Parsing failed.  Reason: " + exp.getMessage());
		        printHelp(options, System.err);
		    }
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
		    		if (! console.processCommand(s))  {
		    			System.exit(1);
		    		}
		    	}
		    	System.exit(0);
		    }
		    if (cli.hasOption('p')) {
		    	ProcessListManager processListManager = new ProcessListManager();
		    	for(String ps : processListManager.getLocalProcesses()) {
		    		System.out.println(ps);
		    	}
		    	System.exit(0);
		    }
		}
		
		System.err.println(props.getProperty("message.welcome","Welcome to tjconsole"));
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
