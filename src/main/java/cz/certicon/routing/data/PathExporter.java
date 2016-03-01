/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data;

import cz.certicon.routing.model.entity.Path;
import java.io.IOException;

/**
 * The root interface for exporting the routed path into a {@link DataDestination}
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
public interface PathExporter {

    /**
     * Exports the given {@link Path} into the given {@link DataDestination}
     *
     * @param destination an instance of {@link DataDestination} to export the path into
     * @param path an instance of {@link Path} to be exported
     * @throws IOException thrown when an error occurs during the export
     */
    public void exportPath( DataDestination destination, Path path ) throws IOException;
}
