package org.jsoftware.tjconsole;

public class Output {
    private boolean displayInfo = true;

    public void setDisplayInfo(boolean displayInfo) {
        this.displayInfo = displayInfo;
    }

    public void outInfo(String text) {
        if (displayInfo) {
            System.out.println("INFO: " + text + "\n");
            System.out.flush();
        }
    }

    public void outMBeanOutput(String text) {
        System.out.print(text);
        System.out.flush();
    }

    public void outError(String text) {
        System.err.print("ERROR: " + text + "\n");
        System.err.flush();
    }

    public void outPrompt(String prompt) {
        System.out.print(prompt);
        System.out.flush();
    }

}
