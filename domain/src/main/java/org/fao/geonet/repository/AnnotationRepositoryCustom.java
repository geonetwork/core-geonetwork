package org.fao.geonet.repository;

import org.fao.geonet.domain.AnnotationEntity;

import javax.annotation.Nonnull;

public interface AnnotationRepositoryCustom {

    boolean exists(@Nonnull String uuid);

    AnnotationEntity findByUUID(@Nonnull String uuid);
}
