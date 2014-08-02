package org.jsoftware.tjconsole.command.definition;


import org.jsoftware.tjconsole.command.definition.InvokeOperationCommandDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;


public class InvokeOperationCommandTest {

    @Test
    public void testSplit1() {
        String input = "Abc, \"as,c\", \"as \", dd";
        List<String> params = InvokeOperationCommandDefinition.mySplit(input);
        Assert.assertEquals("Abc", params.get(0));
        Assert.assertEquals("as,c", params.get(1));
        Assert.assertEquals("as ", params.get(2));
        Assert.assertEquals("dd", params.get(3));
    }
}
