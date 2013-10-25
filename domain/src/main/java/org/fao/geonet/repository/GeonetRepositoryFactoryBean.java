package org.fao.geonet.repository;

import org.fao.geonet.domain.HarvesterSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import javax.persistence.EntityManager;
import java.io.Serializable;

import static org.springframework.data.querydsl.QueryDslUtils.QUERY_DSL_PRESENT;

/**
 * The factory that allows all Geonetwork repositories to automatically get the functionality from GeonetRepository
 * if they extend that interface.
 *
 * @param <R> The repository type
 * @param <T> The entity type the repository returns
 * @param <I> the id type of the entity
 */
public class GeonetRepositoryFactoryBean<R extends JpaRepository<T, I>, T, I extends Serializable>
        extends JpaRepositoryFactoryBean<R, T, I> {

    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {

        return new GeonetRepositoryFactory(entityManager);
    }

    private static class GeonetRepositoryFactory<T, I extends Serializable> extends JpaRepositoryFactory {

        private EntityManager entityManager;

        public GeonetRepositoryFactory(EntityManager entityManager) {
            super(entityManager);

            this.entityManager = entityManager;
        }

        protected Object getTargetRepository(RepositoryMetadata metadata) {

            Class<?> repositoryInterface = metadata.getRepositoryInterface();

            if (isQueryDslExecutor(repositoryInterface)) {
                return super.getTargetRepository(metadata);
            } else if (metadata.getDomainType().equals(HarvesterSetting.class)) {
                return new HarvesterSettingRepositoryOverridesImpl((Class<HarvesterSetting>) metadata.getDomainType(), entityManager);
            } else {
                return new GeonetRepositoryImpl((Class<T>) metadata.getDomainType(), entityManager);
            }
        }

        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {

            if (isQueryDslExecutor(metadata.getRepositoryInterface())) {
                return super.getRepositoryBaseClass(metadata);
            } else if (metadata.getDomainType().equals(HarvesterSetting.class)) {
                return HarvesterSettingRepositoryOverridesImpl.class;
            } else {
                return GeonetRepositoryImpl.class;
            }
        }

        /**
         * Returns whether the given repository interface requires a QueryDsl specific implementation to be chosen.
         *
         * @param repositoryInterface
         * @return
         */
        private boolean isQueryDslExecutor(Class<?> repositoryInterface) {

            return QUERY_DSL_PRESENT && QueryDslPredicateExecutor.class.isAssignableFrom(repositoryInterface);
        }

    }
}