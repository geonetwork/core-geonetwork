package org.fao.geonet.repository;

import java.util.List;

import javax.annotation.Nonnull;

import org.fao.geonet.domain.Setting;
import org.fao.geonet.domain.SettingNamedQueries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * DataAccess object for accessing settings.
 * 
 * @author Jesse
 */
public interface SettingRepository extends JpaRepository<Setting, Integer>, SettingRepositoryCustom {

    public static final String ID_PREFIX = "id:";
    String SEPARATOR = "/";

    /**
     * Get the root setting.
     */
    @Nonnull
    @Query(name = SettingNamedQueries.QUERY_FIND_ROOT.NAME)
    List<Setting> findRoots();

    List<Setting> findByName(String name);

    @Query(name = SettingNamedQueries.QUERY_FIND_ALL_CHILDREN.NAME)
    @Nonnull
    List<Setting> findAllChildren(@Param("parentid") int parentid);

    @Query(name = SettingNamedQueries.QUERY_FIND_CHILDREN_BY_NAME.NAME)
    List<Setting> findChildrenByName(@Param("parentid") int parentid, @Param("name") String name);
}
