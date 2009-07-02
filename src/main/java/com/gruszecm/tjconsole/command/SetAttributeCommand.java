package com.gruszecm.tjconsole.command;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;

import org.apache.commons.beanutils.ConvertUtils;

import com.gruszecm.tjconsole.TJContext;

public class SetAttributeCommand extends AbstractAttributeCommand {

	public SetAttributeCommand(TJContext context, PrintStream output) {
		super(context, output);
	}

	@Override
	protected String getPrefix() {
		return "SET";
	}

	@Override
	protected void actionEvn(String input, String attribute) throws Exception {
		Object oldV = ctx.getEnviroment().get(attribute);
		Object newV = ConvertUtils.convert(getValue(input), oldV.getClass());
		ctx.setEvniromentVariable(attribute, newV);
		output.append("SET " + attribute + " TO " + newV).append('\n');
	}
	
	@Override
	protected void actionBean(String input, String att) throws Exception {
		List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>(ctx.getAttributes());
		if (att.length() > 0) {
			for(Iterator<MBeanAttributeInfo> it=attributes.iterator(); it.hasNext();) {
				MBeanAttributeInfo a = it.next();
				if (! a.getName().startsWith(att) || (! correctAttribute(a))) {
					it.remove();
				}
			}
		}
		if (attributes.size() != 1) {
			throw new IllegalArgumentException("Attributes " + attributes);
		}
		MBeanAttributeInfo attributeInfo = attributes.get(0);
		Object value = getValue(input);
		Attribute attribute = new Attribute(attributeInfo.getName(), value);
		ctx.getServer().setAttribute(ctx.getObjectName(), attribute);
		output.append("SET " + attributeInfo.getName() + " TO " + value).append('\n');
	}
	
	private String getValue(String input) {
		input = input.substring(getPrefix().length());
		while(input.startsWith(" ")) input = input.substring(1);
		int eqin = input.indexOf('=');
		int spin = input.indexOf(' ');
		if ((eqin > 0 && spin > eqin) || spin<0) {
			input = input.replaceFirst("=", " ");
		}
		int ind = input.indexOf(' ');
		if (ind < 0) throw new IllegalArgumentException("Value is missing!");
		input = input.substring(ind+1);
		return input;
	}

	@Override
	protected boolean correctAttribute(MBeanAttributeInfo ai) {
		return ai.isWritable();
	}
	
	@Override
	public CommandHelp getHelp() {
		return new CommandHelp("Set attribute value.", "SET attributeName newValue", "SET");
	}
	
	
	@Override
	protected String getCSuffix() {
		return " ";
	}

}
