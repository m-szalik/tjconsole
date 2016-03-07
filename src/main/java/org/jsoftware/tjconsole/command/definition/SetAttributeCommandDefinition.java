package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CommandAction;
import org.jsoftware.tjconsole.console.Output;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;

/**
 * Operation to set mxBean attribute value
 *
 * @author szalik
 */
public class SetAttributeCommandDefinition extends AbstractCommandDefinition {

    public SetAttributeCommandDefinition() {
        super("Set attribute value.", "set <attributeName>=<newValue>", "set", true);
    }


    @Override
    public CommandAction action(String input) {
        String[] data = input.substring(prefix.length()).trim().split("=", 2);
        final String attribute = data[0].trim();
        final String valueStr = data[1].trim();
        return new CommandAction() {
            @Override
            public void doAction(TJContext ctx, Output output) throws Exception {
                MBeanAttributeInfo attr = null;
                for (MBeanAttributeInfo ai : ctx.getAttributes()) {
                    if (ai.getName().equalsIgnoreCase(attribute)) {
                        attr = ai;
                        break;
                    }
                }
                if (attr == null) {
                    output.outError("No attribute found '" + attribute + "'");
                    ctx.fail(this, 30);
                } else {
                    if (!attr.isWritable()) {
                        output.outError("Attribute " + attribute + " is read only");
                        ctx.fail(this, 31);
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
        return new AbstractAttributeCompleter(ctx, prefix, " ") {
            @Override
            protected boolean condition(MBeanAttributeInfo ai) {
                return ai.isWritable();
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
