package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CmdDescription;
import org.jsoftware.tjconsole.command.CommandAction;
import org.jsoftware.tjconsole.console.Output;

import javax.management.ObjectName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Select mxBean to connect to.
 *
 * @author szalik
 */
public class UseCommandDefinition extends AbstractCommandDefinition {
    private Logger logger = Logger.getLogger(getClass().getName());

    public UseCommandDefinition() {
        super("Select bean.", "use <beanName>", "use", false);
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
                    ObjectName objectName = new ObjectName(bName);
                    if (ctx.getServer().isRegistered(objectName)) {
                        output.outInfo("Attached to bean " + bName);
                        ctx.setObjectName(objectName);
                        notifyObservers(objectName);
                    } else {
                        output.outError("Bean " + bName + " not found.");
                        ctx.fail(this, 15);
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
                                candidates.add(" " + s);
                            }
                        }
                    } catch (IOException e) {
                        logger.throwing(getClass().getName(), "complete - Error receiving bean names from JMX Server", e);
                    }
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
