/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.measuring;

import cz.certicon.routing.model.basic.TimeUnits;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class TimeLogger {

    private static final Map<Event, TimeMeasurement> TIME_MAP = new HashMap<>();
    private static TimeUnits timeUnits = TimeUnits.MILLISECONDS;

    public static void setTimeUnits( TimeUnits timeUnits ) {
        TimeLogger.timeUnits = timeUnits;
        for ( TimeMeasurement value : TIME_MAP.values() ) {
            value.setTimeUnits( timeUnits );
        }
    }

    public static void log( Event event, Command command ) {
        command.execute( getTimeMeasurement( event ) );
    }

    public static TimeMeasurement getTimeMeasurement( Event event ) {
        TimeMeasurement time = TIME_MAP.get( event );
        if ( time == null ) {
            time = new TimeMeasurement();
            time.setTimeUnits( timeUnits );
            TIME_MAP.put( event, time );
        }
        return time;
    }

    public static enum Event {
        PREPROCESSING, GRAPH_LOADING, PREPROCESSED_LOADING, NODE_SEARCHING, ROUTING, ROUTE_BUILDING, PATH_LOADING;
    }

    public static enum Command {
        START {
            @Override
            void execute( TimeMeasurement timeMeasurement ) {
                timeMeasurement.start();
            }
        }, STOP {
            @Override
            void execute( TimeMeasurement timeMeasurement ) {
                timeMeasurement.stop();
            }
        };

        abstract void execute( TimeMeasurement timeMeasurement );
    }
}
