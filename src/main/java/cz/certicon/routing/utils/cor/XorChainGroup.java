/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.cor;

/**
 * XOR implementation of the {@link ChainGroup} interface. Executes only until
 * the first true execution is met. Returns true, if such execution was found,
 * false otherwise.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <Traveller> traveling instance
 */
public class XorChainGroup<Traveller> extends AbstractChainGroup<Traveller> {

    @Override
    protected boolean executeNext( ChainLink<Traveller> next, Traveller t ) {
        if ( !next.execute( t ) ) {
            return next( t );
        } else {
            return true;
        }
    }
}
