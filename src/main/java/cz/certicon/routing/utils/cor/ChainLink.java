/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.cor;

/**
 * Link of the chain, which is able to execute a single operation on the
 * traveler. Links can be grouped into {@link ChainGroup}. Implementations offer
 * OR {@link OrChainGroup} or XOR {@link XorChainGroup} functionality.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <Traveler> traveling instance
 */
public interface ChainLink<Traveler> {

    /**
     * Execute the chain-link operation
     *
     * @param t traveler object
     * @return true if the operation was executed, false otherwise
     */
    public boolean execute( Traveler t );
}
