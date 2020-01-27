package org.fao.geonet.repository;

import org.fao.geonet.domain.AnnotationEntity;
import org.fao.geonet.domain.AnnotationEntity_;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class AnnotationRepositoryImpl implements  AnnotationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean exists(@Nonnull String uuid) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<AnnotationEntity> root = query.from(AnnotationEntity.class);
        Predicate predicate = cb.equal(root.get(AnnotationEntity_.uuid), uuid);
        return entityManager.createQuery(query.select(cb.count(root)).where(predicate)).getSingleResult() > 0;
    }

    @Override
    public AnnotationEntity findByUUID(@Nonnull String uuid) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AnnotationEntity> query = cb.createQuery(AnnotationEntity.class);
        Root<AnnotationEntity> root = query.from(AnnotationEntity.class);
        Predicate predicate = cb.equal(root.get(AnnotationEntity_.uuid), uuid);
        return entityManager.createQuery(query.where(predicate)).getSingleResult();
    }
}
