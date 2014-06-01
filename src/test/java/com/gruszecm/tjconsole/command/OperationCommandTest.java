package com.gruszecm.tjconsole.command;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;


public class OperationCommandTest {

    @Test
    public void testSplit1() {
        String input = "Abc, \"as,c\", \"as \", dd";
        List<String> params = OperationCommand.mySplit(input);
        Assert.assertEquals("Abc", params.get(0));
        Assert.assertEquals("as,c", params.get(1));
        Assert.assertEquals("as ", params.get(2));
        Assert.assertEquals("dd", params.get(3));
    }
}
