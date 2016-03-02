/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Restriction {

    private final Map<String, Map<String, Integer>> allowed = new HashMap<>();
    private final Map<String, Map<String, Integer>> forbidden = new HashMap<>();

    public static Restriction getDefault() {
        Restriction r = new Restriction();
        r.addAllowedGroup( "highway" );
        r.addForbiddenPair( "motor_vehicle", "no" );
        return r;
    }

    public Restriction addAllowedGroup( String key ) {
        return addGroup( allowed, key );
    }

    public Restriction addAllowedPair( String key, String value ) {
        return addPair( allowed, new Pair( key, value ) );
    }

    public Restriction addForbiddenGroup( String key ) {
        return addGroup( forbidden, key );
    }

    public Restriction addForbiddenPair( String key, String value ) {
        return addPair( forbidden, new Pair( key, value ) );
    }

    public boolean isAllowed( List<Pair> pairs ) {
//        System.out.println( "forbidden check" );
        if ( pairs.stream().noneMatch( ( p ) -> ( isInMap( forbidden, p ) ) ) ) {
        } else {
            return false;
        }
//        System.out.println( "allowed check" );
        return pairs.stream().anyMatch( ( p ) -> ( isInMap( allowed, p ) ) );
    }

    private boolean isInMap( Map<String, Map<String, Integer>> keys, Pair pair ) {
//        System.out.println( "checking pair: " + pair );
        Map<String, Integer> map = keys.get( pair.key );
        if ( map != null ) {
//            System.out.println( "group is there" );
            if ( map.isEmpty() ) {
//                System.out.println( "is ok" );
                return true;
            } else {
//                System.out.println( "is " + ( ( map.containsKey( pair.value ) ) ? "" : "NOT " ) + "ok" );
                return map.containsKey( pair.value );
            }
        } else {
//            System.out.println( "is NOT ok" );
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

    private Restriction addGroup( Map<String, Map<String, Integer>> keys, String key ) {
        Map<String, Integer> vals = keys.get( key );
        if ( vals == null ) {
            vals = new HashMap<>();
            keys.put( key, vals );
        }
        return this;
    }

    private Restriction addPair( Map<String, Map<String, Integer>> keys, Pair pair ) {
        Map<String, Integer> map = keys.get( pair.key );
        if ( map == null ) {
            map = new HashMap<>();
            keys.put( pair.key, map );
        }
        map.put( pair.value, 1 );
//        System.out.println( "put in: " + pair.key + ", " + pair.value );
        return this;
    }
}
