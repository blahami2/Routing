/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils;

import cz.certicon.routing.model.entity.Coordinates;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for communication
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
public class CommunicationUtils {

    /**
     * Serves for decoding polyline string into the list of coordinates. 
     * 
     * @param encoded polyline coordinate string
     * @return list of decoded coordinates 
     */
    private List<Coordinates> decodePolyline( String encoded ) {
        List<Coordinates> poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while ( index < len ) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt( index++ ) - 63;
                result |= ( b & 0x1f ) << shift;
                shift += 5;
            } while ( b >= 0x20 );
            int dlat = ( ( result & 1 ) != 0 ? ~( result >> 1 ) : ( result >> 1 ) );
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt( index++ ) - 63;
                result |= ( b & 0x1f ) << shift;
                shift += 5;
            } while ( b >= 0x20 );
            int dlng = ( ( result & 1 ) != 0 ? ~( result >> 1 ) : ( result >> 1 ) );
            lng += dlng;
            Coordinates p = new Coordinates( ( ( (double) lat / 1E5 ) ),
                    ( ( (double) lng / 1E5 ) ) );
            poly.add( p );
        }
        return poly;
    }
}
