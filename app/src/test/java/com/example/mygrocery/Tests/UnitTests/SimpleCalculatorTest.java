package com.example.mygrocery.Tests.UnitTests;

import static org.junit.Assert.assertEquals;

import com.example.mygrocery.Tests.UnitTests.SimpleCalculator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SimpleCalculatorTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void twoPlusTwoEqualsFour(){
        SimpleCalculator calculator = new SimpleCalculator();
        int sum = calculator.add(2,2);
        assertEquals(4, sum);

    }

    @Test
    public void threePlusSevenEqualsTen(){
        SimpleCalculator calculator = new SimpleCalculator();
        int sum = calculator.add(3,7);
        assertEquals(11, sum); // 10==10 .... This test should fail since 11 != 10

    }
}