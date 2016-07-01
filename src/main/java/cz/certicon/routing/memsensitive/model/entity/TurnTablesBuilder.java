/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity;

import cz.certicon.routing.memsensitive.model.entity.ch.PreprocessedData;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface TurnTablesBuilder<T, G> {

    public void addRestriction( G graph, PreprocessedData chData, int arrayId, long from, int fromPosition, long via, long to );

    public void addRestriction( G graph, int arrayId, long from, int fromPosition, long via, long to );

    public T build( G graph, PreprocessedData chData );

    public T build( G graph );
}
