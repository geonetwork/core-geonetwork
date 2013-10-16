package org.fao.geonet.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * The implementation for the IsoLanguageRepository custom method.
 * <p/>
 * User: Jesse
 * Date: 9/9/13
 * Time: 5:37 PM
 */
public class IsoLanguageRepositoryImpl {

    @PersistenceContext
    private EntityManager _entityManager;

}
