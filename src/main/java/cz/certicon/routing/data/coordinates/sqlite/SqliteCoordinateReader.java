/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates.sqlite;

import cz.certicon.routing.data.basic.database.impl.StringSqliteReader;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import cz.certicon.routing.model.entity.CoordinateSetBuilder;
import cz.certicon.routing.model.entity.CoordinateSetBuilderFactory;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.utils.GeometryUtils;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * An implementation of the {@link CoordinateReader} interface based on the
 * SQLite database.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SqliteCoordinateReader implements CoordinateReader {

    private final StringSqliteReader reader;

    /**
     * See {@link AbstractSqliteDatabase} for further details.
     *
     * @param connectionProperties use SQLite database properties.
     */
    public SqliteCoordinateReader( Properties connectionProperties ) {
        this.reader = new StringSqliteReader( connectionProperties );
    }

    // TODO more memory-efficient to search only for a set of data_id, not edge_id, however it requires building sets and maps and more complex operations
    @Override
    public <T> T readCoordinates( CoordinateSetBuilderFactory<T> coordinateSetBuilderFactory, Iterator<Long> edgeIds ) throws IOException {
        CoordinateSetBuilder<T> coordinateSetBuilder = coordinateSetBuilderFactory.createCoordinateSetBuilder();
        ResultSet rs;
        StringBuilder inArray = new StringBuilder();
        while ( edgeIds.hasNext() ) {
            inArray.append( edgeIds.next() ).append( "," );
        }
        if ( inArray.length() > 0 ) {
            try {
                inArray.delete( inArray.length() - 1, inArray.length() );
                rs = reader.read( "SELECT e.id AS edge_id, ST_AsText(d.geom) AS linestring "
                        + "FROM edges e "
                        + "JOIN edges_data d "
                        + "ON e.id = d.data_id "
                        + "WHERE e.id IN ("
                        + inArray.toString()
                        + ")" );
                int idColumnIdx = rs.findColumn( "edge_id" );
                int linestringColumnIdx = rs.findColumn( "linestring" );
                while ( rs.next() ) {
                    long id = rs.getLong( idColumnIdx );
                    List<Coordinate> coordinates = GeometryUtils.toCoordinatesFromWktLinestring( rs.getString( linestringColumnIdx ) );
                    coordinateSetBuilder.addCoordinates( id, coordinates );
                }
            } catch ( SQLException ex ) {
                throw new IOException( ex );
            }
        }
        reader.close();
        return coordinateSetBuilder.build();
    }

}
