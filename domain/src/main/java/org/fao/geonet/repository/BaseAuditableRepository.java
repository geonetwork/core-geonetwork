package org.fao.geonet.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.history.RevisionRepository;

@NoRepositoryBean
public interface BaseAuditableRepository<U> extends RevisionRepository<U, Integer, Integer>, JpaRepository<U, Integer> {
}
