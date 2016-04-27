/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.cor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <Traveller> travelling instance
 */
public abstract class SimpleChainGroup<Traveller> implements ChainGroup<Traveller> {

        private final List<ChainLink<Traveller>> list = new LinkedList<>();
        private Iterator<ChainLink<Traveller>> iterator = null;

        @Override
        public void addChainLink( ChainLink cl ) {
            list.add( cl );
        }

        @Override
        public boolean execute( Traveller t ) {
            iterator = list.iterator();
            return next( t );
        }

        @Override
        public boolean next( Traveller t ) {
            if ( !getIterator().hasNext() ) {
                return false;
            }
            ChainLink<Traveller> next = getIterator().next();
            return executeNext( next, t );
        }

        abstract protected boolean executeNext( ChainLink<Traveller> next, Traveller t );

        protected Iterator<ChainLink<Traveller>> getIterator() {
            return iterator;
        }
    }