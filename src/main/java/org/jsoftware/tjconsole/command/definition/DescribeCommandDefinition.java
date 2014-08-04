package org.jsoftware.tjconsole.command.definition;

import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CommandAction;
import org.jsoftware.tjconsole.console.Output;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.util.ArrayList;
import java.util.Collections;
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
                List<String> outList = new ArrayList<String>();
                for (MBeanAttributeInfo ai : ctx.getAttributes()) {
                    outList.add("@|white " + ai.getName() + "|@\t" + (ai.isReadable() ? "R" : " ") + (ai.isWritable() ? "W" : " ") + "\t" + ai.getType());
                }
                for (MBeanOperationInfo oi : ctx.getServer().getMBeanInfo(ctx.getObjectName()).getOperations()) {
                    StringBuilder out = new StringBuilder();
                    out.append("@|red ").append(oi.getName());
                    MBeanParameterInfo[] parameters = oi.getSignature();
                    if (parameters.length == 0) {
                        out.append("()");
                    } else {
                        out.append("(|@");
                        for(int i=0; i<parameters.length; i++) {
                            out.append("@|blue ").append(parameters[i].getName()).append('(').append(parameters[i].getType()).append("|@");
                            if (i +1 < parameters.length) {
                                out.append(",");
                            }
                        }
                        out.append("@|red )|@");
                    }
                    out.append("\treturns ").append(oi.getReturnType());
                    outList.add(out.toString());
                }
                Collections.sort(outList);
                for(String s : outList) {
                    output.println(s);
                }
            }
        };

    }

}
