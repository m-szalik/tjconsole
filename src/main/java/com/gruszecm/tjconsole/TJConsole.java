package com.gruszecm.tjconsole;
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

import com.gruszecm.tjconsole.TJContext;
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
	private PrintStream output;
	private boolean quit = false;
	private List<AbstractCommand> commands;
	
	private HelpCommand helpCommand;
	private String propmptPattern;
	
	public TJConsole() throws IOException, BackingStoreException {
		context = new TJContext();
		reader = new ConsoleReader ();
		reader.setDebug (new PrintWriter (new FileWriter ("writer.debug", true)));
		output = System.out;
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
		output.print(prompt);
		output.flush();
	}


	private void processCommand(String line) {
		AbstractCommand command = null;
		for(AbstractCommand ac : commands) {
			if (ac.matches(line)) {
				command = ac;
				break;
			}
		}
		if (command == null) {
			if (line != null && line.trim().length() > 0) {
				output.append("Command not found!");
			}
		} else {
			try {
				command.action(line);
			} catch (Exception e) {
				System.err.println("Command " + line + "error: " + e.getMessage());
				e.printStackTrace();
			}
		}
//		output.append('\n');
		output.flush();
	}
	
	public static void main(String[] args) throws Exception {
		Properties props = new Properties();
		props.load(TJConsole.class.getResourceAsStream("/tjconsole.properties"));
		System.err.println(props.getProperty("message.welcome","Welcome to tjconsole"));
		com.gruszecm.tjconsole.TJConsole console = new com.gruszecm.tjconsole.TJConsole();
		console.propmptPattern = props.getProperty("propmpt.pattern", "> ");
		if (args.length > 0) {
			new ConnectCommand(console.context, console.output).action("\\c " + args[0]);
		}
		console.read();
		System.out.println("Quit.");
		
	}

}
