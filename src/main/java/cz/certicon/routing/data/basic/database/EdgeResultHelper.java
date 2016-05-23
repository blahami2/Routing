/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.basic.database;

import cz.certicon.routing.model.entity.Coordinate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class EdgeResultHelper {

    private final ResultSet resultSet;

    public EdgeResultHelper( ResultSet resultSet ) {
        this.resultSet = resultSet;
    }

    public long getId() throws SQLException {
        return resultSet.getLong( Columns.ID.getName() );
    }

    public long getOsmId() throws SQLException {
        return resultSet.getLong( Columns.OSM_ID.getName() );
    }

    public long getDataId() throws SQLException {
        return resultSet.getLong( Columns.DATA_ID.getName() );
    }

    public long getSourceId() throws SQLException {
        return resultSet.getLong( Columns.SOURCE.getName() );
    }

    public long getTargetId() throws SQLException {
        return resultSet.getLong( Columns.TARGET.getName() );
    }

    public int getSpeed() throws SQLException {
        return resultSet.getInt( Columns.SPEED.getName() );
    }

    public boolean getIsPaid() throws SQLException {
        return resultSet.getBoolean( Columns.IS_PAID.getName() );
    }

    public int getRoadType() throws SQLException {
        return resultSet.getInt( Columns.ROAD_TYPE.getName() );
    }

    public double getLength() throws SQLException {
        return resultSet.getDouble( Columns.LENGTH.getName() );
    }

    public boolean getIsForward() throws SQLException {
        return resultSet.getBoolean( Columns.IS_FORWARD.getName() );
    }

    public List<Coordinate> getGeometry() throws SQLException {
        List<Coordinate> coordinates = new ArrayList<>();
        String linestring = resultSet.getString( Columns.GEOMETRY.getName() );
        linestring = linestring.substring( "LINESTRING(".length(), linestring.length() - ")".length() );
        for ( String cord : linestring.split( "," ) ) {
            Coordinate coord = new Coordinate(
                    Double.parseDouble( cord.split( " " )[1] ),
                    Double.parseDouble( cord.split( " " )[0] )
            );
            coordinates.add( coord );
        }
        return coordinates;
    }

    public static enum Columns implements ColumnInterface {
        ID( "id" ), IS_FORWARD( "is_forward" ), OSM_ID( "osm_id" ), DATA_ID( "data_id" ), SOURCE( "source_id" ), TARGET( "target_id" ), SPEED( "speed" ), IS_PAID( "is_paid" ), ROAD_TYPE( "road_type" ), GEOMETRY( "ST_AsText(geom)" ), LENGTH( "length" );

        private final String name;

        private Columns( String name ) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public static String select( ColumnInterface... columns ) {
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < columns.length; i++ ) {
            ColumnInterface column = columns[i];
            sb.append( column.getName() );
            if ( i < columns.length - 1 ) {
                sb.append( ", " );
            }
        }
        return sb.toString();
    }

    public interface ColumnInterface {

        public String getName();

    }

    private static class StringColumn implements ColumnInterface {

        private final String name;

        public StringColumn( String name ) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

    }
}
