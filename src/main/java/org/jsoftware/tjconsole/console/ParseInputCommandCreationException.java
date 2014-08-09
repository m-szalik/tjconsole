package org.jsoftware.tjconsole.console;

public class ParseInputCommandCreationException extends ParseInputException {

    public ParseInputCommandCreationException(String input, Throwable init) {
        super("Cannot create command for '" + input + "'", input);
        initCause(init);
    }

}
