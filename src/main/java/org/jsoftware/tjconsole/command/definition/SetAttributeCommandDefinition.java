package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.console.Output;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CommandAction;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import java.util.List;

/**
 * Operation to set mxBean attribute value
 *
 * @author szalik
 */
public class SetAttributeCommandDefinition extends AbstractCommandDefinition {

    public SetAttributeCommandDefinition() {
        super("Set attribute value.", "Set attributeName newValue", "set", true);
    }


    @Override
    public CommandAction action(String input) throws Exception {
        String[] data = input.substring(prefix.length()).trim().split("=", 2);
        final String attribute = data[0].trim();
        final String valueStr = data[1].trim();
        return new CommandAction() {
            @Override
            public void doAction(TJContext ctx, Output output) throws Exception {
                MBeanAttributeInfo attr = null;
                for(MBeanAttributeInfo ai : ctx.getAttributes()) {
                    if (ai.getName().equalsIgnoreCase(attribute)) {
                        attr = ai;
                        break;
                    }
                }
                if (attr == null) {
                    output.outError("No attribute found '" + attribute + "'");
                } else {
                    if (!attr.isWritable()) {
                        output.outError("Attribute " + attribute + " is read only");
                        return;
                    }
                    Object val = getValueAsType(valueStr, attr.getType());
                    Attribute attribute = new Attribute(attr.getName(), val);
                    ctx.getServer().setAttribute(ctx.getObjectName(), attribute);
                }
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
                            if (ai.isWritable()) {
                                candidates.add(ai.getName() + "=");
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



    private Object getValueAsType(String valInput, String type) {
        if ("null".equalsIgnoreCase(valInput)) {
            return null;
        }
        if ("boolean".equals(type)) {
            return Boolean.valueOf(valInput);
        }
        if ("long".equals(type)) {
            return Long.valueOf(valInput);
        }
        if ("int".equals(type)) {
            return Integer.valueOf(valInput);
        }
        return valInput;
    }

}
