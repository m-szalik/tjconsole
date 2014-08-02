package org.jsoftware.tjconsole.exception;

public class ParseInputCommandNotFoundException extends ParseInputException {

    public ParseInputCommandNotFoundException(String input) {
        super("Cannot find command for '" + input + "'", input);
    }

}
