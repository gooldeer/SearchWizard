package com.moysa.searchwizard.core;

import org.junit.Assert;
import org.junit.Test;

import java.util.TreeSet;

/**
 * Test class for Expander
 */
public class ExpanderTest {

    @Test
    public void testExpand() throws Exception {

        String request = "Найти хороший стиль медленной песни";

        Expander expander = new Expander(request);

        //sorted by string
        TreeSet<String> result = new TreeSet<>(expander.expand());

        result.forEach(System.out::println);

        Assert.assertEquals(54, result.size());
    }
}
