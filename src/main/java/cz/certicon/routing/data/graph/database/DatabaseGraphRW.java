/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.graph.database;

import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.EdgeData;
import cz.certicon.routing.data.basic.database.AbstractDatabase;
import cz.certicon.routing.data.graph.GraphReader;
import cz.certicon.routing.data.graph.GraphWriter;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.common.SimpleEdgeAttributes;
import cz.certicon.routing.model.entity.common.SimpleEdgeData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * An implementation of the {@link GraphReader}/{@link GraphWriter} interfaces
 * using the {@link AbstractDatabase} class.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class DatabaseGraphRW extends AbstractDatabase<Graph, Pair<GraphEntityFactory, DistanceFactory>> implements GraphReader, GraphWriter {

    public DatabaseGraphRW( Properties connectionProperties ) {
        super( connectionProperties );
    }

    @Override
    protected Graph checkedRead( Pair<GraphEntityFactory, DistanceFactory> in ) throws SQLException {
        Map<Node.Id, Node> nodeMap = new HashMap<>();
        GraphEntityFactory graphEntityFactory = in.a;
        DistanceFactory distanceFactory = in.b;
        Graph graph = graphEntityFactory.createGraph();
        ResultSet rs;
        rs = getStatement().executeQuery( "SELECT n.id, ST_AsText(d.geom) AS point, d.osm_id "
                + "FROM nodes_routing n "
                + "JOIN nodes_data_routing d ON n.data_id = d.id;" );
        int idColumnIdx = rs.findColumn( "id" );
        int pointColumnIdx = rs.findColumn( "point" );
        int osmIdColumnIdx = rs.findColumn( "osm_id" );
        while ( rs.next() ) {
            String content = rs.getString( pointColumnIdx );
            content = content.substring( "POINT(".length(), content.length() - ")".length() );
            String[] lonlat = content.split( " " );
            Node node = graphEntityFactory.createNode( Node.Id.createId( rs.getLong( idColumnIdx ) ),
                    Double.parseDouble( lonlat[1] ),
                    Double.parseDouble( lonlat[0] )
            );
            node.setOsmId( rs.getLong( osmIdColumnIdx ) );
            graph.addNode( node );
            nodeMap.put( node.getId(), node );
        }
        rs = getStatement().executeQuery( "SELECT e.id, e.source_id, e.target_id, d.length, d.is_paid, e.speed, d.osm_id "
                + "FROM edges_routing e "
                + "JOIN edges_data_routing d ON e.data_id = d.id;" );
        idColumnIdx = rs.findColumn( "id" );
        int sourceColumnIndex = rs.findColumn( "source_id" );
        int targetColumnIndex = rs.findColumn( "target_id" );
        int lengthColumnIndex = rs.findColumn( "length" );
        int paidColumnIndex = rs.findColumn( "is_paid" );
        int speedColumnIndex = rs.findColumn( "speed" );
        int osmIdColumnIndex = rs.findColumn( "osm_id" );
        while ( rs.next() ) {
            Node.Id sourceId = Node.Id.createId( rs.getLong( sourceColumnIndex ) );
            Node.Id targetId = Node.Id.createId( rs.getLong( targetColumnIndex ) );
            EdgeData edgeData = new SimpleEdgeData(
                    rs.getInt( speedColumnIndex ),
                    rs.getBoolean( paidColumnIndex ),
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
            graph.addEdge( edge );
        }
        return graph;
    }

    @Override
    protected void checkedWrite( Graph in ) throws SQLException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

}
