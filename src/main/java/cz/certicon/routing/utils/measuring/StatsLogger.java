/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.measuring;

import java.util.HashMap;
import java.util.Map;

/**
 * Logging class for statistic counters.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class StatsLogger {

    private static final Map<Statistic, Integer> STATS_MAP = new HashMap<>();

    /**
     * Log given {@link Command} for given {@link Statistic} data type
     *
     * @param statistic given statistic data type
     * @param command given command
     */
    public static void log( Statistic statistic, Command command ) {
        STATS_MAP.put( statistic, command.execute( getValue( statistic ) ) );
    }

    /**
     * Return value of the given {@link Statistic} counter
     *
     * @param statistic given statistic data type
     * @return counter
     */
    public static int getValue( Statistic statistic ) {
        Integer val = STATS_MAP.get( statistic );
        if ( val == null ) {
            val = 0;
            STATS_MAP.put( statistic, val );
        }
        return val;
    }

    /**
     * Interface for commands as well as wrapping class for predefined commands.
     */
    public interface Command {

        int execute( int input );

        /**
         * Increment given counter
         */
        public static final Command INCREMENT = new Command() {
            @Override
            public int execute( int input ) {
                return input + 1;
            }
        };

        /**
         * Reset given counter
         */
        public static final Command RESET = new Command() {
            @Override
            public int execute( int input ) {
                return 0;
            }
        };

        /**
         * Decrement given counter
         */
        public static final Command DECREMENT = new Command() {
            @Override
            public int execute( int input ) {
                return input - 1;
            }
        };
    }

    /**
     * Available statistic data types
     */
    public static enum Statistic {
        /**
         * Amount of examined (visited) nodes during algorithm execution
         */
        NODES_EXAMINED,
        /**
         * Amount of examined (visited) edges during algorithm execution
         */
        EDGES_EXAMINED;
    }
}
