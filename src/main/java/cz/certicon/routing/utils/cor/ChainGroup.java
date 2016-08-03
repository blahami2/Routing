/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.cor;

/**
 * Group of chain-links, which acts as a single chain-link on the outside
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <Traveller> traveling instance
 */
public interface ChainGroup<Traveller> extends ChainLink<Traveller> {

    /**
     * Adds chain-link into this group
     *
     * @param chainlink chain-link to be added
     */
    public void addChainLink( ChainLink chainlink );

    /**
     * Calls next chain-link in this group, returns result of the sub-link's
     * execute operation or false if no more chain-links are present. The
     * concrete behavior is based on the implementation.
     *
     * @param traveler traveling object
     * @return result of execute operation or false if no more chain-links are
     * present
     */
    public boolean next( Traveller traveler );
}
