package com.gruszecm.tjconsole.command;

import java.io.PrintStream;

import com.gruszecm.tjconsole.TJConsole;
import com.gruszecm.tjconsole.TJContext;

public class QuitCommand extends AbstractCommand {
	private TJConsole console;
	
	public QuitCommand(TJContext context, PrintStream output, TJConsole console) {
		super(context, output);
		this.console = console;
	}

	@Override
	public void action(String input) throws Exception {
		console.quit();
	}
	
	@Override
	public boolean matches(String input) {
		return input.equalsIgnoreCase("\\q") || input.equalsIgnoreCase("quit");
	}
	
	@Override
	public CommandHelp getHelp() {
		return new CommandHelp("Quit.", "\\q", "\\q");
	}
	
	
}
