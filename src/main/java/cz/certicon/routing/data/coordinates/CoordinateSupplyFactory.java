/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.DataSource;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface CoordinateSupplyFactory {

    public CoordinateReader createReader( DataSource source );

    public CoordinateWriter createWriter( DataDestination destination );
}
