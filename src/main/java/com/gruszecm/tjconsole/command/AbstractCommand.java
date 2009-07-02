package com.gruszecm.tjconsole.command;

import java.io.PrintStream;

import com.gruszecm.tjconsole.TJContext;

public abstract class AbstractCommand {
	protected TJContext ctx;;
	protected PrintStream output;

	public AbstractCommand(TJContext ctx, PrintStream output) {
		super();
		this.ctx = ctx;
		this.output = output;
	}

	public abstract boolean matches(String input);
	
	public abstract void action(String input) throws Exception;

	public abstract CommandHelp getHelp();
	
}
