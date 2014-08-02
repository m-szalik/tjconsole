package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.console.Output;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CommandAction;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import java.util.List;

/**
 * Get mxBean attribute information (name, if it is read-only or read-write)
 *
 * @author szalik
 */
public class DescribeCommandDefinition extends AbstractCommandDefinition {

    public DescribeCommandDefinition() {
        super("Describe attributes and operations of current bean.", "describe", "describe", true);
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
                output.println(out.toString());
            }
        };

    }

}
