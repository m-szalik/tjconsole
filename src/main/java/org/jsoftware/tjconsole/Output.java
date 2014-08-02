package org.jsoftware.tjconsole;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.PrintStream;
import java.io.PrintWriter;

import static org.fusesource.jansi.Ansi.ansi;

public class Output implements AutoCloseable {
    private final PrintStream out;
    private boolean useColors = true, ansiInstalled;

    public Output(PrintStream out, boolean useColors) {
        this.out = out;
        setUseColors(useColors);
    }


    public boolean isUseColors() {
        return useColors;
    }


    public void setUseColors(boolean useColors) {
        this.useColors = useColors;
        if (useColors) {
            AnsiConsole.systemInstall();
            ansiInstalled = true;
        } else {
            AnsiConsole.systemUninstall();
            ansiInstalled = false;
        }
    }


    public void outInfo(String text) {
        if (useColors) {
            out.println(ansi().fg(Ansi.Color.WHITE).bold().a(text).reset());
        } else {
            out.println(text);
        }
        out.flush();
    }


    public void outMBeanOutput(String text) {
        if (useColors) {
            out.println(ansi().fg(Ansi.Color.BLUE).a(text).reset());
        } else {
            out.println("INFO:  " + text);
        }
        out.flush();
    }


    public void outApp(String text) {
        out.println(text);
        out.flush();
    }


    public void outError(String text) {
        if (useColors) {
            out.println(ansi().fg(Ansi.Color.RED).a(text).reset());
        } else {
            out.println("ERROR: " + text);
        }
        out.flush();
    }


    public void outPrompt(String promptPattern, TJContext context) {
        String prompt = promptPattern.replace("%b", context.getObjectName() == null ? "#none#" : context.getObjectName().toString());
        out.println();
        out.print(prompt);
    }


    @Override
    public void close() throws Exception {
        if (ansiInstalled) {
            AnsiConsole.systemUninstall();
        }
        out.close();
    }

}
