package com.gruszecm.tjconsole.command;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.MBeanAttributeInfo;

import com.gruszecm.tjconsole.DataOutputService;
import com.gruszecm.tjconsole.TJContext;

public class GetAttributeCommand extends AbstractAttributeCommand {

	public GetAttributeCommand(TJContext context, PrintStream output) {
		super(context, output);
	}

	@Override
	protected String getPrefix() {
		return "GET";
	}

	@Override
	public void actionBean(String input, String att) throws Exception {
		List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>(ctx.getAttributes());
		if (att.length() > 0) {
			for(Iterator<MBeanAttributeInfo> it=attributes.iterator(); it.hasNext();) {
				if (! it.next().getName().startsWith(att)) it.remove();
			}
		}
		for(MBeanAttributeInfo ai : attributes) {
			output.append(ai.getName()).append(" = ");
			Object value = ctx.getServer().getAttribute(ctx.getObjectName(), ai.getName());
			DataOutputService.get(ai.getType()).output(value, output);
			output.append('\n');
			output.flush();
		}
	}
	
	@Override
	protected void actionEvn(String input, String attribute) throws Exception {
		output.append(attribute).append(" = ");
		output.append(ctx.getEnviroment().get(attribute).toString());
		output.append('\n');
		output.flush();
	}
	
	@Override
	public CommandHelp getHelp() {
		return new CommandHelp("Get attribute value", "GET [attributeName]", "GET");
	}	

}
