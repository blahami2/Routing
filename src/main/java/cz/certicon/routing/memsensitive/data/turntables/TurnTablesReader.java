/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.data.turntables;

import cz.certicon.routing.memsensitive.model.entity.TurnTablesBuilder;
import cz.certicon.routing.memsensitive.model.entity.ch.PreprocessedData;
import java.io.IOException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface TurnTablesReader {

    public <T, G> T read( G graph, TurnTablesBuilder<T, G> builder ) throws IOException;

    public <T, G> T read( G graph, TurnTablesBuilder<T, G> builder, PreprocessedData preprocessedData ) throws IOException;
}
