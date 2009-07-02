package com.gruszecm.tjconsole.command;

import java.io.Serializable;

import com.gruszecm.tjconsole.TJContext;

public class CommandHelp implements Serializable {
	private static final long serialVersionUID = 974727180626249506L;
	private String description;
	private String full;
	private String prefix;
	
	public CommandHelp(String description, String full, String prefix) {
		super();
		this.description = description;
		this.full = full;
		this.prefix = prefix;
	}
	
	public String getDescription() {
		return description;
	}
	public String getFull() {
		return full;
	}
	public String getPrefix() {
		return prefix;
	}

	public boolean isProper(TJContext ctx) {
		return true;
	}
	
}
