package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.DataOutputService;
import org.jsoftware.tjconsole.Output;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CmdDescription;
import org.jsoftware.tjconsole.command.CommandAction;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.ReflectionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Command to get attribute value of mxBean
 *
 * @author szalik
 */
public class GetAttributeCommandDefinition extends AbstractCommandDefinition {
    public GetAttributeCommandDefinition() {
        super("Get attribute value", "get [attributeName]", "get", true);
    }


    @Override
    public CommandAction action(String input) throws Exception {
        final String[] attributes = input.substring(prefix.length()).trim().split("[ ,]");
        return new CommandAction() {
            @Override
            public void doAction(TJContext ctx, Output output) throws Exception {
                StringBuilder sb = new StringBuilder();
                for (MBeanAttributeInfo ai : ctx.getAttributes()) {
                    if (skip(ai.getName())) {
                        continue;
                    }
                    sb.append(ai.getName()).append(" = ");
                    Object value = ctx.getServer().getAttribute(ctx.getObjectName(), ai.getName());
                    DataOutputService.get(ai.getType()).output(value, sb);
                    sb.append('\n');
                }
                output.outMBeanOutput(sb.toString());
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
        return new Completer() {
            @Override
            public int complete(String buffer, int cursor, List<CharSequence> candidates) {
                if (matches(buffer) && ctx.isBeanSelected()) {
                    try {
                        for (MBeanAttributeInfo ai : ctx.getAttributes()) {
                            if (ai.isReadable()) {
                                String s = prefix + ' ' + ai.getName();
                                if (s.startsWith(buffer)) {
                                    candidates.add(ai.getName());
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();    // FIXME
                    }
                }
                return cursor;
            }
        };
    }
}
