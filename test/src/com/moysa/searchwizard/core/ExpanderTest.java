package com.moysa.searchwizard.core;

import org.junit.Test;

import java.util.Set;

/**
 * Test class for Expander
 */
public class ExpanderTest {

    @Test
    public void testExpand() throws Exception {



        Expander expander = new Expander("Послушать хорошую музыку сейчас");

        Set<String> result = expander.expand();

        result.forEach(System.out::println);
    }
}
