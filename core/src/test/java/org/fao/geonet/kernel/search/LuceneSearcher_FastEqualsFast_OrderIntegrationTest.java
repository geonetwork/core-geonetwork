package org.fao.geonet.kernel.search;

import jeeves.server.ServiceConfig;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.fao.geonet.kernel.search.LuceneSearcher_FastEqualsFullLoad_OrderIntegrationTest.getTitlesFromMetadataElements;

/**
 * Test Xml Search Service.
 *
 * Created by Jesse on 1/27/14.
 */
public class LuceneSearcher_FastEqualsFast_OrderIntegrationTest extends AbstractLanguageSearchOrderIntegrationTest {

    @Autowired
    private MetadataRepository _metadataRepository;

    @Override
    protected String[] doSearch(String lang) throws Exception {
        _serviceContext.setLanguage(lang);
        Element request = new Element("request")
                .addContent(new Element(Geonet.SearchResult.FAST).setText("true"))
                .addContent(new Element("from").setText("1"))
                .addContent(new Element("to").setText("50"))
                .addContent(new Element("abstract").setText(""+ _abstractSearchTerm))
                .addContent(new Element("sortOrder").setText("reverse"))
                .addContent(new Element("sortBy").setText("_title"));
        final ServiceConfig config = new ServiceConfig();
        _luceneSearcher.search(_serviceContext, request, config);
        final Element result = _luceneSearcher.present(_serviceContext, request, config);
        final List<Element> nodes = (List<Element>) Xml.selectNodes(result, "metadata/geonet:info/id", Arrays.asList(Edit.NAMESPACE));
        String[] titles = new String[nodes.size()];
        for (int i = 0; i < titles.length; i++) {
            final String mdId = nodes.get(i).getText();
            final Metadata md = _metadataRepository.findOne(mdId);
            final Element xmlData = md.getXmlData(false);
            titles[i] = getTitlesFromMetadataElements(_serviceContext, request, new Element("record").addContent(xmlData))[0];
        }
        return titles;
    }
}
