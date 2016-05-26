/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.measuring;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class StatsLogger {

    private static final Map<Statistic, Integer> STATS_MAP = new HashMap<>();

    public static void log( Statistic statistic, Command command ) {
        STATS_MAP.put( statistic, command.execute( getValue( statistic ) ) );
    }

    public static int getValue( Statistic statistic ) {
        Integer val = STATS_MAP.get( statistic );
        if ( val == null ) {
            val = 0;
            STATS_MAP.put( statistic, val );
        }
        return val;
    }

    public interface Command {

        int execute( int input );

        public static final Command INCREMENT = new Command() {
            @Override
            public int execute( int input ) {
                return input + 1;
            }
        };

        public static final Command RESET = new Command() {
            @Override
            public int execute( int input ) {
                return 0;
            }
        };

        public static final Command DECREMENT = new Command() {
            @Override
            public int execute( int input ) {
                return input - 1;
            }
        };
    }

    public static enum Statistic {
        NODES_EXAMINED, EDGES_EXAMINED;
    }
}
