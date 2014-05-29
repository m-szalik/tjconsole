package com.gruszecm.tjconsole;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.management.remote.JMXServiceURL;

//import sun.management.ConnectorAddressLink;

public class ProcessListManager {
	private static final String LOCAL_PREFIX = "LOCAL:";
	private boolean notSupported;
	
	
	public Collection<String> getLocalProcesses() {
		if (notSupported) {
			return Collections.emptyList();
		} else {
			try {
				return checkLPS();
			} catch (Exception e) {
				notSupported = true;
				return Collections.emptyList();
			}
		}
	}

	private Collection<String> checkLPS() throws IOException {
//		ProcessBuilder pb = new ProcessBuilder("jps");
//		Process process = pb.start();
//		BufferedReader br = null;
//		try {
//			br = new BufferedReader(new InputStreamReader(process.getInputStream()));
//			ArrayList<String> list = new ArrayList<String>();
//			String l ;
//			while((l = br.readLine()) != null) {
//				String[] ll = l.split(" ", 2);
//				if (ll[1].equalsIgnoreCase("jps")) continue;
//				int pid = Integer.valueOf(ll[0]);
//				if (ConnectorAddressLink.importFrom(pid) != null) {
//					list.add(LOCAL_PREFIX + pid + " - " + ll[1]);
//				}
//			}
//			return list;
//		} finally {
//			if (br != null) {
//				br.close();
//			}
//		}
        return Collections.emptyList(); // TODO
	}

	public boolean isLocalProcess(String url) {
		return url.startsWith(LOCAL_PREFIX);
	}

	public JMXServiceURL getLocalServiceURL(String url) throws IOException {
		String s = url.substring(LOCAL_PREFIX.length());
		int index = s.lastIndexOf(" ");
		if (index < 0) index = s.lastIndexOf("-");
		if (index > 0) s = s.substring(0, index-1).trim();
		int pid = Integer.valueOf(s);
		String serviceURL = null; // TODO //ConnectorAddressLink.importFrom(pid);
		if (serviceURL == null) throw new IOException("Can not connect to local process - " + pid);
		return new JMXServiceURL(serviceURL);
	}
	
}
