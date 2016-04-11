/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.osm;

/**
 * Way tags
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public enum TagKey {
    AERIALWAY,
    AEROWAY,
    AMENITY,
    BARRIER,
    BOUNDARY,
    BUILDING,
    CRAFT,
    EMERGENCY,
    GEOLOGICAL,
    HIGHWAY,
    HISTORIC,
    LANDUSE,
    LEISURE,
    MAN_MADE,
    MILITARY,
    NATURAL,
    OFFICE,
    PLACES,
    POWER,
    PUBLIC_TRANSPORT,
    RAILWAY,
    ROUTE,
    SHOP,
    SPORT,
    TOURISM,
    WATERWAY,
    NAME /*todo shitload of other http://wiki.openstreetmap.org/wiki/Map_Features#Additional_properties*/;

    public static TagKey parse( String tagKey ) {
        TagKey key;
        try {
            return valueOf( tagKey.toUpperCase());
        } catch(IllegalArgumentException e){
            return null;
        }
    }
}
