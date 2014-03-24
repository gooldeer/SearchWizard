package com.moysa.searchwizard.core;

import org.junit.Test;

import java.util.TreeSet;

/**
 * Test class for Expander
 */
public class ExpanderTest {

    @Test
    public void testExpand() throws Exception {

        String request = "Было-бы хорошо сейчас послушать хорошую музыку";

        Expander expander = new Expander(request);

        //sorted by string
        TreeSet<String> result = new TreeSet<>(expander.expand());

        result.forEach(System.out::println);
    }
}
