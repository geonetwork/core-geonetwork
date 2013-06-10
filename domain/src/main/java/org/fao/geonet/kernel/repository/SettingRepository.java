package org.fao.geonet.kernel.repository;

import static org.fao.geonet.kernel.domain.Setting.*;

import java.util.List;

import javax.annotation.Nonnull;

import org.fao.geonet.kernel.domain.Setting;
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
    @Query(name = QUERY_FIND_ROOT.NAME)
    List<Setting> findRoots();

    List<Setting> findByName(String name);

    @Query(name = QUERY_FIND_ALL_CHILDREN.NAME)
    @Nonnull
    List<Setting> findAllChildren(@Param("parentid") int parentid);

    @Query(name = QUERY_FIND_CHILDREN_BY_NAME.NAME)
    List<Setting> findChildrenByName(@Param("parentid") int parentid, @Param("name") String name);
}
