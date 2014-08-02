package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.DataOutputService;
import org.jsoftware.tjconsole.console.Output;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CommandAction;

import javax.management.MBeanAttributeInfo;

/**
 * Command to get attribute value of mxBean
 *
 * @author szalik
 */
public class GetAttributeCommandDefinition extends AbstractCommandDefinition {
    public GetAttributeCommandDefinition() {
        super("Get attribute value", "get <attributeName>", "get", true);
    }


    @Override
    public CommandAction action(String input) throws Exception {
        final String[] attributes = input.substring(prefix.length()).trim().split("[ ,]");
        return new CommandAction() {
            @Override
            public void doAction(TJContext ctx, Output output) throws Exception {
                StringBuilder sb = new StringBuilder("@|yellow ");
                for (MBeanAttributeInfo ai : ctx.getAttributes()) {
                    if (skip(ai.getName())) {
                        continue;
                    }
                    Object value = ctx.getServer().getAttribute(ctx.getObjectName(), ai.getName());
                    DataOutputService.get(ai.getType()).output(value, sb);
                    sb.append(" |@");
                }
                output.println(sb.toString());
            }

            private boolean skip(String aiName) {
                if (attributes.length == 0) {
                    return false;
                }
                for(String a : attributes) {
                    if (a.equalsIgnoreCase(aiName)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    @Override
    public Completer getCompleter(final TJContext ctx) {
        return new AbstractAttributeCompleter(ctx, prefix, "") {
            @Override
            protected boolean condition(MBeanAttributeInfo ai) {
                return ai.isReadable();
            }
        };
    }
}
