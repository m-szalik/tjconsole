package com.gruszecm.tjconsole.command;

import com.gruszecm.tjconsole.DataOutputService;
import com.gruszecm.tjconsole.Output;
import com.gruszecm.tjconsole.TJContext;

import javax.management.MBeanAttributeInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Get mxBean attribute information (name, if it is read-only or read-write)
 *
 * @author szalik
 */
public class InfoAttributeCommand extends AbstractAttributeCommand {

    public InfoAttributeCommand(TJContext context, Output output) {
        super(context, output);
    }

    @Override
    protected String getPrefix() {
        return "\\a";
    }

    @Override
    protected void actionEvn(String input, String attribute) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Environment variable - ").append(attribute).append('\n');
        output.outMBeanOutput(sb.toString());
    }

    @Override
    protected void actionBean(String input, String att) throws Exception {
        List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>(ctx.getAttributes());
        if (att.length() > 0) {
            for (Iterator<MBeanAttributeInfo> it = attributes.iterator(); it.hasNext(); ) {
                if (!it.next().getName().startsWith(att)) it.remove();
            }
        }
        StringBuilder sb = new StringBuilder();
        for (MBeanAttributeInfo ai : attributes) {
            sb.append('\t').append(ai.getName()).append("::");
            sb.append(ai.getType());
            if (ai.isWritable() && ai.isReadable()) {
                sb.append("  WR");
            } else {
                if (ai.isReadable()) {
                    sb.append("  RO");
                } else if (ai.isWritable()) {
                    sb.append("  WO");
                } else {
                    sb.append("  --");
                }
            }
            if (ai.isReadable()) {
                sb.append("  (");
                Object value = ctx.getServer().getAttribute(ctx.getObjectName(), ai.getName());
                DataOutputService.get(ai.getType()).output(value, sb);
                sb.append(')');
            }
            sb.append('\n');
            output.outMBeanOutput(sb.toString());
        }
    }

    @Override
    public CmdDescription getHelp() {
        return new CmdDescription("Get attribute information.", "\\a [attributeName]", "\\a");
    }


}
