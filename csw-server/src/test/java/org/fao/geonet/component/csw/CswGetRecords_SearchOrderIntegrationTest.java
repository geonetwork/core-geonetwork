package org.fao.geonet.component.csw;

import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.kernel.search.AbstractLanguageSearchOrderIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 * Test Xml Search Service.
 * <p/>
 * Created by Jesse on 1/27/14.
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:csw-integration-test-context.xml")
public class CswGetRecords_SearchOrderIntegrationTest extends AbstractLanguageSearchOrderIntegrationTest {
    @Autowired
    private GetRecords _getRecords;

    @Override
    protected String[] doSearch(String lang) throws Exception {
        _serviceContext.setLanguage(lang);

        String[] parts = _abstractSearchTerm.split(" ");
        Element filter = new Element("And", Csw.NAMESPACE_OGC);
        for (String part : parts) {
            Element isEqualTo = new Element("PropertyIsEqualTo", Csw.NAMESPACE_OGC)
                    .addContent(new Element("PropertyName",
                            Csw.NAMESPACE_OGC).setText("abstract"))
                    .addContent(new Element("Literal",
                            Csw.NAMESPACE_OGC).setText("" + part));

            filter.addContent(isEqualTo);
        }

        if (parts.length == 1) {
            filter = (Element) filter.getChildren().get(0);
            filter.detach();
        }

        Element request = new Element("GetRecords", Csw.NAMESPACE_CSW)
                .setAttribute("service", "CSW")
                .setAttribute("version", "2.0.2")
                .setAttribute("resultType", "results")
                .setAttribute("startPosition", "1")
                .setAttribute("maxRecords", "50")
                .setAttribute("outputSchema", "csw:Record")
                .addContent(new Element("Query", Csw.NAMESPACE_CSW)
                        .addContent(new Element("ElementSetName", Csw.NAMESPACE_CSW).setText("summary"))
                        .addContent(new Element("SortBy", Csw.NAMESPACE_OGC)
                                .addContent(
                                        new Element("SortProperty", Csw.NAMESPACE_OGC)
                                                .addContent(new Element("PropertyName", Csw.NAMESPACE_OGC).setText("_title"))
                                                .addContent(new Element("SortOrder", Csw.NAMESPACE_OGC).setText("A"))
                                ))
                        .addContent(
                                new Element("Constraint", Csw.NAMESPACE_CSW)
                                        .setAttribute("version", "1.0.0")
                                        .addContent(
                                                new Element("Filter", Csw.NAMESPACE_OGC)
                                                        .addContent(filter)
                                        )
                        )
                );

        final Element result = _getRecords.execute(request, _serviceContext);

        final String xpath = "*//csw:SummaryRecord/dc:title";
        final List<Element> nodes = (List<Element>) Xml.selectNodes(result, xpath, Arrays.asList(Csw.NAMESPACE_CSW, Csw.NAMESPACE_DC));
        String[] titles = new String[nodes.size()];
        for (int i = 0; i < titles.length; i++) {
            titles[i] = nodes.get(i).getText();
        }
        return titles;
    }
}
