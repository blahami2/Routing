/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.nodesearch.database;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.EdgeData;
import cz.certicon.routing.data.basic.database.AbstractServerDatabase;
import cz.certicon.routing.data.basic.database.EdgeResultHelper;
import cz.certicon.routing.data.nodesearch.NodeSearcher;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.common.SimpleEdgeData;
import cz.certicon.routing.utils.DoubleComparator;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class DatabaseNodeSearcher implements NodeSearcher {

    private final NodeSearchDB database;

    public DatabaseNodeSearcher( Properties connectionProperties ) {
        this.database = new NodeSearchDB( connectionProperties );
    }

    @Override
    public Map<Node.Id, Distance> findClosestNodes( Coordinates coordinates, DistanceFactory distanceFactory, NodeSearcher.SearchFor searchFor ) throws IOException {
        database.setDistanceFactory( distanceFactory );
        database.open();
        Map<Node.Id, Distance> read = database.read( coordinates );
        database.close();
        return read;
    }

    private static class NodeSearchDB extends AbstractServerDatabase<Map<Node.Id, Distance>, Coordinates> {

        private DistanceFactory distanceFactory;

        public NodeSearchDB( Properties connectionProperties ) {
            super( connectionProperties );
        }

        public void setDistanceFactory( DistanceFactory distanceFactory ) {
            this.distanceFactory = distanceFactory;
        }

        @Override
        protected Map<Node.Id, Distance> checkedRead( Coordinates in ) throws SQLException {
            Map<Node.Id, Distance> map = new HashMap<>();
            ResultSet rs;
//            rs = getStatement().executeQuery( "SELECT * FROM nodes_view n WHERE ST_Equals(n.geom, ST_GeomFromText('POINT(" + in.getLongitude() + " " + in.getLatitude() + ")',4326));" );
//            if ( rs.next() ) {
//                map.put( in, distanceFactory.createZeroDistance() );
//                return map;
//            }
            rs = getStatement().executeQuery( "SELECT " + EdgeResultHelper.select( EdgeResultHelper.Columns.SPEED, EdgeResultHelper.Columns.IS_PAID, EdgeResultHelper.Columns.LENGTH
            ) + ", out_point AS point, out_distance AS distance FROM public.\"find_node\"(" + in.getLongitude() + ", " + in.getLatitude() + ");" );

            while ( rs.next() ) {
                EdgeResultHelper edgeResultHelper = new EdgeResultHelper( rs );
                long nodeId = rs.getLong( "point" );
                double length = rs.getDouble( "distance" );
                if ( DoubleComparator.compare( 0, length, 0.0000001 ) == 0 ) {
                    map.clear();
                    map.put( Node.Id.createId( nodeId ), distanceFactory.createZeroDistance() );
                    return map;
                }
                EdgeData edgeData = new SimpleEdgeData( edgeResultHelper.getSpeed(), edgeResultHelper.getIsPaid(), edgeResultHelper.getLength() );
                map.put( Node.Id.createId( nodeId ), distanceFactory.createFromEdgeDataAndLength( edgeData, length ) );
            }

//            DSLContext dsl = DSL.using( getConnection(), SQLDialect.POSTGRES_9_5 );
//            Result<Record> fetch = dsl.select().from( "public.\"find_node\"(" + in.getLongitude() + ", " + in.getLatitude() + ")" ).fetch();
//            SQLQuery sqlQuery = new SQLQuery( getConnection(), new PostgreSQLTemplates() );
//            List fetch1 = sqlQuery.select( Expressions.asString( "ST_AsText(out_point) AS point" ), Expressions.asString( "out_distance AS distance" ) )
//                    .from( Expressions.asString( "public.\"find_node\"(" + in.getLongitude() + ", " + in.getLatitude() + ")" ) )
//                    .fetch();
//            for ( Object object : fetch1 ) {
//
//            }
//            for ( Record record : fetch ) {
//                String value = record.getValue( "", String.class );
//                value = value.substring( "POINT(".length(), value.length() - ")".length() );
//                String[] lonlat = value.split( " " );
//                Coordinates node = new Coordinates(
//                        Double.parseDouble( lonlat[1] ),
//                        Double.parseDouble( lonlat[0] )
//                );
//                Double length = record.getValue( "", Double.class );
//                map.put( node, length );
//            }
            return map;
        }

        @Override
        protected void checkedWrite( Map<Node.Id, Distance> in ) throws SQLException {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
