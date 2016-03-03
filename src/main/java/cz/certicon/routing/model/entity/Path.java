/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import cz.certicon.routing.application.algorithm.Distance;

/**
 * The root interface for representation of path (as a sequence of edges)
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface Path extends Iterable<Edge> {

    /**
     * Adds edge into the path (does not guarantee order)
     *
     * @param edge an instance of {@link Edge} to be added
     * @return this instance
     */
    public Path addEdge( Edge edge );

    /**
     * Adds edge to the beginning of this path
     *
     * @param edge an instance of {@link Edge} to be added
     * @return this instance
     */
    public Path addEdgeAsFirst( Edge edge );

    /**
     * Adds edge to the end of this path
     *
     * @param edge an instance of {@link Edge} to be added
     * @return this instance
     */
    public Path addEdgeAsLast( Edge edge );

    /**
     * Getter for the first node in this path (source)
     *
     * @return an instance of {@link Node}
     */
    public Node getSourceNode();

    /**
     * Getter for the last node in this path (target)
     *
     * @return an instance of {@link Node}
     */
    public Node getTargetNode();

    /**
     * Getter for the graph which is this path based on
     *
     * @return an instance of {@link Graph}
     */
    public Graph getGraph();

    /**
     * Getter for the abstract length of this path
     *
     * @return an instance of {@link Distance}
     */
    public Distance getDistance();

    /**
     * Getter for the overall length of this path
     *
     * @return double length in meters
     */
    public double getLength();
    
    /**
     * Getter for the overall time of this path (estimated)
     * @return double time in seconds
     */
    public double getTime();

    /**
     * Connects other path to the end of this path
     *
     * @param otherPath an instance of {@link Path} to be connected
     * @return this instance
     */
    public Path connectWith( Path otherPath );

    /**
     * Retrieves an aggregation of string representations from all the edges in
     * this path
     *
     * @return an instance of {@link String} representing connected edge labels
     */
    public String toStringLabelPath();

    /**
     * Retrieves number of edges in this path.
     *
     * @return integer amount of edges
     */
    public int size();

    /**
     * Creates a {@link Spliterator} over the elements in this collection.
     *
     * Implementations should document characteristic values reported by the
     * spliterator. Such characteristic values are not required to be reported
     * if the spliterator reports {@link Spliterator#SIZED} and this collection
     * contains no elements.
     *
     * <p>
     * The default implementation should be overridden by subclasses that can
     * return a more efficient spliterator. In order to preserve expected
     * laziness behavior for the {@link #stream()} and
     * {@link #parallelStream()}} methods, spliterators should either have the
     * characteristic of {@code IMMUTABLE} or {@code CONCURRENT}, or be
     * <em><a href="Spliterator.html#binding">late-binding</a></em>. If none of
     * these is practical, the overriding class should describe the
     * spliterator's documented policy of binding and structural interference,
     * and should override the {@link #stream()} and {@link #parallelStream()}
     * methods to create streams using a {@code Supplier} of the spliterator, as
     * in:
     * <pre>{@code
     *     Stream<E> s = StreamSupport.stream(() -> spliterator(), spliteratorCharacteristics)
     * }</pre>
     * <p>
     * These requirements ensure that streams produced by the {@link #stream()}
     * and {@link #parallelStream()} methods will reflect the contents of the
     * collection as of initiation of the terminal stream operation.
     *
     * @implSpec The default implementation creates a
     * <em><a href="Spliterator.html#binding">late-binding</a></em> spliterator
     * from the collections's {@code Iterator}. The spliterator inherits the
     * <em>fail-fast</em> properties of the collection's iterator.
     * <p>
     * The created {@code Spliterator} reports {@link Spliterator#SIZED}.
     *
     * @implNote The created {@code Spliterator} additionally reports
     * {@link Spliterator#SUBSIZED}.
     *
     * <p>
     * If a spliterator covers no elements then the reporting of additional
     * characteristic values, beyond that of {@code SIZED} and {@code SUBSIZED},
     * does not aid clients to control, specialize or simplify computation.
     * However, this does enable shared use of an immutable and empty
     * spliterator instance (see {@link Spliterators#emptySpliterator()}) for
     * empty collections, and enables clients to determine if such a spliterator
     * covers no elements.
     *
     * @return a {@code Spliterator} over the elements in this collection
     * @since 1.8
     */
//    @Override
//    public Spliterator<Edge> spliterator();
    /**
     * Returns a sequential {@code Stream} with this collection as its source.
     *
     * <p>
     * This method should be overridden when the {@link #spliterator()} method
     * cannot return a spliterator that is {@code IMMUTABLE},
     * {@code CONCURRENT}, or <em>late-binding</em>. (See {@link #spliterator()}
     * for details.)
     *
     * @implSpec The default implementation creates a sequential {@code Stream}
     * from the collection's {@code Spliterator}.
     *
     * @return a sequential {@code Stream} over the elements in this collection
     * @since 1.8
     */
//    default Stream<Edge> stream() {
//        return StreamSupport.stream( spliterator(), false );
//    }
}
