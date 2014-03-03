package org.fao.geonet.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Implement custom methods for StatusValueRepository that can't be implemented by the query dsl of spring-data.
 * <p/>
 * User: Jesse
 * Date: 9/10/13
 * Time: 7:13 AM
 */
public class StatusValueRepositoryImpl {

    @PersistenceContext
    private EntityManager _EntityManager;

}
