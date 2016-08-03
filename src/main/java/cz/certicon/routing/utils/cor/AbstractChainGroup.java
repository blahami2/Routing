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
 * Abstract implementation of {@link ChainGroup} interface. Adds implementation
 * of the execute operation and next operation.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <Traveler> traveling instance
 */
public abstract class AbstractChainGroup<Traveler> implements ChainGroup<Traveler> {

    private final List<ChainLink<Traveler>> list = new LinkedList<>();
    private Iterator<ChainLink<Traveler>> iterator = null;

    @Override
    public void addChainLink( ChainLink cl ) {
        list.add( cl );
    }

    @Override
    public boolean execute( Traveler t ) {
        iterator = list.iterator();
        return next( t );
    }

    @Override
    public boolean next( Traveler t ) {
        if ( !getIterator().hasNext() ) {
            return false;
        }
        ChainLink<Traveler> next = getIterator().next();
        return executeNext( next, t );
    }

    /**
     * Let the extending class choose, whether to call next or not.
     *
     * @param next the next chain-link in line
     * @param traveler traveling object
     * @return result of the execution
     */
    abstract protected boolean executeNext( ChainLink<Traveler> next, Traveler traveler );

    protected Iterator<ChainLink<Traveler>> getIterator() {
        return iterator;
    }
}
