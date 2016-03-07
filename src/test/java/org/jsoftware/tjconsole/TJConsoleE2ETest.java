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
        tjConsole.executeCommand("use java.lang:type=Memory", outputHelper.getOutput());
        outputHelper.clear();
        tjConsole.executeCommand("get ObjectName", outputHelper.getOutput());
        Assert.assertEquals("ObjectName=java.lang:type=Memory", outputHelper.toString().trim().replace(" ", ""));
    }

    @Test
    public void testCmdSet() throws Exception {
        tjConsole.executeCommand("use java.lang:type=Memory", outputHelper.getOutput());
        tjConsole.executeCommand("set Verbose=true", outputHelper.getOutput());
        outputHelper.clear();
        tjConsole.executeCommand("get Verbose", outputHelper.getOutput());
        Assert.assertTrue(outputHelper.toString().contains("Verbose = true"));
    }

    @Test
    public void testCmdInvoke() throws Exception {
        tjConsole.executeCommand("use java.lang:type=Memory", outputHelper.getOutput());
        outputHelper.clear();
        tjConsole.executeCommand("invoke gc", outputHelper.getOutput());
        Assert.assertEquals("Methodresult:(void)", outputHelper.toString().trim().replace(" ", ""));
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
}

