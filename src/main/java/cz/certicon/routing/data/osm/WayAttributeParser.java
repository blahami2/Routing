/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.osm;

import cz.certicon.routing.model.entity.EdgeAttributes;
import cz.certicon.routing.model.entity.common.SimpleEdgeAttributes;
import cz.certicon.routing.utils.SpeedUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class WayAttributeParser {

    private static final String COUNTRY_CODE_DELIMITER = "_";

    private static final MatchMap PAID = MatchMap.create( new Pair( "toll", "yes" ) );
    private static final MatchMap ONE_WAY = MatchMap.create( new Pair( "oneway", "yes" ), new Pair( "highway", "motorway" ) );
    private static final MatchMap MAXSPEED = MatchMap.create( "maxspeed" );
    private static final MatchMap MAXSPEED_FORWARD = MatchMap.create( "maxspeed:forward" );
    private static final MatchMap MAXSPEED_NONE = MatchMap.create( new Pair( "maxspeed", "none" ) );
    private static final MatchMap MAXSPEED_LANES = MatchMap.create( "maxspeed:lanes" );
    private static final MatchMap SOURCE_MAXSPEED = MatchMap.create( "source:maxspeed" );
    private static final MatchMap SOURCE_MAXSPEED_IMPLICIT = MatchMap.create( new Pair( "source:maxspeed", "implicit" ) );
    private static final MatchMap SOURCE_MAXSPEED_SIGN = MatchMap.create( new Pair( "source:maxspeed", "sign" ) );
    private static final MatchMap HIGHWAY = MatchMap.create( "highway" );

    public EdgeAttributes parse( String countryCode, boolean insideCity, List<Pair> pairs, double length ) {
        Properties defaultSpeedProperties = new Properties();
        Properties maxSpeedProperties = new Properties();
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream( "cz/certicon/routing/data/osm/osm_defaultspeed_zone_type.properties" );
            defaultSpeedProperties.load( in );
            in.close();
            in = getClass().getClassLoader().getResourceAsStream( "cz/certicon/routing/data/osm/osm_maxspeed_source_zones.properties" );
            maxSpeedProperties.load( in );
            in.close();
        } catch ( IOException ex ) {
            throw new AssertionError( ex );
        }
//        System.out.println( "entries for maxspeedproperties" );
//        for ( Map.Entry<Object, Object> entry : maxSpeedProperties.entrySet() ) {
//            System.out.println( "entry = " + entry );
//        }

        boolean oneWay = false;
        boolean paid = false;
        double speed = -1;

        boolean none = false;
        boolean implicit = false;
        boolean sign = false;
        String zoneType = null;
        String wayType = null;

        for ( Pair pair : pairs ) {
            // order does matter
            if ( PAID.contains( pair ) ) {
                paid = true;
            }
            if ( ONE_WAY.contains( pair ) ) {
                oneWay = true;
            } else if ( MAXSPEED_NONE.contains( pair ) ) {
                none = true;
            } else if ( MAXSPEED.contains( pair ) || MAXSPEED_FORWARD.contains( pair ) ) { // ignores backward
                // ^([0-9][\.0-9]+?)(?:[ ]?(?:km/h|kmh|kph|mph|knots))?$
                if ( pair.value.matches( "^([0-9][\\.0-9]+?)$" ) ) {
                    String number = pair.value.replaceAll( "^([0-9][\\.0-9]+)$", "$1" );
                    speed = Integer.parseInt( number );
                } else if ( pair.value.matches( "^([0-9][\\.0-9]+?)(?:[ ]?(?:km/h|kmh|kph))?$" ) ) {
                    String number = pair.value.replaceAll( "^([0-9][\\.0-9]+?)(?:[ ]?(?:km/h|kmh|kph))?$", "$1" );
                    speed = Integer.parseInt( number );
                } else if ( pair.value.matches( "^([0-9][\\.0-9]+?)(?:[ ]?(?:mph))?$" ) ) {
                    String number = pair.value.replaceAll( "^([0-9][\\.0-9]+?)(?:[ ]?(?:mph))?$", "$1" );
                    speed = SpeedUtils.mphToKmph( Integer.parseInt( number ) );
                } else if ( pair.value.matches( "^([0-9][\\.0-9]+?)(?:[ ]?(?:knots))?$" ) ) {
                    String number = pair.value.replaceAll( "^([0-9][\\.0-9]+?)(?:[ ]?(?:knots))?$", "$1" );
                    speed = SpeedUtils.knotToKmph( Integer.parseInt( number ) );
                } else if ( pair.value.split( ":" ).length > 1 ) {
                    zoneType = pair.value;
                }
            }
            if ( MAXSPEED_LANES.contains( pair ) ) {
                String[] split = pair.value.split( "|" );
                speed = Integer.parseInt( split[0] );
            }
            if ( SOURCE_MAXSPEED_IMPLICIT.contains( pair ) ) {
                implicit = true;
            } else if ( SOURCE_MAXSPEED_SIGN.contains( pair ) ) {
                sign = true;
            } else if ( SOURCE_MAXSPEED.contains( pair ) ) {
                zoneType = pair.value;
            }
            if ( HIGHWAY.contains( pair ) ) {
                wayType = pair.value;
            }
        }
        if ( speed < 0 ) {
//            if ( implicit || none ) {
            if ( zoneType != null ) {
//                    String[] split = zoneType.split( ":" );
//                    String zone = split[0];
//                    String type = split[1];
                try {
                    speed = Integer.parseInt( maxSpeedProperties.getProperty( zoneType.replaceAll( ":", COUNTRY_CODE_DELIMITER ).toLowerCase() ) );
                } catch ( NumberFormatException ex ) {
                    error( pairs );
                }
            } else if ( wayType != null ) {
                try {
                    speed = Integer.parseInt( defaultSpeedProperties.getProperty( ( countryCode + COUNTRY_CODE_DELIMITER + wayType + ( insideCity ? "-inside" : "" ) ).toLowerCase() ) );
                } catch ( NumberFormatException ex ) {
                    error( pairs );
                }
            } else {
                error( pairs );
            }
//            } else {
//
//            }
        }

        return SimpleEdgeAttributes.builder( speed ).setLength( length ).setOneWay( oneWay ).setPaid( paid ).build();
    }

    private void error( List<Pair> pairs ) {
        StringBuilder sb = new StringBuilder();
        for ( Pair pair : pairs ) {
            sb.append( pair.key ).append( "=" ).append( pair.value ).append( "\n" );
        }
        throw new AssertionError( "Unknown speed! Attributes: \n" + sb.toString() );
    }

    private static class MatchMap {

        private final Map<String, Map<String, Integer>> map = new HashMap<>();

        private MatchMap() {
        }

        public static MatchMap create( Pair... pairs ) {
            MatchMap newMap = new MatchMap();
            for ( Pair pair : pairs ) {
                newMap.add( pair );
            }
            return newMap;
        }

        public static MatchMap create( String... groups ) {
            MatchMap newMap = new MatchMap();
            for ( String group : groups ) {
                newMap.add( group );
            }
            return newMap;
        }

        public static MatchMap create( List<Pair> pairs, List<String> groups ) {
            MatchMap newMap = new MatchMap();
            for ( Pair pair : pairs ) {
                newMap.add( pair );
            }

            for ( String group : groups ) {
                newMap.add( group );
            }
            return newMap;
        }

        private MatchMap add( String group ) {
            if ( !map.containsKey( group ) ) {
                Map<String, Integer> valMap = new HashMap<>();
                map.put( group, valMap );
            }
            return this;
        }

        private MatchMap add( Pair pair ) {
            Map<String, Integer> valMap = map.get( pair.key );
            if ( valMap == null ) {
                valMap = new HashMap<>();
                map.put( pair.key, valMap );
            }
            valMap.put( pair.value, 1 );
            return this;
        }

        public boolean contains( Pair pair ) {
            Map<String, Integer> valMap = map.get( pair.key );
            if ( valMap != null ) {
                if ( valMap.isEmpty() ) {
                    return true;
                } else {
                    return valMap.containsKey( pair.value );
                }
            }
            return false;
        }
    }

    public static class Pair {

        final String key;
        final String value;

        public Pair( String key, String value ) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Pair{" + "key=" + key + ", value=" + value + '}';
        }
    }
}
