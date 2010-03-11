package com.gruszecm.tjconsole.command;

import com.gruszecm.tjconsole.Output;
import com.gruszecm.tjconsole.TJContext;

public abstract class AbstractCommand {
	protected TJContext ctx;;
	protected Output output;

	public AbstractCommand(TJContext ctx, Output output) {
		super();
		this.ctx = ctx;
		this.output = output;
	}

	public abstract boolean matches(String input);
	
	public abstract void action(String input) throws Exception;

	public abstract CommandHelp getHelp();
	
}
