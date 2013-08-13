package org.fao.geonet.repository;

import java.util.List;

import javax.annotation.Nonnull;

import org.fao.geonet.domain.AbstractSetting;

public interface AbstractSettingRepoCustom<T extends AbstractSetting> {
    List<T> findByPath(String pathToSetting);
    T findOneByPath(String pathToSetting);
    @Nonnull
    List<T> findRoots();
    @Nonnull
    List<T> findAllChildren(int parentid);
    List<T> findChildrenByName(int parentid, String name);
}