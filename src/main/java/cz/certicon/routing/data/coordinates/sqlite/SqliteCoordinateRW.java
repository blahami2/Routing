/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates.sqlite;

import cz.certicon.routing.data.basic.database.AbstractEmbeddedDatabase;
import cz.certicon.routing.data.basic.database.impl.AbstractSqliteDatabase;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import cz.certicon.routing.data.coordinates.CoordinateWriter;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.utils.GeometryUtils;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.sqlite.SQLiteConfig;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SqliteCoordinateRW extends AbstractSqliteDatabase<Map<Edge, List<Coordinates>>, Set<Edge>> implements CoordinateReader, CoordinateWriter {

    public SqliteCoordinateRW( Properties connectionProperties ) {
        super( connectionProperties );
    }

    @Override
    protected Map<Edge, List<Coordinates>> checkedRead( Set<Edge> edges ) throws SQLException {
        Map<Long, List<Coordinates>> dataCoordinatesMap = new HashMap<>();
        Map<Edge, List<Coordinates>> coordinateMap = new HashMap<>();
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
                + "FROM edges_data "
                + "WHERE id IN ("
                + inArray.toString()
                + ")" );
        int idColumnIdx = rs.findColumn( "id" );
        int linestringColumnIdx = rs.findColumn( "linestring" );
        while ( rs.next() ) {
            Long key = rs.getLong( idColumnIdx );
            if ( !dataCoordinatesMap.containsKey( key ) ) {
                List<Coordinates> coordinates = GeometryUtils.toCoordinatesFromWktLinestring( rs.getString( linestringColumnIdx ) );
                dataCoordinatesMap.put( key, coordinates );
            }
        }
        for ( Edge edge : edges ) {
            coordinateMap.put( edge, dataCoordinatesMap.get( edge.getDataId() ) );
        }
        return coordinateMap;
    }

    @Override
    protected void checkedWrite( Map<Edge, List<Coordinates>> in ) throws SQLException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

}
