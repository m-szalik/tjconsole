package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CommandAction;
import org.jsoftware.tjconsole.console.Output;
import org.jsoftware.tjconsole.console.ParseInputCommandCreationException;
import org.jsoftware.tjconsole.localjvm.JvmPid;
import org.jsoftware.tjconsole.localjvm.LocalJvmAttachException;
import org.jsoftware.tjconsole.localjvm.ProcessListManager;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Command to connect to server
 *
 * @author szalik
 */
public class ConnectCommandDefinition extends AbstractCommandDefinition {
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final List<String> remoteConnectionHistory;
    private final Preferences prefs;


    public ConnectCommandDefinition() throws BackingStoreException {
        super("Connect to mBean server.", "connect <host>:<port> or <user>:<password>@<host>:<port> or <local jvm pid>", "connect", false);
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
                logger.throwing(getClass().getName(), "addRemote - Error saving data to user preferences", e);
            }
        }
    }

    @Override
    public CommandAction action(final String input) {
        return new CommandAction() {
            @Override
            public void doAction(TJContext ctx, Output output) throws Exception {
                final String url = extractURL(input);
                String messageURL;
                Map<String, Object> env = null;
                if (url.length() == 0) {  // current state
                    if (ctx.getServer() == null) {
                        output.outError("Not connected to any JMX server.");
                        ctx.fail(this, 10);
                    } else {
                        output.outInfo("Connected to " + ctx.getServer().getDefaultDomain());
                    }
                    return;
                }
                boolean saveURL = false;
                MBeanServerConnection serverConnection;
                JMXServiceURL serviceURL;
                if (ProcessListManager.isLocalProcess(url)) {
                    try {
                        serviceURL = new ProcessListManager().getLocalServiceURL(url);
                        messageURL = url;
                    } catch (LocalJvmAttachException e) {
                        output.outError("Unable to connect to local JVM. Please enable jmx remote access on your java process. More: https://gist.github.com/m-szalik/93c559bf2ad964078e1e");
                        ctx.fail(this, 10);
                        return;
                    }
                } else {
                    String hostPort;
                    int lm = url.lastIndexOf('@');
                    if (lm > 0) { // user and password
                        hostPort = url.substring(lm + 1);
                        String passwordAndUser = url.substring(0, lm);
                        env = new HashMap<String, Object>();
                        int semicolon = passwordAndUser.indexOf(':');
                        String user, password;
                        if (semicolon <= 0) {
                            password = "";
                            user = passwordAndUser;
                        } else {
                            user = passwordAndUser.substring(0, semicolon);
                            password = passwordAndUser.substring(semicolon + 1);
                        }
                        env.put(JMXConnector.CREDENTIALS, new String[]{user, password});
                    } else {
                        hostPort = url;
                    }
                    String host;
                    int port;
                    String[] urlParts = hostPort.split(":", 2);
                    host = urlParts[0];
                    if (urlParts.length == 2) {
                        try {
                            port = Integer.parseInt(urlParts[1].trim());
                        } catch (NumberFormatException ex) {
                            throw new ParseInputCommandCreationException("Invalid port number '" + urlParts[1].trim() + "'", ex);
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid connection URL (expected host:port) but found '" + hostPort + "'");
                    }
                    serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
                    messageURL = host + ":" + port;
                    saveURL = lm <= 0;
                }
                JMXConnector connector = env == null ? JMXConnectorFactory.connect(serviceURL) : JMXConnectorFactory.connect(serviceURL, env);
                serverConnection = connector.getMBeanServerConnection();
                if (serverConnection != null) {
                    output.outInfo("Connected to " + messageURL + ". Default domain name is " + serverConnection.getDefaultDomain());
                    ctx.setServer(serverConnection, url);
                    if (saveURL) {
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
        return input.startsWith(prefix);
    }


    private String extractURL(String in) {
        return in.substring(prefix.length()).trim();
    }

    class ConnectCompleter implements Completer {
        private final ProcessListManager processListManager = new ProcessListManager();

        @Override
        public int complete(String buffer, int cursor, List<CharSequence> candidates) {
            if (matches(buffer)) {
                String urlPrefix = extractURL(buffer);
                ArrayList<String> urlCandidate = new ArrayList<String>(remoteConnectionHistory);
                for (JvmPid jvm : processListManager.getLocalProcessList()) {
                    urlCandidate.add(jvm.getShortName());
                }
                for (String s : urlCandidate) {
                    if (s.startsWith(urlPrefix)) candidates.add(" " + s);
                }
                return prefix.length();
            } else {
                return -1;
            }
        }
    }
}

