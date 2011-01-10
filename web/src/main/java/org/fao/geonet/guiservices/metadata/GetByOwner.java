package org.fao.geonet.guiservices.metadata;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * Retrieve all metadata editable by the current user.
 *
 */
public class GetByOwner implements Service {

	private ServiceConfig _config;

	public void init(String appPath, ServiceConfig config) throws Exception {
		_config = config;
	}

	public Element exec(Element params, ServiceContext context) throws Exception {

		Element request = makeSearchRequest(params, context);
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SearchManager searchMan = gc.getSearchmanager();

        LuceneSearcher searcher = null;
        try {
            searcher = (LuceneSearcher) searchMan.newSearcher(SearchManager.LUCENE, Geonet.File.SEARCH_LUCENE);

            searcher.search(context, request, _config);

			Element presentRequest = new Element("request");
            presentRequest.addContent(new Element("fast").setText("true"));
			presentRequest.addContent(new Element("from").setText("1"));
			presentRequest.addContent(new Element("to").setText(searcher.getSize()+""));

			List<Element> results = searcher.present(context, presentRequest, _config).getChildren();
            List<Element> responseList = new ArrayList<Element>();

            for (Element element : results) {
                if(element.getName().equals("summary")) {
                    continue;
                }
                Element info = element.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);
                String id = info.getChildText(Edit.Info.Elem.ID);
                if (id != null && id != "") {
                    Element md = gc.getDataManager().getMetadata(context, id, false);
                    responseList.add(md);
                }
            }
            Element response = new Element("response");
            for (Iterator<Element> i = responseList.iterator(); i.hasNext();) {
                response.addContent(i.next());
            }
            return response;
        }
        finally {
            if (searcher != null) searcher.close();
        }
	}

	private Element makeSearchRequest(Element params, ServiceContext ctx) throws Exception {

		// create a search request
        Element request = new Element(Jeeves.Elem.REQUEST);

        // exclude harvested metadata
        request.addContent(new Element("isHarvested").setText("n"));

        // include template metadata
        request.addContent(new Element("template").setText("n"));
        request.addContent(new Element("template").setText("y"));
        request.addContent(new Element("template").setText("s"));

        // restrict to editable metadata
        request.addContent(new Element("editable").setText("true"));

        //
        // sorting
        //

		String sortBy = sortByParameter(params);

		if ("title".equals(sortBy)) {
			Element sortOrder = request.getChild("sortOrder");
			if (sortOrder == null) {
				sortOrder = new Element("sortOrder");
				request.addContent(sortOrder);
			}
			sortOrder.setText("reverse");
		}
		Element sortByElement = request.getChild("sortBy");
		if (sortByElement == null) {
			sortByElement = new Element("sortBy");
			request.addContent(sortByElement);
		}
		sortByElement.setText(sortBy);

		return request;
	}

    private String sortByParameter(Element params) {
		Element sortByEl = params.getChild(Geonet.SearchResult.SORT_BY);
		String sortBy = null;
		if (sortByEl == null) {
			sortBy = "date";
		}
        else {
			sortBy = sortByEl.getText();
		}
        return sortBy;
    }

}