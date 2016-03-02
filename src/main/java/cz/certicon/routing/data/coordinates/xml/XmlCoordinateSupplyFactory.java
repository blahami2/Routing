/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates.xml;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.coordinates.CoordinateWriter;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import cz.certicon.routing.data.coordinates.CoordinateSupplyFactory;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class XmlCoordinateSupplyFactory implements CoordinateSupplyFactory {

    @Override
    public CoordinateReader createReader( DataSource source ) {
        return new XmlCoordinateReader( source );
    }

    @Override
    public CoordinateWriter createWriter( DataDestination destination ) {
        return new XmlCoordinateWriter( destination );
    }

}
