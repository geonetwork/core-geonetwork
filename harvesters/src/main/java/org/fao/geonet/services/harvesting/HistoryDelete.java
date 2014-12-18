package org.fao.geonet.services.harvesting;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.repository.HarvestHistoryRepository;
import org.fao.geonet.repository.specification.HarvestHistorySpecs;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HistoryDelete implements Service {
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig config) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        long nrRecs = 0;
        final HarvestHistoryRepository historyRepository = context.getBean(HarvestHistoryRepository.class);

        String uuid = Util.getParam(params, "uuid");
        if (uuid != null) {
            final Specification<HarvestHistory> hasHarvesterUuid = HarvestHistorySpecs.hasHarvesterUuid(uuid);
            nrRecs = historyRepository.count(hasHarvesterUuid);
            historyRepository.deleteAll(hasHarvesterUuid);
        } else {
            Collection<Integer> ids = Collections2.transform(params.getChildren("id"), new Function<Object, Integer>() {
                @Nullable
                @Override
                public Integer apply(@Nonnull Object input) {
                    return Integer.valueOf(((Element) input).getText());
                }
            });

            List<Element> files = params.getChildren("file");

            for (Element file : files) {
                try {
                    File f = new File(file.getTextTrim());
                    if (f.exists() && f.canWrite()) {
                        if (!f.delete()) {
                            org.fao.geonet.utils.Log.warning(Geonet.HARVESTER,
                                    "Removing history. Failed to delete file: " + f.getCanonicalPath());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!ids.isEmpty()) {
                nrRecs = historyRepository.deleteAllById(ids);
            }
        }
        return new Element(Jeeves.Elem.RESPONSE).setText(nrRecs + "");
    }
}
