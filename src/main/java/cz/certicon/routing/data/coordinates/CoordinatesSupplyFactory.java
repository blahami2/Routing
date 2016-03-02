/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates;

import java.net.URI;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface CoordinatesSupplyFactory {

    public CoordinatesReader createReader( URI sourceUri );

    public CoordinatesWriter createWriter( URI targetUri );
}
