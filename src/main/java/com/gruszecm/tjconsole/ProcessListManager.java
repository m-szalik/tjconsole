package com.gruszecm.tjconsole;

import sun.management.ConnectorAddressLink;

import javax.management.remote.JMXServiceURL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

//import sun.management.ConnectorAddressLink;

/**
 * Util to resolve Java PIDs and resolve mxBeanServer URL by PID
 */
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


    /**
     * @return list of Java local processes like LOCAL:PID - processName
     * @throws IOException
     */
    private Collection<String> checkLPS() throws IOException {
		ProcessBuilder pb = new ProcessBuilder("jps");
		Process process = pb.start();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			ArrayList<String> list = new ArrayList<String>();
			String l ;
			while((l = br.readLine()) != null) {
				String[] ll = l.split(" ", 2);
				if (ll[1].equalsIgnoreCase("jps")) continue;
				int pid = Integer.valueOf(ll[0]);
				if (ConnectorAddressLink.importFrom(pid) != null) {
					list.add(LOCAL_PREFIX + pid + " - " + ll[1]);
				}
			}
			return list;
		} finally {
			if (br != null) {
				br.close();
			}
		}
    }


    /**
     * @param url url to check
     * @return true if url string is local process url
     */
    public boolean isLocalProcess(String url) {
        return url.startsWith(LOCAL_PREFIX);
    }


    /**
     * @param url string of local mxBeanServer url
     * @return MxBeanServer URL for local process
     * @throws IOException
     * @see #getLocalProcesses()
     */
    public JMXServiceURL getLocalServiceURL(String url) throws IOException {
        String s = url.substring(LOCAL_PREFIX.length());
        int index = s.lastIndexOf(" ");
        if (index < 0) {
            index = s.lastIndexOf("-");
        }
        if (index > 0) {
            s = s.substring(0, index - 1).trim();
        }
        int pid = Integer.parseInt(s);
        String serviceURL = ConnectorAddressLink.importFrom(pid);
        if (serviceURL == null) {
            throw new IOException("Cannot connect to local process - " + pid + "(");
        }
        return new JMXServiceURL(serviceURL);
    }

}
