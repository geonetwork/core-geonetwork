package org.fao.geonet.kernel.search;

import jeeves.server.ServiceConfig;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.util.List;

/**
 * Test Xml Search Service.
 *
 * Created by Jesse on 1/27/14.
 */
public class LuceneSearcher_FastEqualsIndex_OrderIntegrationTest extends AbstractLanguageSearchOrderIntegrationTest {
    @Override
    protected String[] doSearch(String lang) throws Exception {
        _serviceContext.setLanguage(lang);
        Element request = new Element("request")
                .addContent(new Element("fast").setText(Geonet.SearchResult.INDEX))
                .addContent(new Element("from").setText("1"))
                .addContent(new Element("to").setText("50"))
                .addContent(new Element("abstract").setText(""+ _abstractSearchTerm))
                .addContent(new Element("sortOrder").setText("reverse"))
                .addContent(new Element("sortBy").setText("_title"));
        final ServiceConfig config = new ServiceConfig();
        _luceneSearcher.search(_serviceContext, request, config);
        final Element result = _luceneSearcher.present(_serviceContext, request, config);
        final List<Element> nodes = (List<Element>) Xml.selectNodes(result, "metadata/title");
        String[] titles = new String[nodes.size()];
        for (int i = 0; i < titles.length; i++) {
            titles[i] = nodes.get(i).getText();
        }
        return titles;
    }
}
