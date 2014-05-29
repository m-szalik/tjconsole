package com.gruszecm.tjconsole.command;

import java.util.ArrayList;
import java.util.List;

import jline.Completor;

import com.gruszecm.tjconsole.Output;
import com.gruszecm.tjconsole.TJContext;

public class HelpCommand extends AbstractCommand implements Completor{
	private final List<CommandHelp> commandHelps;

	public HelpCommand(TJContext ctx, Output output) {
		super(ctx, output);
		commandHelps = new ArrayList<CommandHelp>();
	}
	
	public void addCommand(AbstractCommand command) {
		commandHelps.add(command.getHelp());
	}
	
	public void action(String input) throws Exception {
		for(CommandHelp h : commandHelps) {
			StringBuilder sb = new StringBuilder();
			sb.append(h.getFull()).append(" - ").append(h.getDescription()).append('\n');
			output.outMBeanOutput(sb.toString());
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
