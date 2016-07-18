/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm.preprocessing.ch;

import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.NodeState;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ProcessingData {

    public final TIntList sources = new TIntArrayList();
    public final TIntList targets = new TIntArrayList();
    public final TIntList startEdges = new TIntArrayList();
    public final TIntList endEdges = new TIntArrayList();
    public final TIntList[] incomingShortcuts;
    public final TIntList[] outgoingShortcuts;
    public final TFloatList lengths = new TFloatArrayList();
    public final TIntList tmpSources = new TIntArrayList();
    public final TIntList tmpTargets = new TIntArrayList();
    public final TIntList tmpStartEdges = new TIntArrayList();
    public final TIntList tmpEndEdges = new TIntArrayList();
    public final TIntList[] tmpIncomingShortcuts;
    public final TIntList[] tmpOutgoingShortcuts;
    public final TFloatList tmpLengths = new TFloatArrayList();
    public final TIntSet tmpNodes = new TIntHashSet();
    public final Graph graph;
    public final TIntObjectMap<List<ShortcutLocator>> edgeTrs = new TIntObjectHashMap<>(); // key = edge, value = list{a = node, b = sequence}
    public final TIntObjectMap<List<TIntList>> turnRestrictions = new TIntObjectHashMap<>();
    public final TIntObjectMap<List<ShortcutLocator>> shortcutsTrs = new TIntObjectHashMap<>(); // key = shortcut, value = list{a = node, b = sequence}
    public final TIntObjectMap<List<TIntList>> tmpTurnRestrictions = new TIntObjectHashMap<>();
    public final TIntObjectMap<List<ShortcutLocator>> tmpShortcutsTrs = new TIntObjectHashMap<>(); // key = shortcut, value = list{a = node, b = sequence}
    private int shortcutCounter = 0;
    private int tmpShortcutCounter = -1;
    private boolean temporary = false;

    public ProcessingData( Graph graph ) {
        this.graph = graph;
        incomingShortcuts = new TIntArrayList[graph.getNodeCount()];
        outgoingShortcuts = new TIntArrayList[graph.getNodeCount()];
        tmpIncomingShortcuts = new TIntArrayList[graph.getNodeCount()];
        tmpOutgoingShortcuts = new TIntArrayList[graph.getNodeCount()];
        // add only to edgeTrs
        if ( graph.getTurnRestrictions() != null ) {
            int[][][] tr = graph.getTurnRestrictions();
            for ( int i = 0; i < graph.getNodeCount(); i++ ) {
                int[][] nodeTr = tr[i];
                if ( nodeTr != null ) {
                    for ( int j = 0; j < nodeTr.length; j++ ) {
                        for ( int k = 0; k < nodeTr[j].length; k++ ) {
                            int edge = nodeTr[j][k];
                            List<ShortcutLocator> pairs = edgeTrs.get( edge );
                            if ( pairs == null ) {
                                pairs = new ArrayList<>();
                                edgeTrs.put( edge, pairs );
                            }
                            pairs.add( new ShortcutLocator( i, j ) );
                        }
                    }
                }
            }
        }
    } // add only to edgeTrs

    public void addShortcut( int startEdge, int endEdge ) {
        //            System.out.println( "Adding shortcut: #" + ( shortcutCounter + graph.getEdgeCount() ) + " = " + startEdge + " -> " + endEdge );
        int source = getSource( startEdge );
        int target = getTarget( endEdge );
        int thisId = shortcutCounter + graph.getEdgeCount();
        if ( thisId == source || thisId == target ) {
            //                throw new AssertionError( "shortcut #" + thisId + " = " + source + " -> " + target );
        }
        //            System.out.println( "#" + shortcutCounter + " - adding shortcut[edges] - " + startEdge + " -> " + endEdge );
        //            System.out.println( "shortcut[nodes] - " + source + " -> " + target );
        sources.add( source );
        //            System.out.println( "shortcut - sources = " + sources );
        targets.add( target );
        //            System.out.println( "shortcut - targets = " + targets );
        startEdges.add( startEdge );
        //            System.out.println( "shortcut - start edges = " + startEdges );
        endEdges.add( endEdge );
        //            System.out.println( "shortcut - end edges = " + endEdges );
        lengths.add( getLength( startEdge ) + getLength( endEdge ) );
        if ( incomingShortcuts[target] == null ) {
            incomingShortcuts[target] = new TIntArrayList();
        }
        incomingShortcuts[target].add( thisId );
        //            System.out.println( "shortcut - incoming[#" + target + "] = " + incomingShortcuts[target] );
        if ( outgoingShortcuts[source] == null ) {
            outgoingShortcuts[source] = new TIntArrayList();
        }
        outgoingShortcuts[source].add( thisId );
        //            System.out.println( "shortcut - outgoing[#" + source + "] = " + outgoingShortcuts[source] );
        // ADD TR if needed
        Set<ShortcutLocator> trSet = getShortcutLocators( edgeTrs, startEdge, endEdge );
        addTurnRestrictions( trSet, turnRestrictions, shortcutsTrs, startEdge, endEdge, thisId );
        trSet = getShortcutLocators( turnRestrictions, shortcutsTrs, startEdge, endEdge );
        addTurnRestrictions( trSet, turnRestrictions, turnRestrictions, shortcutsTrs, startEdge, endEdge, thisId );
        shortcutCounter++;
    }

    private Set<ShortcutLocator> getShortcutLocators( TIntObjectMap<List<TIntList>> trs, TIntObjectMap<List<ShortcutLocator>> trMap, int startEdge, int endEdge ) {
        Set<ShortcutLocator> shortcutLocators = new HashSet<>();
        int[] edges = { startEdge, endEdge };
        for ( int currentEdge : edges ) {
            if ( trMap.containsKey( currentEdge ) ) {
                // if there are turn restrictions on the startEdge, apply the restrictions to this shortcut (prepare them for addition)
                List<ShortcutLocator> pairs = trMap.get( currentEdge ); // obtain all the pairs of starting edge
                for ( ShortcutLocator pair : pairs ) {
                    TIntList sequence = trs.get( pair.getNode() ).get( pair.getSequenceIndex() ); // get sequence on the given index for the given node
                    if ( endEdge == sequence.get( 0 ) ) {
                        // if the sequence begins with the endEdge, add
                        shortcutLocators.add( pair );
                    } else if ( startEdge == sequence.get( sequence.size() - 1 ) ) {
                        // if the sequence ends with the startEdge, add
                        shortcutLocators.add( pair );
                    } else {
                        // if the sequence just contains the edge, find out whether a turn restriction contains this whole shortcut
                        for ( int i = 0; i < sequence.size(); i++ ) {
                            int edge = sequence.get( i );
                            if ( edge == startEdge ) {
                                if ( i + 1 < sequence.size() && sequence.get( i + 1 ) == endEdge ) {
                                    shortcutLocators.add( pair );
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return shortcutLocators;
    }

    private Set<ShortcutLocator> getShortcutLocators( /* turn restrictions provided by graph, */ TIntObjectMap<List<ShortcutLocator>> trMap, int startEdge, int endEdge ) {
        Set<ShortcutLocator> shortcutLocators = new HashSet<>();
        int[] edges = { startEdge, endEdge };
        for ( int currentEdge : edges ) {
            if ( trMap.containsKey( currentEdge ) ) {
                // if there are turn restrictions on the startEdge, apply the restrictions to this shortcut (prepare them for addition)
                List<ShortcutLocator> pairs = trMap.get( currentEdge ); // obtain all the pairs of starting edge
                for ( ShortcutLocator pair : pairs ) {
                    int[][][] trs = graph.getTurnRestrictions();
                    int[] sequence = trs[pair.getNode()][pair.getSequenceIndex()]; // get sequence on the given index for the given node
                    if ( endEdge == sequence[0] ) {
                        // if the sequence begins with the endEdge, add
                        shortcutLocators.add( pair );
                    } else if ( startEdge == sequence[sequence.length - 1] ) {
                        // if the sequence ends with the startEdge, add
                        shortcutLocators.add( pair );
                    } else {
                        // if the sequence just contains the edge, find out whether a turn restriction contains this whole shortcut
                        for ( int i = 0; i < sequence.length; i++ ) {
                            int edge = sequence[i];
                            if ( edge == startEdge ) {
                                if ( i + 1 < sequence.length && sequence[i + 1] == endEdge ) {
                                    shortcutLocators.add( pair );
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return shortcutLocators;
    }

    private void addTurnRestrictions( Set<ShortcutLocator> trSet, TIntObjectMap<List<TIntList>> sourceTurnRestrictions, TIntObjectMap<List<TIntList>> targetTurnRestrictions, TIntObjectMap<List<ShortcutLocator>> targetTurnRestrictionMap, int startEdge, int endEdge, int shortcutId ) {
        for ( ShortcutLocator pair : trSet ) {
            // for each relevant <node,sequence> pair add new turn-restriction
            int node = pair.getNode();
            int seqId = pair.getSequenceIndex();
            TIntList sequence = sourceTurnRestrictions.get( node ).get( seqId ); // get current sequence
            addTurnRestriction( node, sequence, targetTurnRestrictions, targetTurnRestrictionMap, startEdge, endEdge, shortcutId );
        }
    }

    private void addTurnRestrictions( Set<ShortcutLocator> trSet, /* turn restrictions provided by graph, */ TIntObjectMap<List<TIntList>> targetTurnRestrictions, TIntObjectMap<List<ShortcutLocator>> targetTurnRestrictionMap, int startEdge, int endEdge, int shortcutId ) {
        for ( ShortcutLocator pair : trSet ) {
            // for each relevant <node,sequence> pair add new turn-restriction
            int node = pair.getNode();
            int seqId = pair.getSequenceIndex();
            TIntList sequence = new TIntArrayList( graph.getTurnRestrictions()[node][seqId] ); // get current sequence
            addTurnRestriction( node, sequence, targetTurnRestrictions, targetTurnRestrictionMap, startEdge, endEdge, shortcutId );
        }
    }

    private void addTurnRestriction( int node, TIntList sequence, TIntObjectMap<List<TIntList>> targetTurnRestrictions, TIntObjectMap<List<ShortcutLocator>> targetTurnRestrictionMap, int startEdge, int endEdge, int shortcutId ) {
        //            if ( temporary ) {
        //
        //            } else {
        //                System.out.println( "Adding turn restriction for node#" + node + ", shortcutId = " + shortcutId + ", startEdge = " + startEdge + ", endEdge = " + endEdge );
        //
        //            }
//        System.out.println( "adding tr: node = " + node + ", sequence = " + sequence + ", se = " + startEdge + ", ee = " + endEdge + ", shortcut = " + shortcutId );
        TIntList seq = new TIntArrayList(); // create new sequence for the new turn-restriction
        int lastNode = -1;
        if ( endEdge == sequence.get( 0 ) ) {
            // if the sequence begins with the endEdge, add this shortcut to the beginning and then copy the rest of the turn-restriction sequence
            seq.add( shortcutId );
            for ( int i = 1; i < sequence.size(); i++ ) {
                seq.add( sequence.get( i ) );
            }
            lastNode = node;
        } else if ( startEdge == sequence.get( sequence.size() - 1 ) ) {
            // if the sequence ends with the startEdge, copy all but the last of the turn-restriction, then add this shortcut
            for ( int i = 0; i < sequence.size() - 1; i++ ) {
                seq.add( sequence.get( i ) );
            }
            seq.add( shortcutId );
            lastNode = node;
        } else {
            int lastEdge = -1;
            {
                // get the FIRST node independent of the direction // limit variables to this block // TODO what is this for???
                int e1 = sequence.get( 0 ); // first edge
                int s1 = getSource( e1 ); // its source and target
                int t1 = getTarget( e1 );
                int e2 = sequence.get( 1 ); // second edge
                int s2 = getSource( e2 ); // its source and target
                int t2 = getTarget( e2 );
                if ( t1 == s2 || t1 == t2 ) {
                    // if the target is connected to the second edge - regular direction
                    lastNode = s1;
                } else if ( s1 == s2 || s1 == t2 ) {
                    // if the source is connected to the second edge - opposite direction
                    lastNode = t1;
                }
            } // now the 'lastNode' contains the first node of the sequence
            for ( int i = 0; i < sequence.size(); i++ ) {
                // for each part of this sequence
                // add to the list until the start and edge are met - add them as one
                // save last node
                if ( lastEdge != -1 ) {
                    lastNode = getOtherNode( lastEdge, lastNode ); // get other node at the beginning - will not set after setting the last edge
                }
                if ( sequence.get( i ) == startEdge && sequence.get( i + 1 ) == endEdge ) {
                    lastEdge = shortcutId;
                    i++; // move past the target edge
                } else {
                    lastEdge = sequence.get( i );
                }
                seq.add( lastEdge );
            }
        }
//        System.out.println( "sequence built: " + seq );
        List<TIntList> trList = targetTurnRestrictions.get( lastNode ); // add new turn-restriction to trs (create new list if necessary)
        if ( trList == null ) {
            trList = new ArrayList<>();
            targetTurnRestrictions.put( lastNode, trList );
        }
        trList.add( seq );
//        System.out.println( "trs for node: " + targetTurnRestrictions.get( lastNode ) );
        List<ShortcutLocator> strs = targetTurnRestrictionMap.get( shortcutId ); // add new locator to map (create new list if necessary)
        if ( strs == null ) {
            strs = new ArrayList<>();
            targetTurnRestrictionMap.put( shortcutId, strs );
        }
        strs.add( new ShortcutLocator( lastNode, trList.size() - 1 ) );
//        System.out.println( "trmap for shortcut: " + targetTurnRestrictionMap.get( shortcutId ) );
        TIntIterator iterator = sequence.iterator(); // for each edge in the sequence add information about a new sequence related to it, skip edges contained in the shortcuts
        while ( iterator.hasNext() ) {
            int e = iterator.next();
            if ( e != startEdge && e != endEdge ) {
                strs = targetTurnRestrictionMap.get( e ); // add new locator to map (create new list if necessary)
                if ( strs == null ) {
                    strs = new ArrayList<>();
                    targetTurnRestrictionMap.put( e, strs );
                }
                strs.add( new ShortcutLocator( lastNode, trList.size() - 1 ) );
            }
        }
    }

    public void addTemporaryShortcut( int startEdge, int endEdge ) {
        //            System.out.println( "Adding temporary shortcut: #" + ( tmpShortcutCounter + graph.getEdgeCount() ) + " = " + startEdge + " -> " + endEdge );
        int source = getSource( startEdge );
        int target = getTarget( endEdge );
        int thisId = tmpShortcutCounter + shortcutCounter + graph.getEdgeCount();
        if ( thisId == source || thisId == target ) {
            //                throw new AssertionError( "shortcut #" + thisId + " = " + source + " -> " + target );
        }
        tmpSources.add( source );
        tmpTargets.add( target );
        tmpStartEdges.add( startEdge );
        tmpEndEdges.add( endEdge );
        tmpLengths.add( getLength( startEdge ) + getLength( endEdge ) );
        if ( tmpIncomingShortcuts[target] == null ) {
            tmpIncomingShortcuts[target] = new TIntArrayList();
        }
        tmpIncomingShortcuts[target].add( thisId );
        tmpNodes.add( target );
        if ( tmpOutgoingShortcuts[source] == null ) {
            tmpOutgoingShortcuts[source] = new TIntArrayList();
        }
        tmpNodes.add( source );
        tmpOutgoingShortcuts[source].add( thisId );
        // ADD TR if needed
        temporary = true;
        Set<ShortcutLocator> trSet = getShortcutLocators( edgeTrs, startEdge, endEdge );
        addTurnRestrictions( trSet, tmpTurnRestrictions, tmpShortcutsTrs, startEdge, endEdge, thisId );
        trSet = getShortcutLocators( turnRestrictions, shortcutsTrs, startEdge, endEdge );
        addTurnRestrictions( trSet, turnRestrictions, tmpTurnRestrictions, tmpShortcutsTrs, startEdge, endEdge, thisId );
        trSet = getShortcutLocators( tmpTurnRestrictions, tmpShortcutsTrs, startEdge, endEdge );
        addTurnRestrictions( trSet, tmpTurnRestrictions, tmpTurnRestrictions, tmpShortcutsTrs, startEdge, endEdge, thisId );
        temporary = false;
        tmpShortcutCounter++;
    }

    public void clearTemporaryShortcuts() {
        //            System.out.println( "clearing tmp shortcuts" );
        tmpShortcutsTrs.clear();
        tmpTurnRestrictions.clear();
        tmpShortcutCounter = 0;
        tmpSources.clear();
        tmpTargets.clear();
        tmpStartEdges.clear();
        tmpEndEdges.clear();
        TIntIterator iterator = tmpNodes.iterator();
        while ( iterator.hasNext() ) {
            int n = iterator.next();
            tmpIncomingShortcuts[n] = null;
            tmpOutgoingShortcuts[n] = null;
        }
        tmpLengths.clear();
        tmpNodes.clear();
        //            System.out.println( "cleared" );
    }

    public int size() {
        return shortcutCounter;
    }

    //        public boolean evaluableEdge( int edge ) {
    //            return edge < ( graph.getEdgeCount() + size() );
    //        }
    public TIntIterator getIncomingEdgesIterator( int node ) {
        return new IncomingIterator( graph, node );
    }

    public TIntIterator getOutgoingEdgesIterator( int node ) {
        return new OutgoingIterator( graph, node );
    }

    public long getEdgeOrigId( int edge, long startId ) {
        if ( edge < graph.getEdgeCount() ) {
            return graph.getEdgeOrigId( edge );
        }
        if ( edge >= graph.getEdgeCount() + shortcutCounter ) {
            //                throw new AssertionError( "Temporary shortcut@getEdgeOrigId: edge = " + edge + ", edge count = " + graph.getEdgeCount() + ", shortcut counter = " + shortcutCounter );
        }
        //            System.out.println( startId + " + " + edge + " - " + graph.getEdgeCount() );
        return startId + edge - graph.getEdgeCount();
    }

    public int getOtherNode( int edge, int node ) {
        int source = getSource( edge );
        if ( source != node ) {
            return source;
        }
        return getTarget( edge );
    }

    public int getSource( int edge ) {
        if ( edge < graph.getEdgeCount() ) {
            return graph.getSource( edge );
        }
        if ( edge < graph.getEdgeCount() + shortcutCounter ) {
            return sources.get( edge - graph.getEdgeCount() );
        }
        return tmpSources.get( edge - graph.getEdgeCount() - shortcutCounter );
    }

    public int getTarget( int edge ) {
        if ( edge < graph.getEdgeCount() ) {
            return graph.getTarget( edge );
        }
        if ( edge < graph.getEdgeCount() + shortcutCounter ) {
            return targets.get( edge - graph.getEdgeCount() );
        }
        return tmpTargets.get( edge - graph.getEdgeCount() - shortcutCounter );
    }

    public float getLength( int edge ) {
        if ( edge < graph.getEdgeCount() ) {
            return graph.getLength( edge );
        }
        if ( edge < graph.getEdgeCount() + shortcutCounter ) {
            return lengths.get( edge - graph.getEdgeCount() );
        }
        return tmpLengths.get( edge - graph.getEdgeCount() - shortcutCounter );
    }

    public boolean isValidWay( NodeState state, int targetEdge, Map<NodeState, NodeState> predecessorArray ) {
        return isValidWay( state, targetEdge, predecessorArray, turnRestrictions ) && isValidWay( state, targetEdge, predecessorArray, tmpTurnRestrictions ) && graph.isValidWay( state, targetEdge, predecessorArray );
    }

    private boolean isValidWay( NodeState state, int targetEdge, Map<NodeState, NodeState> predecessorArray, TIntObjectMap<List<TIntList>> trs ) {
        // what if predecessor is a shortcut... ???
        if ( trs == null ) {
            // without turn restrictions, everything is valid
            return true;
        }
        int node = state.getNode();
        if ( !trs.containsKey( node ) ) {
            // without turn restrictions for the concrete node, every turn is valid
            return true;
        }
        List<TIntList> sequences = trs.get( node );
        for ( int i = 0; i < sequences.size(); i++ ) {
            // for all restrictions for this node
            TIntList edgeSequence = sequences.get( i ); // load the edge sequence of this particular restrictions
            if ( edgeSequence.get( edgeSequence.size() - 1 ) == targetEdge ) {
                // if the last edge of this sequence is the target edge
                NodeState currState = state;
                for ( int j = edgeSequence.size() - 2; j >= 0; j-- ) {
                    // for every edge in the sequence (except for the last, it is already checked) compare it with the predecessor
                    if ( currState.getEdge() != edgeSequence.get( j ) ) {
                        break;
                    }
                    if ( j == 0 ) {
                        // all passed, the turn restriction edge sequence matches the way, therefore it is forbidden
                        return false;
                    }
                    currState = predecessorArray.get( currState );
                }
            }
        }
        return true;
    }

    private class IncomingIterator implements TIntIterator {

        private final int node;
        private final Graph graph;
        private int position = -1;

        public IncomingIterator( Graph graph, int node ) {
            //                System.out.println( "#" + node + " - IN iterator creation" );
            this.node = node;
            this.graph = graph;
        }

        @Override
        public boolean hasNext() {
            // ... see note at NeighbourListGraph
            int size = incomingShortcuts[node] != null ? incomingShortcuts[node].size() : 0;
            int tmpSize = tmpIncomingShortcuts[node] != null ? tmpIncomingShortcuts[node].size() : 0;
            return position + 1 < graph.getIncomingEdges( node ).length + size + tmpSize;
        }

        @Override
        public int next() {
            int next;
            position++;
            int size = incomingShortcuts[node] != null ? incomingShortcuts[node].size() : 0;
            if ( position < graph.getIncomingEdges( node ).length ) {
                next = graph.getIncomingEdges( node )[position];
            } else if ( position < graph.getIncomingEdges( node ).length + size ) {
                next = incomingShortcuts[node].get( position - graph.getIncomingEdges( node ).length );
            } else {
                next = tmpIncomingShortcuts[node].get( position - graph.getIncomingEdges( node ).length - size );
            }
            //                System.out.println( "#" + node + " - next = " + next );
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private class OutgoingIterator implements TIntIterator {

        private final int node;
        private int position = -1;
        private final Graph graph;

        public OutgoingIterator( Graph graph, int node ) {
            //                System.out.println( "#" + node + " - OUT iterator creation" );
            this.node = node;
            this.graph = graph;
        }

        @Override
        public boolean hasNext() {
            // see above, analogically
            boolean hasNext;
            int size = outgoingShortcuts[node] != null ? outgoingShortcuts[node].size() : 0;
            int tmpSize = tmpOutgoingShortcuts[node] != null ? tmpOutgoingShortcuts[node].size() : 0;
            hasNext = position + 1 < graph.getOutgoingEdges( node ).length + size + tmpSize;
            //                System.out.println( "hasNext=" + hasNext + ", position = " + position + ", edges = " + graph.getOutgoingEdges( node ).length + ", shortcuts = " + size + ", tmpshortcuts = " + tmpSize );
            return hasNext;
        }

        @Override
        public int next() {
            int next;
            position++;
            int size = outgoingShortcuts[node] != null ? outgoingShortcuts[node].size() : 0;
            if ( position < graph.getOutgoingEdges( node ).length ) {
                next = graph.getOutgoingEdges( node )[position];
            } else if ( position < graph.getOutgoingEdges( node ).length + size ) {
                next = outgoingShortcuts[node].get( position - graph.getOutgoingEdges( node ).length );
            } else {
                //                    System.out.println( "index = " + ( position - graph.getOutgoingEdges( node ).length - size ) );
                //                    System.out.println( "size = " + ( tmpOutgoingShortcuts[node] != null ? tmpOutgoingShortcuts[node].size() : 0 ) );
                next = tmpOutgoingShortcuts[node].get( position - graph.getOutgoingEdges( node ).length - size );
            }
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }
    }

}
