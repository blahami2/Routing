/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.graph.sqlite;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import static cz.certicon.routing.GlobalOptions.MEASURE_TIME;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.EdgeData;
import cz.certicon.routing.data.basic.database.AbstractEmbeddedDatabase;
import cz.certicon.routing.data.graph.GraphReader;
import cz.certicon.routing.data.graph.GraphWriter;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.common.SimpleEdgeAttributes;
import cz.certicon.routing.model.entity.common.SimpleEdgeData;
import cz.certicon.routing.utils.GeometryUtils;
import cz.certicon.routing.utils.measuring.TimeLogger;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sqlite.SQLiteConfig;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SqliteGraphRW extends AbstractEmbeddedDatabase<Graph, Pair<GraphEntityFactory, DistanceFactory>> implements GraphReader, GraphWriter {

    private final String spatialitePath;
//    private final String libspatialitePath;

    public SqliteGraphRW( Properties connectionProperties ) throws IOException {
        super( connectionProperties );
        SQLiteConfig config = new SQLiteConfig();
        config.enableLoadExtension( true );
        for ( Map.Entry<Object, Object> entry : config.toProperties().entrySet() ) {
            connectionProperties.put( entry.getKey(), entry.getValue() );
        }
        this.spatialitePath = connectionProperties.getProperty( "spatialite_path" );
    }

    @Override
    public void open() throws IOException {
        super.open();
        try {
            getStatement().execute( "SELECT load_extension('" + spatialitePath + "')" );
//        this.libspatialitePath = libspatialitePath;
        } catch ( SQLException ex ) {
            throw new IOException( ex );
        }
    }

    @Override
    protected Graph checkedRead( Pair<GraphEntityFactory, DistanceFactory> in ) throws SQLException {
        Map<Node.Id, Node> nodeMap = new HashMap<>();
        GraphEntityFactory graphEntityFactory = in.a;
        DistanceFactory distanceFactory = in.b;
        Graph graph = graphEntityFactory.createGraph();
        ResultSet rs;
        rs = getStatement().executeQuery( "SELECT n.id AS id, ST_AsText(d.geom) AS geom, d.osm_id AS osm_id "
                + "FROM nodes n "
                + "JOIN nodes_data d ON n.data_id = d.id;" );
        int idColumnIdx = rs.findColumn( "id" );
        int pointColumnIdx = rs.findColumn( "geom" );
        int osmIdColumnIdx = rs.findColumn( "osm_id" );
        while ( rs.next() ) {
//            System.out.println( rs.getLong( "id" ) );
//            System.out.println( "osm = " + rs.getLong( "osm_id" ) );
//            System.out.println( rs.getString( pointColumnIdx ) );
            Coordinate coords = GeometryUtils.toCoordinatesFromWktPoint( rs.getString( pointColumnIdx ) );
            Node node = graphEntityFactory.createNode( Node.Id.createId( rs.getLong( idColumnIdx ) ),
                    coords.getLatitude(), coords.getLongitude()
            );
            node.setOsmId( rs.getLong( osmIdColumnIdx ) );
            graph.addNode( node );
            nodeMap.put( node.getId(), node );
        }
        rs = getStatement().executeQuery( "SELECT e.id, e.is_forward, e.data_id, e.source_id, e.target_id, d.length, d.is_paid, d.speed_fw, d.speed_bw, d.osm_id "
                + "FROM edges e "
                + "JOIN edges_data d ON e.data_id = d.id;" );
        idColumnIdx = rs.findColumn( "id" );
        int dataIdColumnIndex = rs.findColumn( "data_id" );
        int sourceColumnIndex = rs.findColumn( "source_id" );
        int targetColumnIndex = rs.findColumn( "target_id" );
        int lengthColumnIndex = rs.findColumn( "length" );
        int paidColumnIndex = rs.findColumn( "is_paid" );
        int forwardColumnIndex = rs.findColumn( "is_forward" );
        int speedFwColumnIndex = rs.findColumn( "speed_fw" );
        int speedBwColumnIndex = rs.findColumn( "speed_bw" );
        int osmIdColumnIndex = rs.findColumn( "osm_id" );
        while ( rs.next() ) {
            Node.Id sourceId = Node.Id.createId( rs.getLong( sourceColumnIndex ) );
            Node.Id targetId = Node.Id.createId( rs.getLong( targetColumnIndex ) );
            EdgeData edgeData = new SimpleEdgeData(
                    ( rs.getInt( forwardColumnIndex ) == 1 ) ? rs.getInt( speedFwColumnIndex ) : rs.getInt( speedBwColumnIndex ),
                    rs.getInt( paidColumnIndex ) == 1,
                    rs.getDouble( lengthColumnIndex )
            );
            Edge edge = graphEntityFactory.createEdge(
                    Edge.Id.createId( rs.getLong( idColumnIdx ) ),
                    nodeMap.get( sourceId ),
                    nodeMap.get( targetId ),
                    distanceFactory.createFromEdgeData( edgeData )
            );
            edge.setAttributes( SimpleEdgeAttributes.builder()
                    .setLength( edgeData.getLength() )
                    .setPaid( edgeData.isPaid() )
                    .build() );
            edge.setSpeed( edgeData.getSpeed() );
            edge.setOsmId( rs.getLong( osmIdColumnIndex ) );
            edge.setDataId( rs.getLong( dataIdColumnIndex ) );
            graph.addEdge( edge );
        }
        return graph;
    }

    @Override
    protected void checkedWrite( Graph graph ) throws SQLException {
//        getConnection().setAutoCommit( false ); //transaction block start
//        // initialize SpatiaLite
//        getStatement().execute( "SELECT load_extension('" + libspatialitePath + "')" );
//        getStatement().execute( "SELECT InitSpatialMetadata()" );
//        // create tables
//        getStatement().execute( "CREATE TABLE edges_data ("
//                + "id INTEGER NOT NULL PRIMARY KEY,"
//                + "osm_id INTEGER,"
//                + "is_paid INTEGER,"
//                + "length REAL,"
//                + "speed_fw INTEGER,"
//                + "speed_bw INTEGER"
//                + ");" );
//        getStatement().execute( "SELECT AddGeometryColumn('edges_data','geom',4326,'LINESTRING','XY')" );
//        getStatement().execute( "CREATE TABLE edges ("
//                + "id INTEGER NOT NULL PRIMARY KEY,"
//                + "data_id INTEGER,"
//                + "is_forward INTEGER,"
//                + "source_id INTEGER,"
//                + "target_id INTEGER"
//                + ");" );
//        getStatement().execute( "CREATE TABLE nodes_data ("
//                + "id INTEGER NOT NULL PRIMARY KEY,"
//                + "osm_id INTEGER"
//                + ");" );
//        getStatement().execute( "SELECT AddGeometryColumn('nodes_data','geom',4326,'POINT','XY')" );
//        getStatement().execute( "CREATE TABLE nodes ("
//                + "id INTEGER NOT NULL PRIMARY KEY,"
//                + "data_id INTEGER"
//                + ");" );
//        // populate database
//        PreparedStatement edgeDataStatement = getConnection().prepareStatement( "INSERT INTO edges_data (id, osm_id, is_paid, length, speed_fw, speed_bw, state, geom) VALUES (?, ?, ?, ?, ?, ?, ?, ?)" );
//        Map<Long, EdgeRoutingData> edgesDataMap = new HashMap<>();
//        for ( Edge edge : graph.getEdges() ) {
//            EdgeRoutingData data = edgesDataMap.get( edge.getDataId() );
//            if ( data == null ) {
//                data = new EdgeRoutingData( edge.getDataId(), edge.getOsmId(), edge.getAttributes().isPaid(), edge.getAttributes().getLength() );
//                
//                edgesDataMap.put( edge.getDataId(), data );
//            }
//            if ( edgesDataMap.containsKey( edge.getDataId() ) ) {
//
//            } else {
//
//            }
//        }
//        getConnection().commit();
    }

//    private static class EdgeRoutingData {
//
//        private final long id;
//        private final long osmId;
//        private final boolean isPaid;
//        private final double length;
//        private int speedForward;
//        private int speedBackward;
//
//        public EdgeRoutingData( long id, long osmId, boolean isPaid, double length ) {
//            this.id = id;
//            this.osmId = osmId;
//            this.isPaid = isPaid;
//            this.length = length;
//        }
//
//        public void setSpeedForward( int speedForward ) {
//            this.speedForward = speedForward;
//        }
//
//        public void setSpeedBackward( int speedBackward ) {
//            this.speedBackward = speedBackward;
//        }
//
//        public long getId() {
//            return id;
//        }
//
//        public long getOsmId() {
//            return osmId;
//        }
//
//        public boolean isIsPaid() {
//            return isPaid;
//        }
//
//        public double getLength() {
//            return length;
//        }
//
//        public int getSpeedForward() {
//            return speedForward;
//        }
//
//        public int getSpeedBackward() {
//            return speedBackward;
//        }
//
//        @Override
//        public int hashCode() {
//            int hash = 5;
//            hash = 23 * hash + (int) ( this.id ^ ( this.id >>> 32 ) );
//            return hash;
//        }
//
//        @Override
//        public boolean equals( Object obj ) {
//            if ( this == obj ) {
//                return true;
//            }
//            if ( obj == null ) {
//                return false;
//            }
//            if ( getClass() != obj.getClass() ) {
//                return false;
//            }
//            final EdgeRoutingData other = (EdgeRoutingData) obj;
//            if ( this.id != other.id ) {
//                return false;
//            }
//            return true;
//        }
//
//    }
}
