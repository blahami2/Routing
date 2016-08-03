/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for frequent operations with collections.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class CollectionUtils {

    /**
     * Returns list from a map referenced by the given key. If the list is not
     * present under the given key, a new list is created, inserted into the map
     * and returned. In other words, it ensures retrieval of a list for the
     * given key.
     *
     * @param <Key> key type of the map
     * @param <Value> value type of the list (which acts as a value in the map)
     * @param map key-value map, where value is of type {@link List}
     * @param node key
     * @return non-null {@link List} for the given key
     */
    public static <Key, Value> List<Value> getList( Map<Key, List<Value>> map, Key node ) {
        List<Value> list = map.get( node );
        if ( list == null ) {
            list = new ArrayList<>();
            map.put( node, list );
        }
        return list;
    }

    /**
     * Converts {@link List} of Integers into an int array
     *
     * @param list list to be converted
     * @return int[] representation
     */
    public static int[] toIntArray( List<Integer> list ) {
        int[] array = new int[list.size()];
        for ( int i = 0; i < list.size(); i++ ) {
            array[i] = list.get( i );
        }
        return array;
    }
}
