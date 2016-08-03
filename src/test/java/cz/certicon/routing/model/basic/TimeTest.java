/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.basic;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class TimeTest {

    public TimeTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getTimeUnits method, of class Time.
     */
    @Test
    public void testGetTimeUnits() {
        System.out.println( "getTimeUnits" );
        Time instance = new Time( TimeUnits.SECONDS, 5 );
        TimeUnits expResult = TimeUnits.SECONDS;
        TimeUnits result = instance.getTimeUnits();
        assertEquals( expResult, result );
    }

    /**
     * Test of getNanoseconds method, of class Time.
     */
    @Test
    public void testGetNanoseconds() {
        System.out.println( "getNanoseconds" );
        Time instance = new Time( TimeUnits.SECONDS, 5 );
        long expResult = 5000000000L;
        long result = instance.getNanoseconds();
        assertEquals( expResult, result );
    }

    /**
     * Test of getTime method, of class Time.
     */
    @Test
    public void testGetTime() {
        System.out.println( "getTime" );
        Time instance = new Time( TimeUnits.SECONDS, 5 );
        long expResult = 5L;
        long result = instance.getTime();
        assertEquals( expResult, result );
    }

    /**
     * Test of getUnit method, of class Time.
     */
    @Test
    public void testGetUnit() {
        System.out.println( "getUnit" );
        Time instance = new Time( TimeUnits.SECONDS, 5 );
        String expResult = "s";
        String result = instance.getUnit();
        assertEquals( expResult, result );
    }

    /**
     * Test of add method, of class Time.
     */
    @Test
    public void testAdd() {
        System.out.println( "add" );
        Time time = new Time( TimeUnits.MINUTES, 3 );
        Time instance = new Time( TimeUnits.SECONDS, 5 );
        Time expResult = new Time( TimeUnits.SECONDS, 3 * 60 + 5 );
        Time result = instance.add( time );
        assertEquals( expResult, result );
    }

    /**
     * Test of divide method, of class Time.
     */
    @Test
    public void testDivide() {
        System.out.println( "divide" );
        long divisor = 4L;
        Time instance = new Time( TimeUnits.SECONDS, 50 );
        Time expResult = new Time( TimeUnits.SECONDS, 50 / 4 );
        Time result = instance.divide( divisor );
        assertEquals( expResult, result );
    }

    /**
     * Test of toString method, of class Time.
     */
    @Test
    public void testToString() {
        System.out.println( "toString" );
        Time instance = new Time( TimeUnits.SECONDS, 50 );
        String expResult = "50";
        String result = instance.toString();
        assertEquals( expResult, result );
    }

}
