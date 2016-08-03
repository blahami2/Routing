/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.cor;

/**
 * OR implementation of the {@link ChainGroup} interface. Executes all the
 * sublinks. Returns true if ANY of the sublinks' executions is true.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <Traveller> traveling instance
 */
public class OrChainGroup<Traveller> extends AbstractChainGroup<Traveller> {

    @Override
    protected boolean executeNext( ChainLink<Traveller> next, Traveller t ) {
        return next.execute( t );
    }

}
