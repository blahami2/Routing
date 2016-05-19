/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.data.ch;

import cz.certicon.routing.model.entity.ch.ChDataFactory;
import java.io.IOException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface ContractionHierarchiesDataRW {

    public <T> T read( ChDataFactory<T> chDataFactory ) throws NotPreprocessedException, IOException;

    public <T> void write( ChDataFactory<T> chDataFactory, T entity ) throws IOException;
}
