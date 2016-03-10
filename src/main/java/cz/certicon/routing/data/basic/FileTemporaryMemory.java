/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.basic;

import cz.certicon.routing.data.DataDestination;
import cz.certicon.routing.data.DataSource;
import cz.certicon.routing.data.TemporaryMemory;
import java.io.File;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class FileTemporaryMemory implements TemporaryMemory {

    private final File file;

    public FileTemporaryMemory( File file ) {
        this.file = file;
    }

    @Override
    public DataDestination getMemoryAsDestination() {
        return new FileDestination( file );
    }

    @Override
    public DataSource getMemoryAsSource() {
        return new FileSource( file );
    }

}
