/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.ch;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface ChDataFactory<T> {
    public ChDataBuilder<T> createChDataBuilder();
    
    public ChDataExtractor<T> createChDataExtractor(T extracted);
}
