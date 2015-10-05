package org.fao.geonet.services.openwis.product;


import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.responses.OkResponse;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;;
import org.fao.geonet.services.openwis.DataListResponse;
import org.jdom.Element;
import org.openwis.metadata.product.ProductMetadataManager;
import org.openwis.products.client.ProductMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for ProductMetadata.
 *
 * @author Jose García
 */
@Controller("openwis.product")
public class ProductMetadataController {

    @Autowired
    private SearchManager searchMan;

    @Autowired
    private ProductMetadataManager manager;

    @RequestMapping(value = { "/{lang}/openwis.productmetadata.search" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody
    DataListResponse list(@PathVariable String lang,
                  @RequestParam Long start,
                  @RequestParam(required = false) Long maxRecords,
                  HttpServletRequest request) {

        // Data-tables start param starts at 0
        start = start + 1;

        DataListResponse<ProductMetadataDTO> response = new DataListResponse<>();

        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        ServiceManager serviceManager = appContext.getBean(ServiceManager.class);

        ServiceContext context = serviceManager.createServiceContext("openwis.productmetadata.search", lang, request);

        try {

            if (maxRecords == null) maxRecords = new Long(20);

            // Create search request
            Element requestEl = new Element("request");

            requestEl.addContent(new Element("from").setText(start + ""));
            requestEl.addContent(new Element("to").setText((start + maxRecords) + ""));
            requestEl.addContent(new Element(Geonet.SearchResult.RESULT_TYPE).setText(Geonet.SearchResult.ResultType.RESULTS));
            requestEl.addContent(new Element(Geonet.SearchResult.FAST).setText("index"));
            requestEl.addContent(new Element(Geonet.SearchResult.BUILD_SUMMARY).setText("true"));

            MetaSearcher searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);
            ServiceConfig config = new ServiceConfig();

            searcher.search(context, requestEl, config);
            Element resultsEl = searcher.present(context, requestEl, config);

            Element summary = (Element) resultsEl.getChildren().get(0);

            int total = Integer.parseInt(summary.getAttributeValue("count"));

            // Process results
            boolean ignore = true;
            for(Element resultEl : (List<Element>) resultsEl.getChildren()) {
                if (ignore) {
                    ignore = false;
                    continue;
                }
                response.addData(new ProductMetadataDTO(resultEl));
            }

            response.setRecordsTotal(new Long(total));
            response.setRecordsFiltered(new Long(total));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return response;
    }


    @RequestMapping(value = { "/{lang}/openwis.productmetadata.get" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody
    ProductMetadata retrieve(@PathVariable String lang,
                             @RequestParam String urn) {


        ProductMetadata pm = manager.getProductMetadataByUrn(urn);
        if (pm == null) throw new ProductMetadataNotFoundException();

        return pm;
    }

    @RequestMapping(value = { "/{lang}/openwis.productmetadata.set" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody
    OkResponse save(@PathVariable String lang,
                    @RequestParam String urn,
                    @RequestParam String overridenGtsCategory,
                    @RequestParam String overridenFncPattern,
                    @RequestParam Integer overridenPriority,
                    @RequestParam String overridenFileExtension) {


        ProductMetadata pm = manager.getProductMetadataByUrn(urn);

        if (pm != null) {
            pm.setOverridenGtsCategory(overridenGtsCategory);
            pm.setOverridenFncPattern(overridenFncPattern);
            pm.setOverridenPriority(overridenPriority);
            pm.setOverridenFileExtension(overridenFileExtension);

            manager.saveOrUpdate(pm);
        } else {
            throw new ProductMetadataNotFoundException();
        }

        return new OkResponse();
    }


}
