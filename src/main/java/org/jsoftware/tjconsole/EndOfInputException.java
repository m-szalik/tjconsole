package org.jsoftware.tjconsole;

public class EndOfInputException extends Exception {
    private final boolean printExitMessage;

    public EndOfInputException(boolean printExitMessage) {
        this.printExitMessage = printExitMessage;
    }

    public boolean isPrintExitMessage() {
        return printExitMessage;
    }
}
