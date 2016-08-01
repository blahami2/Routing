/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.ch;

/**
 * Interface defining factory for CH data builder and extractor
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <T> CH data type
 */
public interface ChDataFactory<T> {

    /**
     * Returns new CH data builder
     *
     * @return new CH data builder
     */
    public ChDataBuilder<T> createChDataBuilder();

    /**
     * Returns new CH data extractor for the given data
     *
     * @param extracted given data
     * @return new CH data extractor
     */
    public ChDataExtractor<T> createChDataExtractor( T extracted );
}
