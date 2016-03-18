/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.coordinates.osm2po;

import cz.certicon.routing.data.basic.database.AbstractDatabase;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import cz.certicon.routing.data.coordinates.CoordinateWriter;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Edge;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.postgis.PGgeometry;
import org.postgis.Point;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class Osm2poCoordinateRW extends AbstractDatabase<Map<Edge, List<Coordinate>>, Set<Edge>> implements CoordinateReader, CoordinateWriter {

    @Override
    protected Map<Edge, List<Coordinate>> checkedRead( Set<Edge> in ) throws SQLException {
        Map<Edge, List<Coordinate>> edgeMap = new HashMap<>();
        PreparedStatement ps = getConnection().prepareStatement( "SELECT geom_way AS geom FROM prg_2po_4pgr WHERE (id = ?);" );
        for ( Edge edge : in ) {
            ps.setLong( 1, edge.getId().getValue() );
//            System.out.println( ps.toString() );
            ResultSet r = ps.executeQuery();
            List<Coordinate> coordinates = new ArrayList<>();
            while ( r.next() ) {
                PGgeometry g = r.getObject( "geom", PGgeometry.class );
                for(int i = 0; i < g.getGeometry().numPoints(); i++){
                    Point point = g.getGeometry().getPoint( i );
//                    System.out.println( "x = " + point.x );
//                    System.out.println( "y = " + point.y );
//                    System.out.println( "z = " + point.z );
                    double lon = point.x;
                    double lat = point.y;
                    coordinates.add( new Coordinate( lat, lon ) );
                }
//                String coordString = r.getString( "geom" );
//                System.out.println( coordString );
//                coordString = coordString.replaceAll( "<[^>]*>", "" );
//                System.out.println( coordString );
//                Scanner sc = new Scanner( coordString );
//                while ( sc.hasNextDouble() ) {
//                    double lon = sc.nextDouble();
//                    double lat = sc.nextDouble();
//                    coordinates.add( new Coordinate( lat, lon ) );
//                    System.out.println( "added coordinate: " + coordinates.get( coordinates.size() - 1 ) );
//                }
//                System.out.println( "next = '" + sc.next() + "'" );
            }
            edgeMap.put( edge, coordinates );
        }
        return edgeMap;
    }

    @Override
    protected void checkedWrite( Map<Edge, List<Coordinate>> in ) throws SQLException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
}
