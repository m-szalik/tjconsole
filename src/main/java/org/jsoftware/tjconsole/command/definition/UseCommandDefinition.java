package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.console.Output;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CmdDescription;
import org.jsoftware.tjconsole.command.CommandAction;

import javax.management.ObjectName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Select mxBean to connect to.
 *
 * @author szalik
 */
public class UseCommandDefinition extends AbstractCommandDefinition {

    public UseCommandDefinition() {
        super("Select bean.", "use beanName", "use", false);
    }


    @Override
    public CommandAction action(final String input) throws Exception {
        return new CommandAction() {
            @Override
            public void doAction(TJContext ctx, Output output) throws Exception {
                StringBuilder sb = new StringBuilder();
                String bName = extractURL(input);
                if (bName.length() == 0) {
                    for (String bn : names(ctx)) {
                        sb.append("\t* ").append(bn).append('\n');
                        output.outInfo(sb.toString());
                    }
                } else {
                    output.outInfo("Connecting to bean " + bName + "...");
                    ObjectName objectName = new ObjectName(bName);
                    if (ctx.getServer().isRegistered(objectName)) {
                        output.outInfo("Connected to bean " + bName);
                        ctx.setObjectName(objectName);
                        notifyObservers(objectName);
                    } else {
                        output.outError("Bean " + bName + " not found.");
                        ctx.setObjectName(null);
                        notifyObservers(null);
                    }
                }
            }
        };
    }



    @Override
    public Completer getCompleter(final TJContext ctx) {
        return new Completer() {
            @Override
            public int complete(String buffer, int cursor, List<CharSequence> candidates) {
                if (matches(buffer) && ctx.isConnected()) {
                    String urlPrefix = extractURL(buffer);
                    try {
                        for (String s : names(ctx)) {
                            if (s.startsWith(urlPrefix)) {
                                candidates.add(s);
                            }
                        }
                    } catch (IOException e) { /* FIXME do not leave empty catch statements, at least log it using logger */ }
                    return prefix.length();
                } else {
                    return -1;
                }
            }
        };
    }


    private Collection<String> names(TJContext ctx) throws IOException {
        ArrayList<String> l = new ArrayList<String>();
        for (Object on : ctx.getServer().queryNames(null, null)) {
            l.add(on.toString());
        }
        return l;
    }


    private String extractURL(String in) {
        return in.substring(prefix.length()).trim();
    }

    @Override
    public CmdDescription getDescription() {
        CmdDescription d = super.getDescription();
        return new CmdDescription(d.getDescription(), d.getFull(), d.getPrefix(), false) {
            @Override
            public boolean canBeUsed(TJContext tjContext) {
                return tjContext.isConnected();
            }
        };
    }
}
