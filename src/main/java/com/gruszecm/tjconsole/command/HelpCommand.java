package com.gruszecm.tjconsole.command;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.gruszecm.tjconsole.TJContext;

import jline.Completor;

public class HelpCommand extends AbstractCommand implements Completor{
	private List<CommandHelp> commandHelps;

	public HelpCommand(TJContext ctx, PrintStream output) {
		super(ctx, output);
		commandHelps = new ArrayList<CommandHelp>();
	}
	
	public void addCommand(AbstractCommand command) {
		commandHelps.add(command.getHelp());
	}
	
	public void action(String input) throws Exception {
		for(CommandHelp h : commandHelps) {
			output.append(h.getFull()).append(" - ").append(h.getDescription()).append('\n');
		}
	}

	@Override
	public CommandHelp getHelp() {
		return new CommandHelp("This help.", "?", "?");
	}

	@Override
	public boolean matches(String input) {
		return input.equalsIgnoreCase("help") || input.equals("?");
	}

	@SuppressWarnings("unchecked")
	public int complete(String buffer, int cursor, List candidates) {
		buffer = buffer.trim().toLowerCase();
		if (buffer.indexOf(' ') < 0) {
			for(CommandHelp h : commandHelps) {
				if (! h.isProper(ctx)) continue;
				if (h.getPrefix().toLowerCase().startsWith(buffer) && ! h.getPrefix().equalsIgnoreCase(buffer)) {
					candidates.add(h.getPrefix() + " ");
				}
			}
		}
		if (candidates.isEmpty() || buffer.length() == 0) return -1; // go on
		else return 0;
	}

}
