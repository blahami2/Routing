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
public class LengthTest {
    
    public LengthTest() {
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
     * Test of getLengthUnits method, of class Length.
     */
    @Test
    public void testGetLengthUnits() {
        System.out.println( "getLengthUnits" );
        Length instance = new Length(LengthUnits.METERS, 5);
        LengthUnits expResult = LengthUnits.METERS;
        LengthUnits result = instance.getLengthUnits();
        assertEquals( expResult, result );
    }

    /**
     * Test of getNanoseconds method, of class Length.
     */
    @Test
    public void testGetNanoseconds() {
        System.out.println( "getNanometers" );
        Length instance = new Length(LengthUnits.METERS, 5);
        long expResult = 5000000000L;
        long result = instance.getNanometers();
        assertEquals( expResult, result );
    }

    /**
     * Test of getLength method, of class Length.
     */
    @Test
    public void testGetLength() {
        System.out.println( "getLength" );
        Length instance = new Length(LengthUnits.METERS, 5);
        long expResult = 5L;
        long result = instance.getLength();
        assertEquals( expResult, result );
    }

    /**
     * Test of getUnit method, of class Length.
     */
    @Test
    public void testGetUnit() {
        System.out.println( "getUnit" );
        Length instance = new Length(LengthUnits.METERS, 5);
        String expResult = "m";
        String result = instance.getUnit();
        assertEquals( expResult, result );
    }

    /**
     * Test of toString method, of class Length.
     */
    @Test
    public void testToString() {
        System.out.println( "toString" );
        Length instance = new Length(LengthUnits.METERS, 50);
        String expResult = "50";
        String result = instance.toString();
        assertEquals( expResult, result );
    }
    
}
