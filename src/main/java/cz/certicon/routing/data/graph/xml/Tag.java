/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.graph.xml;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public enum Tag {
    ROOT, EDGE, NODE, ID, COORDINATE, LATITUDE, LONGITUDE, SPEED_FORWARD, SPEED_BACKWARD, ONEWAY, PAID, LENGTH, SOURCE, TARGET, WAY_ID;
    
    public String shortLowerName() {
        return this.name().toLowerCase();
    }

    public static Tag valueOfIgnoredCase( String tag ) {
        try {
            return valueOf( tag.toUpperCase() );
        } catch(IllegalArgumentException ex){
            throw new IllegalArgumentException("Failed valueOf for: '" + tag.toUpperCase() + "'");
        }
    }
}
