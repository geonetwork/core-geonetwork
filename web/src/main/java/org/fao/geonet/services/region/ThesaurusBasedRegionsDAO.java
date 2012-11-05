package org.fao.geonet.services.region;

import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;

public class ThesaurusBasedRegionsDAO implements RegionsDAO {

    private static final String REGIONS_THESAURUS_NAME = "external.place.regions";
    private final Set<String> localesToLoad;

    public ThesaurusBasedRegionsDAO(java.util.Set<String> localesToLoad) {
        this.localesToLoad = Collections.unmodifiableSet(localesToLoad);
    }
    
    @Override
    public Request createSearchRequest(ServiceContext context) throws Exception {
        Thesaurus thesaurus = getThesaurus(context);
        
        return new ThesaurusRequest(localesToLoad, thesaurus);
    }

    private Thesaurus getThesaurus(ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        ThesaurusManager th = gc.getThesaurusManager();
        Thesaurus regions = th.getThesaurusByName(REGIONS_THESAURUS_NAME);
        if(regions != null) {
            return regions;
        }
        Set<Entry<String, Thesaurus>> all = th.getThesauriMap().entrySet();
        for (Entry<String, Thesaurus> entry : all) {
            if(entry.getKey().contains("regions")) {
                return entry.getValue();
            }
        }
        
        return null;
    }

}
