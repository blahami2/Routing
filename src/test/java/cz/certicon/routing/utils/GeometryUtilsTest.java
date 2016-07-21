/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils;

import cz.certicon.routing.model.entity.CartesianCoords;
import cz.certicon.routing.model.entity.Coordinate;
import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
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
public class GeometryUtilsTest {

    public GeometryUtilsTest() {
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
     * Test of toPointFromCartesian method, of class GeometryUtils.
     */
    @Test
    public void testToPointFromCartesian() {
        System.out.println( "toPointFromCartesian" );
//        CartesianCoords coords = null;
//        Point expResult = null;
//        Point result = GeometryUtils.toPointFromCartesian( coords );
//        assertEquals( expResult, result );
////         TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of getScaledPoint method, of class GeometryUtils.
     */
    @Test
    public void testGetScaledPoint() {
        System.out.println( "getScaledPoint" );
//        Point min = null;
//        Point max = null;
//        Point actual = null;
//        Dimension targetDimension = null;
//        Point expResult = null;
//        Point result = GeometryUtils.getScaledPoint( min, max, actual, targetDimension );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of toCoordinatesFromWktPoint method, of class GeometryUtils.
     */
    @Test
    public void testToCoordinatesFromWktPoint() {
        System.out.println( "toCoordinatesFromWktPoint" );
//        String point = "";
//        Coordinate expResult = null;
//        Coordinate result = GeometryUtils.toCoordinatesFromWktPoint( point );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of toWktFromCoordinates method, of class GeometryUtils.
     */
    @Test
    public void testToWktFromCoordinates_Coordinate() {
        System.out.println( "toWktFromCoordinates" );
//        Coordinate coordinates = null;
//        String expResult = "";
//        String result = GeometryUtils.toWktFromCoordinates( coordinates );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of toCoordinatesFromWktLinestring method, of class GeometryUtils.
     */
    @Test
    public void testToCoordinatesFromWktLinestring() {
        System.out.println( "toCoordinatesFromWktLinestring" );
//        String linestring = "";
//        List<Coordinate> expResult = null;
//        List<Coordinate> result = GeometryUtils.toCoordinatesFromWktLinestring( linestring );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of toWktFromCoordinates method, of class GeometryUtils.
     */
    @Test
    public void testToWktFromCoordinates_List() {
        System.out.println( "toWktFromCoordinates" );
//        List<Coordinate> coordinates = null;
//        String expResult = "";
//        String result = GeometryUtils.toWktFromCoordinates( coordinates );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of pointToLineDistance method, of class GeometryUtils.
     */
    @Test
    public void testPointToLineDistance() {
        System.out.println( "pointToLineDistance" );
        int ax = 2;
        int ay = 2;
        int bx = 4;
        int by = 0;
        double expResult = 2 * Math.sqrt( 2 );
        double eps = 10E-9;
        GeometryUtils.pointToLineDistance( ax, ay, bx, by, 0, 0 );
        GeometryUtils.pointToLineDistance( ax, ay, bx, by, 0, 4 );
        GeometryUtils.pointToLineDistance( ax, ay, bx, by, 4, 4 );
        GeometryUtils.pointToLineDistance( ax, ay, bx, by, 5, 3 );
        GeometryUtils.pointToLineDistance( ax, ay, bx, by, 6, 0 );
        assertEquals( expResult, GeometryUtils.pointToLineDistance( ax, ay, bx, by, 0, 0 ), eps );
        assertEquals( expResult, GeometryUtils.pointToLineDistance( ax, ay, bx, by, 0, 4 ), eps );
        assertEquals( expResult, GeometryUtils.pointToLineDistance( ax, ay, bx, by, 4, 4 ), eps );
        assertEquals( expResult, GeometryUtils.pointToLineDistance( ax, ay, bx, by, 5, 3 ), eps );
        assertEquals( 2, GeometryUtils.pointToLineDistance( ax, ay, bx, by, 6, 0 ), eps );
    }

}
