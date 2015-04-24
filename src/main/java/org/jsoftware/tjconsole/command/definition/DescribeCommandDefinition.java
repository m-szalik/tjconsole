package org.jsoftware.tjconsole.command.definition;

import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CommandAction;
import org.jsoftware.tjconsole.console.Output;

import javax.management.*;
import javax.management.openmbean.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Get mxBean attribute information (name, if it is read-only or read-write)
 *
 * @author szalik
 */
public class DescribeCommandDefinition extends AbstractCommandDefinition {
    private static final int SPACE = 18;

    public DescribeCommandDefinition() {
        super("Describe attributes and operations of current bean.", "describe", "describe", true);
    }


    @Override
    public CommandAction action(String input) {
        return new CommandAction() {
            @Override
            public void doAction(TJContext ctx, Output output) throws Exception {
                List<String> outList = new ArrayList<String>();
                for (MBeanAttributeInfo ai : ctx.getAttributes()) {
                    StringBuilder sb = new StringBuilder("@|cyan ").append(ai.getName()).append(" |@");
                    for (int i = ai.getName().length(); i < SPACE; i++) {
                        sb.append(' ');
                    }
                    sb.append(" ").append(ai.isReadable() ? "R" : " ").append(ai.isWritable() ? "W" : " ").append("  ");
                    describeType(ctx, ai.getName(), ai.getType(), sb);
                    outList.add(sb.toString());
                }
                for (MBeanOperationInfo oi : ctx.getServer().getMBeanInfo(ctx.getObjectName()).getOperations()) {
                    StringBuilder out = new StringBuilder();
                    out.append("@|green ").append(oi.getName());
                    MBeanParameterInfo[] parameters = oi.getSignature();
                    if (parameters.length == 0) {
                        out.append("()|@");
                    } else {
                        out.append("(|@");
                        for (int i = 0; i < parameters.length; i++) {
                            out.append("@|blue ").append(parameters[i].getName()).append(':').append(parameters[i].getType()).append("|@");
                            if (i + 1 < parameters.length) {
                                out.append(",");
                            }
                        }
                        out.append("@|green )|@");
                    }
                    out.append("  ");
                    for (int i = oi.getName().length(); i < SPACE+2; i++) {
                        out.append(' ');
                    }
                    describeType(ctx, oi.getName(), oi.getReturnType(), out);
                    outList.add(out.toString());
                }
                Collections.sort(outList);
                for (String s : outList) {
                    output.println(s);
                }
            }
        };

    }

    private void describeType(TJContext ctx, String name, String type, StringBuilder out) throws AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, IOException {
        if (type.startsWith("javax.management.openmbean.TabularData")) {
            TabularType tt = ((TabularData) ctx.getServer().getAttribute(ctx.getObjectName(), name)).getTabularType();
            describeType(tt, out);
        } else if (type.startsWith("javax.management.openmbean.CompositeData")) {
            CompositeType ct = ((CompositeData) ctx.getServer().getAttribute(ctx.getObjectName(), name)).getCompositeType();
            describeType(ct, out);
        } else {
            out.append(type);
        }
    }

    private static void describeType(Object type, StringBuilder out) {
        if (type instanceof TabularType) {
            TabularType tt = (TabularType) type;
            out.append("TabularData ").append(tt.getTypeName()).append(" (");
            appendDescription(tt.getDescription(), out).append(" of ");
            describeType(tt.getRowType(), out);
            out.append(")");
            return;
        }
        if (type instanceof CompositeType) {
            CompositeType ct = (CompositeType) type;
            out.append("CompositeData ").append(ct.getTypeName()).append(" (");
            appendDescription(ct.getDescription(), out).append(" contains (");
            List<String> keys = new LinkedList<String>(ct.keySet());
            Collections.sort(keys);
            for(int i=0; i<keys.size(); i++) {
                String key = keys.get(i);
                out.append("@|cyan ").append(key).append("|@=");
                describeType(ct.getType(key), out);
                if (i<keys.size() -1) {
                    out.append(',');
                }
            }
            out.append(")");
            return;
        }
        if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) type;
            out.append(arrayType.getTypeName()).append(" (");
            appendDescription(arrayType.getDescription(), out).append(" array of ");
            describeType(arrayType.getElementOpenType(), out);
            out.append(')');
            return;
        }
        if (type instanceof SimpleType) {
            SimpleType simpleType = (SimpleType) type;
            String typeName = simpleType.getTypeName();
            if (typeName.startsWith("java.lang.")) {
                typeName = typeName.substring("java.lang.".length());
            }
            out.append(typeName);
            if (! simpleType.getTypeName().equals(simpleType.getDescription())) {
                out.append(' ');
                appendDescription(simpleType.getDescription(), out);
            }
            return;
        }
        if (type instanceof OpenType) { // other OpenTypes - for future usage
            OpenType openType = (OpenType) type;
            out.append(openType.getTypeName()).append(" (");
            appendDescription(openType.getDescription(), out);
            out.append(" of ").append(openType.getClassName()).append(")");
            return;
        }
        out.append(type.toString());
    }

    private static StringBuilder appendDescription(String desc, StringBuilder out) {
        out.append("@|magenta '").append(desc).append("'|@");
        return out;
    }

}
