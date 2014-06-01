package com.gruszecm.tjconsole.command;

import com.gruszecm.tjconsole.Output;
import com.gruszecm.tjconsole.TJContext;
import jline.Completor;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to print out a help message
 *
 * @author szalik
 */
public class HelpCommand extends AbstractCommand implements Completor {
    private final List<CmdDescription> cmdDescriptions;

    public HelpCommand(TJContext ctx, Output output) {
        super(ctx, output);
        cmdDescriptions = new ArrayList<CmdDescription>();
    }

    public void addCommand(AbstractCommand command) {
        cmdDescriptions.add(command.getHelp());
    }

    public void action(String input) throws Exception {
        for (CmdDescription h : cmdDescriptions) {
            StringBuilder sb = new StringBuilder();
            sb.append(h.getFull()).append(" - ").append(h.getDescription()).append('\n');
            output.outMBeanOutput(sb.toString());
        }
    }

    @Override
    public CmdDescription getHelp() {
        return new CmdDescription("This help.", "?", "?");
    }

    @Override
    public boolean matches(String input) {
        return input.equalsIgnoreCase("help") || input.equals("?");
    }

    @SuppressWarnings("unchecked")
    public int complete(String buffer, int cursor, List candidates) {
        buffer = buffer.trim().toLowerCase();
        if (buffer.indexOf(' ') < 0) {
            for (CmdDescription h : cmdDescriptions) {
                if (!h.isProper(ctx)) {
                    continue;
                }
                if (h.getPrefix().toLowerCase().startsWith(buffer) && !h.getPrefix().equalsIgnoreCase(buffer)) {
                    candidates.add(h.getPrefix() + " ");
                }
            }
        }
        return (candidates.isEmpty() || buffer.length() == 0) ? -1 /* go on */ : 0;
    }

}
