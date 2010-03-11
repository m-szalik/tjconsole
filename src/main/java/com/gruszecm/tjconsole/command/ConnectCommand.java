package com.gruszecm.tjconsole.command;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import jline.Completor;

import com.gruszecm.tjconsole.ProcessListManager;
import com.gruszecm.tjconsole.TJContext;

public class ConnectCommand extends AbstractCommand implements Completor {
	private static final String PREFIX = "\\c";
	
	private List<String> remoteConnectionHistory;
	private Preferences prefs;
	private ProcessListManager processListManager = new ProcessListManager();
	
	public ConnectCommand(TJContext context, PrintStream output) throws BackingStoreException {
		super(context, output);
		remoteConnectionHistory = new ArrayList<String>();
		prefs = Preferences.userNodeForPackage(getClass());
		for(String key : prefs.keys()) {
			String rname = prefs.get(key, null);
			if (rname != null) addRemote(rname, false);
		}
	}

	private void addRemote(String rname, boolean b) {
		if (remoteConnectionHistory.contains(rname)) return;
		remoteConnectionHistory.add(rname);
		if (b) {
			try {
				prefs.put("RNAME_" + (prefs.keys().length+1), rname);
			} catch (BackingStoreException e) {			e.printStackTrace();		}
		}
	}

	@Override
	public void action(String input) throws Exception {
		Map<String,Object> env = ctx.getEnviroment();
		if (env.get("SSL").equals(Boolean.TRUE)) throw new IllegalArgumentException("SSL not supported.");
		if (env.get("USERNAME").toString().length() + env.get("PASSWORD").toString().length() > 0) throw new IllegalArgumentException("USER/PASS not supported.");
		
		String url = extractURL(input);
		if (url.length() == 0) {
			if (ctx.getServer() == null) {
				output.append("Not connected.");
			} else {
				output.append("Connected to " + ctx.getServer().getDefaultDomain());
			}
			output.append('\n');
			return;
		}
		boolean remote = false;
		output.append("Connecting to " + url + "....\n");
		MBeanServerConnection serverConnection;
		if (url.equalsIgnoreCase("local")) {
			serverConnection = ManagementFactory.getPlatformMBeanServer();
		} else {
			JMXServiceURL serviceURL;
			if (processListManager.isLocalProcess(url)) {
				serviceURL = processListManager.getLocalServiceURL(url);
			} else {
				String port = "", host = "";
				String[]urlp = url.split(":",2);
				host = urlp[0];
				if (urlp.length == 2) port = urlp[1];
				serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host+ ":"+port+"/jmxrmi");
				remote = true;
			}
			JMXConnector connector = JMXConnectorFactory.connect(serviceURL);
			serverConnection = connector.getMBeanServerConnection();
		}
		if (serverConnection != null) {
			output.append("Connected to " + url + " - " + serverConnection.getDefaultDomain() + "\n");
			ctx.setServer(serverConnection);
			if (remote) {
				addRemote(url, true);
			}
		}
	}

	@Override
	public boolean matches(String input) {
		return input.startsWith(PREFIX);
	}

	@SuppressWarnings("unchecked")
	public int complete(String buffer, int cursor, List candidates) {
		if (matches(buffer)) {
			String urlprefix = extractURL(buffer);
			ArrayList<String> urlCandidate = new ArrayList<String>(remoteConnectionHistory);
			urlCandidate.addAll(processListManager.getLocalProcesses());
			for(String s : urlCandidate) {
				if (s.startsWith(urlprefix)) candidates.add(s);
			}
			return PREFIX.length();
		} else {
			return -1;
		}
	}
	
	@Override
	public CommandHelp getHelp() {
		return new CommandHelp("Connect to mbean server.", "\\c hostname:port OR LOCAL", "\\c");
	}
	

	private static String extractURL(String in) {
		return in.substring(PREFIX.length()).trim();
	}

}
