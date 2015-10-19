package org.fao.geonet.services.openwis.product;


import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.responses.OkResponse;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
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
                          @RequestParam(value = "iDisplayStart") Long start,
                          @RequestParam(value = "iDisplayLength", required = false) Long maxRecords,
                          HttpServletRequest request) {

        // Data-tables start param starts at 0
        start = start + 1;

        DataListResponse<ProductMetadataDTO> response = new DataListResponse<>();

        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        ServiceManager serviceManager = appContext.getBean(ServiceManager.class);

        ServiceContext context = serviceManager.createServiceContext("openwis.productmetadata.search", lang, request);

        try {
            if (maxRecords == null) maxRecords = new Long(20);

            Element searchRequestEl = createSearchRequest(request, start, maxRecords);

            MetaSearcher searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);
            ServiceConfig config = new ServiceConfig();

            searcher.search(context, searchRequestEl, config);
            Element resultsEl = searcher.present(context, searchRequestEl, config);

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
    ProductMetadata retrieve(@RequestParam String urn) {


        ProductMetadata pm = manager.getProductMetadataByUrn(urn);
        if (pm == null) throw new ProductMetadataNotFoundException();

        return pm;
    }

    @RequestMapping(value = { "/{lang}/openwis.productmetadata.set" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody
    OkResponse save(@PathVariable String lang,
                    @RequestParam String urn,
                    @RequestParam(required = false) String overridenGtsCategory,
                    @RequestParam(required = false) String overridenDataPolicy,
                    @RequestParam(required = false) String overridenFncPattern,
                    @RequestParam(required = false) Integer overridenPriority,
                    @RequestParam(required = false) String overridenFileExtension) {


        ProductMetadata pm = manager.getProductMetadataByUrn(urn);

        if (pm != null) {
            pm.setOverridenGtsCategory(overridenGtsCategory);
            pm.setOverridenDataPolicy(overridenDataPolicy);
            pm.setOverridenFncPattern(overridenFncPattern);
            pm.setOverridenPriority(overridenPriority);
            pm.setOverridenFileExtension(overridenFileExtension);

            manager.saveOrUpdate(pm);
        } else {
            throw new ProductMetadataNotFoundException();
        }

        return new OkResponse();
    }


    /**
     * Builds the GeoNetwork search request element.
     *
     * @param request
     * @param start
     * @param maxRecords
     * @return
     */
    private Element createSearchRequest(HttpServletRequest request, Long start, Long maxRecords) {
        // Create search request
        Element requestEl = new Element("request");

        String[] columns = request.getParameter("sColumns").split(",");

        // Get sort info
        String orderColumnIdx = request.getParameter("iSortCol_0");
        String orderColumnName = columns[Integer.valueOf(orderColumnIdx)];
        String orderDir = request.getParameter("sSortDir_0");
        if (orderDir.equalsIgnoreCase("desc")) orderDir = "";


        // Get search fields
        int numColumns = Integer.parseInt(request.getParameter("iColumns"));
        for (int i = 0; i < numColumns; i++) {
            String sortColumnValue =   request.getParameter("sSearch_" + i);
            if (StringUtils.isNotEmpty(sortColumnValue)) {
                // The counter in sSearch_X terms seem 1 more than the column index in the column array
                String sortColumnName = columns[i-1];
                // Special management for title field : _title is used for sorting, but title is used for searching
                if (sortColumnName.equalsIgnoreCase("_title")) sortColumnName = "title";
                requestEl.addContent(new Element(sortColumnName).setText(sortColumnValue + "*"));
            }
        }


        String searchText = request.getParameter("sSearch");
        if (StringUtils.isNotEmpty(searchText)) {
            // Search in the any field (metadata full text) and the specific fields for category and product metadata
            requestEl.addContent(new Element("any_OR__cat_OR__process_OR__gtsCategory_OR__fncPattern_OR" +
                    "__fileExtension_OR__dataPolicy_OR__localDataResource").setText(searchText + "*"));
        }

        requestEl.addContent(new Element("from").setText(start + ""));
        requestEl.addContent(new Element("to").setText((start + maxRecords) + ""));
        if (StringUtils.isNotEmpty(orderColumnName)) {
            requestEl.addContent(new Element("sortBy").setText(orderColumnName));
            requestEl.addContent(new Element("sortOrder").setText(orderDir));
        }

        requestEl.addContent(new Element(Geonet.IndexFieldNames.IS_TEMPLATE).setText("n"));

        requestEl.addContent(new Element(Geonet.SearchResult.RESULT_TYPE).setText(Geonet.SearchResult.ResultType.RESULTS));
        requestEl.addContent(new Element(Geonet.SearchResult.FAST).setText("index"));
        requestEl.addContent(new Element(Geonet.SearchResult.BUILD_SUMMARY).setText("true"));

        return requestEl;
    }

}
