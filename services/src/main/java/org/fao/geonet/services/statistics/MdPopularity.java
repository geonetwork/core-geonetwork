package org.fao.geonet.services.statistics;


import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo_;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Jeeves service to select the Metadata popularity from database. made as a java service
 * to allow passing limit parameter to the UI part
 *
 * @author nicolas ribot
 */
public class MdPopularity extends NotInReadOnlyModeService {
    /**
     * the max number of results to display
     */
    private int limit = 25;


    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------
    public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
        this.limit = Integer.parseInt(params.getValue("limit"));
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    /**
     * Physically dumps the given table, writing it to the App tmp folder,
     * returning the URL of the file to get.
     */
    @Override
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        final Sort sort = new Sort(Sort.Direction.DESC, SortUtils.createPath(Metadata_.dataInfo, MetadataDataInfo_.popularity));
        final Page<Metadata> all = context.getBean(MetadataRepository.class).findAll(new PageRequest(0, limit, sort));
        Element response = new Element("mdPopularity");
        Element elLimit = new Element("limit").setText("" + limit);
        response.addContent(elLimit);

        for (Metadata metadata : all) {
            elLimit.addContent(new Element("record")
                    .addContent(new Element("id").setText("" + metadata.getId()))
                    .addContent(new Element("uuid").setText(metadata.getUuid()))
                    .addContent(new Element("popularity").setText("" + metadata.getDataInfo().getPopularity()))
            );
        }
        return response;
    }
}
