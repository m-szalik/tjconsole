package org.jsoftware.tjconsole;

import org.jsoftware.tjconsole.console.Output;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.management.MBeanServer;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.regex.Pattern;

public class TJConsoleE2ETest {
    private TestOutputHelper outputHelper;
    private TJConsole tjConsole;

    @Before
    public void setUp() throws Exception {
        tjConsole = new TJConsole(new Properties());
        outputHelper = new TestOutputHelper();
        MBeanServer localMBeanServer = ManagementFactory.getPlatformMBeanServer();
        tjConsole.getContext().setServer(localMBeanServer, "localServer");
    }

    @Test
    public void testCmdHelp() throws Exception {
        tjConsole.executeCommand("help", outputHelper.getOutput());
        Assert.assertTrue(outputHelper.toString().length() > 0);
    }

    @Test
    public void testCmdInfo() throws Exception {
        tjConsole.executeCommand("info", outputHelper.getOutput());
        Assert.assertTrue(outputHelper.toString().contains("localServer"));
    }

    @Test
    public void testCmdUse() throws Exception {
        tjConsole.executeCommand("use", outputHelper.getOutput());
        Assert.assertTrue(outputHelper.toString().contains("Runtime"));
    }

    @Test
     public void testCmdDescribe() throws Exception {
        tjConsole.executeCommand("use java.lang:type=Memory", outputHelper.getOutput());
        outputHelper.clear();
        tjConsole.executeCommand("describe", outputHelper.getOutput());
        Assert.assertTrue(outputHelper.toString().length() > 0);
    }

    @Test
    public void testCmdGet() throws Exception {
        tjConsole.executeCommand("use java.lang:type=Runtime", outputHelper.getOutput());
        outputHelper.clear();
        tjConsole.executeCommand("get StartTime", outputHelper.getOutput());
        outputHelper.matchesPattern("StartTime = \\d*");
    }

    @Test
    public void testCmdSet() throws Exception {
        tjConsole.executeCommand("use java.lang:type=Memory", outputHelper.getOutput());
        tjConsole.executeCommand("set Verbose=true", outputHelper.getOutput());
        outputHelper.clear();
        tjConsole.executeCommand("get Verbose", outputHelper.getOutput());
        Assert.assertEquals("Verbose = true", outputHelper.toString().trim());
    }

    @Test
    public void testCmdInvoke() throws Exception {
        tjConsole.executeCommand("use java.lang:type=Memory", outputHelper.getOutput());
        outputHelper.clear();
        tjConsole.executeCommand("invoke gc", outputHelper.getOutput());
        Assert.assertEquals("Methodresult:(void)", outputHelper.toString().trim().replace(" ", ""));
    }

    @Test
    public void testCmdEnv() throws Exception {
        tjConsole.executeCommand("env", outputHelper.getOutput());
        Assert.assertTrue(outputHelper.toString().contains("DATE_FORMAT"));
        tjConsole.executeCommand("env DATE_FORMAT=yyyy-MMM-dd", outputHelper.getOutput());
        outputHelper.clear();
        tjConsole.executeCommand("env", outputHelper.getOutput());
        System.out.println(outputHelper);
        Assert.assertTrue(outputHelper.toString().contains("DATE_FORMAT = yyyy-MMM-dd"));
    }

    @Test
    public void testCmdPs() throws Exception {
        Pattern processLinePattern = Pattern.compile("^\\d{1,} .*$");
        tjConsole.executeCommand("ps", outputHelper.getOutput());
        String[] processList = outputHelper.toString().trim().split("\n");
        int psCount = 0;
        for(String psLine : processList) {
            psLine = psLine.trim();
            if (psLine.length() == 0) {
                continue;
            }
            psCount++;
            if (! processLinePattern.matcher(psLine).matches()) {
                Assert.fail("Line '" + psLine + "' doesn't look like ps line!");
            }
        }
        Assert.assertTrue("We don't have even one process on the list", psCount > 0);
    }
}

class TestOutputHelper {
    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    public Output getOutput() {
        return new Output(new PrintStream(out), false);
    }

    public String toString() {
        return new String(out.toByteArray());
    }

    public void clear() {
        out.reset();
    }

    public void matchesPattern(final String patternStr) {
        Pattern pattern = Pattern.compile(patternStr);
        String str = toString().trim();
        if (! pattern.matcher(str).matches()) {
            Assert.fail("Input '" + str + "' doesn't match regexp '" + patternStr + "'");
        }
    }
}

