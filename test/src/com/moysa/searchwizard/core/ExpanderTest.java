package com.moysa.searchwizard.core;

import org.junit.Test;

import java.util.Set;

/**
 * Test class for Expander
 */
public class ExpanderTest {

    @Test
    public void testExpand() throws Exception {

        String request = "Было-бы хорошо сейчас послушать хорошую музыку";

        Expander expander = new Expander(request);

        Set<String> result = expander.expand();

        result.forEach(System.out::println);
    }
}
