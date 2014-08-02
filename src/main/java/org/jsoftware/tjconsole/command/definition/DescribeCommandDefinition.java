package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.DataOutputService;
import org.jsoftware.tjconsole.Output;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CmdDescription;
import org.jsoftware.tjconsole.command.CommandAction;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Get mxBean attribute information (name, if it is read-only or read-write)
 *
 * @author szalik
 */
public class DescribeCommandDefinition extends AbstractCommandDefinition {

    public DescribeCommandDefinition() {
        super("Get attributes and operations information.", "describe [attributeName]", "describe", true);
    }


    @Override
    public CommandAction action(String input) throws Exception {
        return new CommandAction() {
            @Override
            public void doAction(TJContext ctx, Output output) throws Exception {
                StringBuilder out = new StringBuilder();
                for (MBeanAttributeInfo ai : ctx.getAttributes()) {
                    out.append(ai.getName()).append("\t attribute ").append(ai.isReadable() ? "R" : " ").append(ai.isWritable() ? "W" : " ").append(" ").append(ai.getType()).append('\n');
                }
                for (MBeanOperationInfo oi : ctx.getServer().getMBeanInfo(ctx.getObjectName()).getOperations()) {
                    out.append(oi.getName()).append("\t operation returns:").append(oi.getReturnType()).append('\n'); // TODO
                }
                output.outMBeanOutput(out.toString());
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
                            candidates.add(ai.getName());
                        }
                        for (MBeanOperationInfo oi : ctx.getServer().getMBeanInfo(ctx.getObjectName()).getOperations()) {
                            candidates.add(oi.getName());
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
