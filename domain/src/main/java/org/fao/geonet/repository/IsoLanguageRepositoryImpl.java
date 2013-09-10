package org.fao.geonet.repository;

import org.fao.geonet.domain.IsoLanguage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * The implementation for the IsoLanguageRepository custom method.
 *
 * User: Jesse
 * Date: 9/9/13
 * Time: 5:37 PM
 */
public class IsoLanguageRepositoryImpl extends LocalizedEntityRepositoryImpl<IsoLanguage, Integer> {

    @PersistenceContext
    private EntityManager _entityManager;

    /**
     * Constructor.
     */
    public IsoLanguageRepositoryImpl() {
        super(IsoLanguage.class);
    }

}
