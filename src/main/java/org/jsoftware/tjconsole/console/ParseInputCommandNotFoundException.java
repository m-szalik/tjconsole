package org.jsoftware.tjconsole.console;

public class ParseInputCommandNotFoundException extends ParseInputException {

    public ParseInputCommandNotFoundException(String input) {
        super("Cannot find command for '" + input + "'", input);
    }

}
