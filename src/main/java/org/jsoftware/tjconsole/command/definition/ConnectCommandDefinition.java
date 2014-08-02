package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.console.Output;
import org.jsoftware.tjconsole.command.CommandAction;
import org.jsoftware.tjconsole.localjvm.*;
import org.jsoftware.tjconsole.TJContext;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Command to connect to server
 *
 * @author szalik
 */
public class ConnectCommandDefinition extends AbstractCommandDefinition {
    private static final String PREFIX = "connect ";
    private final List<String> remoteConnectionHistory;
    private final Preferences prefs;


    public ConnectCommandDefinition() throws BackingStoreException {
        super("Connect to mbean server.", "connect hostname:port or open local_vm_pid", "connect", false);
        remoteConnectionHistory = new ArrayList<String>();
        prefs = Preferences.userNodeForPackage(getClass());
        for (String key : prefs.keys()) {
            String rName = prefs.get(key, null);
            if (rName != null) {
                addRemote(rName, false);
            }
        }
    }

    private void addRemote(String rName, boolean b) {
        if (remoteConnectionHistory.contains(rName)) return;
        remoteConnectionHistory.add(rName);
        if (b) {
            try {
                prefs.put("RNAME_" + (prefs.keys().length + 1), rName);
            } catch (BackingStoreException e) {
                // TODO use logger to log it
            }
        }
    }

    @Override
    public CommandAction action(final String input) throws Exception {
        return new CommandAction() {
            @Override
            public void doAction(TJContext ctx, Output output) throws Exception {
                Map<String, Object> env = ctx.getEnvironment();
                if (env.get("SSL").equals(Boolean.TRUE)) {
                    throw new IllegalArgumentException("SSL not supported.");
                }
                if (env.get("USERNAME").toString().length() + env.get("PASSWORD").toString().length() > 0) {
                    throw new IllegalArgumentException("USER/PASS not supported.");
                }

                String url = extractURL(input);
                if (url.length() == 0) {
                    if (ctx.getServer() == null) {
                        output.outError("Not connected.");
                    } else {
                        output.outInfo("Connected to " + ctx.getServer().getDefaultDomain());
                    }
                    return;
                }
                boolean remote = false;
                output.outInfo("Connecting to " + url + "....\n");
                MBeanServerConnection serverConnection;
                if (url.equalsIgnoreCase(ProcessListManager.LOCAL_PREFIX)) {
                    serverConnection = ManagementFactory.getPlatformMBeanServer();
                } else {
                    JMXServiceURL serviceURL;
                    if (ProcessListManager.isLocalProcess(url)) {
                        try {
                            serviceURL = new ProcessListManager().getLocalServiceURL(url);
                        } catch (LocalJvmAttachException e) {
                            output.outError("Unable to connect to local JVM. Run jvm with -Dcom.sun.management.jmxremote");
                            return;
                        }
                    } else {
                        String port = "", host = "";
                        String[] urlParts = url.split(":", 2);
                        host = urlParts[0];
                        if (urlParts.length == 2) {
                            port = urlParts[1];
                        }
                        serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
                        remote = true;
                    }
                    JMXConnector connector = JMXConnectorFactory.connect(serviceURL);
                    serverConnection = connector.getMBeanServerConnection();
                }
                if (serverConnection != null) {
                    output.outInfo("Connected to " + url + " - " + serverConnection.getDefaultDomain());
                    ctx.setServer(serverConnection);
                    if (remote) {
                        addRemote(url, true);
                    }
                }
            }
        };

    }


    @Override
    public Completer getCompleter(TJContext tjContext) {
        return new ConnectCompleter();
    }

    @Override
    public boolean matches(String input) {
        return input.startsWith(PREFIX);
    }


    private static String extractURL(String in) {
        return in.substring(PREFIX.length()).trim();
    }

    class ConnectCompleter implements Completer {
        private final ProcessListManager processListManager = new ProcessListManager();

        @Override
        public int complete(String buffer, int cursor, List<CharSequence> candidates) {
            if (matches(buffer)) {
                String urlPrefix = extractURL(buffer);
                ArrayList<String> urlCandidate = new ArrayList<String>(remoteConnectionHistory);
                for(JvmPid jvm : processListManager.getLocalProcessList()) {
                    urlCandidate.add(ProcessListManager.LOCAL_PREFIX + " " + jvm.getFullName());
                }
                for (String s : urlCandidate) {
                    if (s.startsWith(urlPrefix)) candidates.add(s);
                }
                return PREFIX.length();
            } else {
                return -1;
            }
        }
    }
}

