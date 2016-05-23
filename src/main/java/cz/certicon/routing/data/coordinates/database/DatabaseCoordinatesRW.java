/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates.database;

import cz.certicon.routing.data.basic.database.AbstractServerDatabase;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import cz.certicon.routing.data.coordinates.CoordinateWriter;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Node;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * An implementation of the {@link CoordinateReader}/{@link CoordinateWriter}
 * interfaces using the {@link AbstractServerDatabase} class.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class DatabaseCoordinatesRW extends AbstractServerDatabase<Map<Edge, List<Coordinate>>, Set<Edge>> implements CoordinateReader, CoordinateWriter {

    public DatabaseCoordinatesRW( Properties connectionProperties ) {
        super( connectionProperties );
    }

    @Override
    protected Map<Edge, List<Coordinate>> checkedRead( Set<Edge> edges ) throws SQLException {
        Map<Long, List<Coordinate>> dataCoordinatesMap = new HashMap<>();
        Map<Edge, List<Coordinate>> coordinateMap = new HashMap<>();
        Map<Long, Edge> edgeMap = new HashMap<>();
        for ( Edge edge : edges ) {
            edgeMap.put( edge.getDataId(), edge );
        }
        ResultSet rs;
        StringBuilder inArray = new StringBuilder();
        for ( Edge edge : edges ) {
            inArray.append( edge.getDataId() ).append( ", " );
        }
        if ( edges.size() > 0 ) {
            inArray.delete( inArray.length() - 2, inArray.length() );
        } else {
            return coordinateMap;
        }
        rs = getStatement().executeQuery( "SELECT id, ST_AsText(geom) AS linestring "
                + "FROM edges_data_routing "
                + "WHERE id IN ("
                + inArray.toString()
                + ")" );
        int idColumnIdx = rs.findColumn( "id" );
        int linestringColumnIdx = rs.findColumn( "linestring" );
        while ( rs.next() ) {
            Long key = rs.getLong( idColumnIdx );
            if ( !dataCoordinatesMap.containsKey( key ) ) {
                List<Coordinate> coordinates = new ArrayList<>();
                String linestring = rs.getString( linestringColumnIdx );
                linestring = linestring.substring( "LINESTRING(".length(), linestring.length() - ")".length() );
                for ( String cord : linestring.split( "," ) ) {
                    Coordinate coord = new Coordinate(
                            Double.parseDouble( cord.split( " " )[1] ),
                            Double.parseDouble( cord.split( " " )[0] )
                    );
                    coordinates.add( coord );
                }
                dataCoordinatesMap.put( key, coordinates );
            }
        }
        for ( Edge edge : edges) {
            coordinateMap.put( edge, dataCoordinatesMap.get( edge.getDataId()) );
        }
        return coordinateMap;
    }

    @Override
    protected void checkedWrite( Map<Edge, List<Coordinate>> in ) throws SQLException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    private Coordinate parseCoord( ResultSet rs, int latColumnIndex, int lonColumnIndex ) throws SQLException {
        int lat = rs.getInt( latColumnIndex );
        int lon = rs.getInt( lonColumnIndex );
        return new Coordinate(
                (double) lat * 10E-8, (double) lon * 10E-8 );
    }
}
