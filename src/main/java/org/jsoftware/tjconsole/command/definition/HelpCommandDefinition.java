package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CmdDescription;
import org.jsoftware.tjconsole.command.CommandAction;
import org.jsoftware.tjconsole.console.Output;

import java.util.List;

/**
 * Command to print out a help message
 *
 * @author szalik
 */
public class HelpCommandDefinition extends AbstractCommandDefinition {
    private final List<CmdDescription> cmdDescriptions;

    public HelpCommandDefinition(List<CmdDescription> cmdDescriptions) {
        super("Print help", "h or help", "help", false);
        this.cmdDescriptions = cmdDescriptions;
    }


    public CommandAction action(String input) {
        return new CommandAction() {
            @Override
            public void doAction(TJContext tjContext, Output output) {
                for (CmdDescription h : cmdDescriptions) {
                    StringBuilder sb = new StringBuilder("@|white ").append(h.getFull()).append(" |@").append("  ");
                    while (sb.length() < 30) {
                        sb.append(' ');
                    }
                    sb.append("@|blue ").append(h.getDescription()).append(" |@");
                    output.println(sb.toString());
                }
            }
        };
    }


    @Override
    public boolean matches(String input) {
        return input.equalsIgnoreCase(prefix) || "?".equals(input);
    }


    @Override
    public Completer getCompleter(final TJContext tjContext) {
        return new Completer() {
            @Override
            public int complete(String buffer, int cursor, List<CharSequence> candidates) {
                buffer = buffer.trim().toLowerCase();
                if (buffer.indexOf(' ') < 0) {
                    for (CmdDescription h : cmdDescriptions) {
                        if (h.canBeUsed(tjContext) && h.getPrefix().toLowerCase().startsWith(buffer) && !h.getPrefix().equalsIgnoreCase(buffer)) {
                            candidates.add(h.getPrefix());
                        }
                    }
                }
                return (candidates.isEmpty() || buffer.length() == 0) ? -1 /* go on */ : 0;
            }
        };
    }

}


