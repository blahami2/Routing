/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.osm;

import cz.certicon.routing.data.osm.WayAttributeParser.Pair;
import cz.certicon.routing.model.entity.EdgeAttributes;
import cz.certicon.routing.model.entity.common.SimpleEdgeAttributes;
import cz.certicon.routing.utils.SpeedUtils;
import java.util.Arrays;
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
public class WayAttributeParserTest {

    public WayAttributeParserTest() {
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
     * Test of parse method, of class WayAttributeParser.
     */
    @Test
    public void testParse() {
        System.out.println( "parse" );
        WayAttributeParser instance = new WayAttributeParser();
        String countryCode = "CZ";
        boolean insideCity = true;
        double length = 50;
        EdgeAttributes expResult;
        EdgeAttributes result;
        List<WayAttributeParser.Pair> pairs;
        // test 1
        pairs = Arrays.asList(
                new Pair( "lit", "yes" ),
                new Pair( "name", "Horáčkova" ),
                new Pair( "lanes", "1" ),
                new Pair( "oneway", "no" ),
                new Pair( "highway", "residential" ),
                new Pair( "surface", "asphalt" ),
                new Pair( "maxspeed", "50" ),
                new Pair( "source:maxspeed", "cz:urban" )
        );
        expResult = SimpleEdgeAttributes.builder( 50 )
                .setLength( length )
                .setOneWay( false )
                .build();
        result = instance.parse( countryCode, insideCity, pairs, length );
        assertEquals( expResult, result );
        // test 2
        pairs = Arrays.asList(
                new Pair( "lit", "yes" ),
                new Pair( "name", "Horáčkova" ),
                new Pair( "lanes", "1" ),
                new Pair( "oneway", "yes" ),
                new Pair( "highway", "residential" ),
                new Pair( "surface", "asphalt" ),
                new Pair( "maxspeed", "50 kmh" ),
                new Pair( "source:maxspeed", "cz:urban" )
        );
        expResult = SimpleEdgeAttributes.builder( 50 )
                .setLength( length )
                .setOneWay( true )
                .build();
        result = instance.parse( countryCode, insideCity, pairs, length );
        assertEquals( expResult, result );
        // test 3
        pairs = Arrays.asList(
                new Pair( "lit", "yes" ),
                new Pair( "name", "Horáčkova" ),
                new Pair( "lanes", "1" ),
                new Pair( "oneway", "no" ),
                new Pair( "highway", "residential" ),
                new Pair( "surface", "asphalt" ),
                new Pair( "maxspeed", "50 mph" ),
                new Pair( "source:maxspeed", "cz:urban" )
        );
        expResult = SimpleEdgeAttributes.builder( SpeedUtils.mphToKmph( 50 ) )
                .setLength( length )
                .setOneWay( false )
                .build();
        result = instance.parse( countryCode, insideCity, pairs, length );
        assertEquals( expResult, result );
        // test 4
        pairs = Arrays.asList(
                new Pair( "lit", "yes" ),
                new Pair( "name", "Horáčkova" ),
                new Pair( "lanes", "1" ),
                new Pair( "oneway", "no" ),
                new Pair( "highway", "residential" ),
                new Pair( "surface", "asphalt" ),
                new Pair( "maxspeed", "50 knots" ),
                new Pair( "source:maxspeed", "cz:urban" )
        );
        expResult = SimpleEdgeAttributes.builder( SpeedUtils.knotToKmph( 50 ) )
                .setLength( length )
                .setOneWay( false )
                .build();
        result = instance.parse( countryCode, insideCity, pairs, length );
        assertEquals( expResult, result );
        // test 5
        pairs = Arrays.asList(
                new Pair( "lit", "yes" ),
                new Pair( "name", "Horáčkova" ),
                new Pair( "lanes", "1" ),
                new Pair( "oneway", "no" ),
                new Pair( "highway", "residential" ),
                new Pair( "surface", "asphalt" ),
                new Pair( "maxspeed", "none" ),
                new Pair( "source:maxspeed", "cz:urban" )
        );
        expResult = SimpleEdgeAttributes.builder( 50 )
                .setLength( length )
                .setOneWay( false )
                .build();
        result = instance.parse( countryCode, insideCity, pairs, length );
        assertEquals( expResult, result );
        // test 6
        pairs = Arrays.asList(
                new Pair( "lit", "yes" ),
                new Pair( "name", "Milánská" ),
                new Pair( "lanes", "2" ),
                new Pair( "highway", "tertiary" ),
                new Pair( "surface", "asphalt" ),
                new Pair( "lanes:both_ways", "1" ),
                new Pair( "source:maxspeed", "survey" ),
                new Pair( "maxspeed:forward", "50" ),
                new Pair( "maxspeed:backward", "40" )
        );
        expResult = SimpleEdgeAttributes.builder( 50 )
                .setLength( length )
                .setOneWay( false )
                .build();
        result = instance.parse( countryCode, insideCity, pairs, length );
        assertEquals( expResult, result );
        // test 7
        pairs = Arrays.asList(
                new Pair( "name", "Radlická" ),
                new Pair( "lanes", "4" ),
                new Pair( "highway", "secondary" ),
                new Pair( "lanes:forward", "2" ),
                new Pair( "lanes:backward", "2" ),
                new Pair( "source:maxspeed", "sign" ),
                new Pair( "maxspeed:backward", "40" )
        );
        expResult = SimpleEdgeAttributes.builder( 50 )
                .setLength( length )
                .setOneWay( false )
                .build();
        result = instance.parse( countryCode, insideCity, pairs, length );
        assertEquals( expResult, result );

        /*
        name=Radlická
lanes=4
highway=secondary
lanes:forward=2
lanes:backward=2
source:maxspeed=sign
maxspeed:backward=40
         */
    }

}
