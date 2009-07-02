package com.gruszecm.tjconsole.command;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class OperationCommaandTest {

	@Test
	public void testSplit1() {
		String input = "Abc, \"as,c\", \"as \", dd";
		List<String> params = OperationCommaand.mysplit(input);
		Assert.assertEquals("Abc", params.get(0));
		Assert.assertEquals("as,c", params.get(1));
		Assert.assertEquals("as ", params.get(2));
		Assert.assertEquals("dd", params.get(3));
	}
}
