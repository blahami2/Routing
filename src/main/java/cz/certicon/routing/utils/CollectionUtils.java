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
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class CollectionUtils {
      public static <Key,Value> List<Value> getList( Map<Key, List<Value>> map, Key node ) {
        List<Value> list = map.get( node );
        if ( list == null ) {
            list = new ArrayList<>();
            map.put( node, list );
        }
        return list;
    }

    public static int[] toIntArray( List<Integer> list ) {
        int[] array = new int[list.size()];
        for ( int i = 0; i < list.size(); i++ ) {
            array[i] = list.get( i );
        }
        return array;
    }
}
