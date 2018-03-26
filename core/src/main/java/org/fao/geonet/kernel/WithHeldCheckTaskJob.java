package org.fao.geonet.kernel;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * Check if any records have empty with held elements
 */
public class WithHeldCheckTaskJob {

    private static final String QUERY =
        "SELECT uuid, schemaid, changedate " +
            "FROM metadata " +
            "WHERE data LIKE '%<gmd:linkage gco:nilReason=\"withheld\" /%'";

    private int numberOfRecordsAffected = 0;

    @PersistenceContext
    private EntityManager _entityManager;

    public void executeInternal() {
        Query query = _entityManager.createNativeQuery(QUERY);
        List<Object[]> resultList = query.getResultList();
        if (resultList.size() != numberOfRecordsAffected  &&
            resultList.size() > 0) {
            Log.error(Geonet.DATA_MANAGER,
                String.format("WithHeldCheckTaskJob / %d records affected (last check was %d). List of affected records: ", resultList.size(), numberOfRecordsAffected));
            resultList.forEach(e -> {
                Log.error(Geonet.DATA_MANAGER,
                    String.format(" * %s (schema: %s), last update on %s", e[0], e[1], e[2]));
            });
            numberOfRecordsAffected = resultList.size();
        }
    }
}
