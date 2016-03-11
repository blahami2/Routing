/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.osm;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SmallWay {

    public final long wayId;
    public final List<SmallNode> nodes = new LinkedList<>();

    public SmallWay( long wayId ) {
        this.wayId = wayId;
    }

}
