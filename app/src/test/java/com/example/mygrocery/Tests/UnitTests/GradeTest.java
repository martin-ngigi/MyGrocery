package com.example.mygrocery.Tests.UnitTests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.example.mygrocery.Tests.UnitTests.Grade;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GradeTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void determineLetterGrade() {
    }

    @Test
    public void fiftyNineShouldReturnF(){
        Grade grade = new Grade();
        char myGrade = grade.determineLetterGrade(59);
        assertEquals('F',myGrade );
    }

    @Test
    public void sixtyNineShouldReturnD(){
        Grade grade = new Grade();
        char myGrade = grade.determineLetterGrade(69);
        assertEquals('D',myGrade );
    }

    @Test
    public void seventyNineShouldReturnC(){
        Grade grade = new Grade();
        char myGrade = grade.determineLetterGrade(79);
        assertEquals('C',myGrade );
    }

    @Test
    public void eightyNineShouldReturnB(){
        Grade grade = new Grade();
        char myGrade = grade.determineLetterGrade(89);
        assertEquals('B',myGrade );
    }

    @Test
    public void ninetyNineShouldReturnA(){
        Grade grade = new Grade();
        char myGrade = grade.determineLetterGrade(99);
        assertEquals('A',myGrade );
    }

    @Test
    public void negativeOneReturnIllegalArgument(){
        Grade grade = new Grade();
        assertThrows(IllegalArgumentException.class,
                () -> {
                    grade.determineLetterGrade(-1);

                });
    }

    @Test
    public void zeroShouldReturnF(){
        Grade grade = new Grade();
        char myGrade = grade.determineLetterGrade(0);
        assertEquals('F',myGrade );
    }

    @Test
    public void sixtyShouldReturnD(){
        Grade grade = new Grade();
        char myGrade = grade.determineLetterGrade(60);
        assertEquals('D',myGrade );
    }

    @Test
    public void seventyShouldReturnC(){
        Grade grade = new Grade();
        char myGrade = grade.determineLetterGrade(70);
        assertEquals('C',myGrade );
    }

    @Test
    public void eightyShouldReturnB(){
        Grade grade = new Grade();
        char myGrade = grade.determineLetterGrade(80);
        assertEquals('B',myGrade );
    }

    @Test
    public void ninetyShouldReturnA(){
        Grade grade = new Grade();
        char myGrade = grade.determineLetterGrade(90);
        assertEquals('A',myGrade );
    }

}