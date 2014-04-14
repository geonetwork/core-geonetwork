package org.fao.geonet.kernel.search;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;

import java.util.Arrays;
import java.util.List;

/**
 * Test Xml Search Service.
 *
 * Created by Jesse on 1/27/14.
 */
public class LuceneSearcher_FastEqualsFullLoad_OrderIntegrationTest extends AbstractLanguageSearchOrderIntegrationTest {
    @Override
    protected String[] doSearch(String lang) throws Exception {
        _serviceContext.setLanguage(lang);
        Element request = new Element("request")
                .addContent(new Element("from").setText("1"))
                .addContent(new Element("to").setText("50"))
                .addContent(new Element("abstract").setText(""+ _abstractSearchTerm))
                .addContent(new Element("sortOrder").setText("reverse"))
                .addContent(new Element("sortBy").setText("_title"));

        final ServiceConfig config = new ServiceConfig();
        _luceneSearcher.search(_serviceContext, request, config);
        final Element result = _luceneSearcher.present(_serviceContext, request, config);
        return getTitlesFromMetadataElements(_serviceContext, request, result);
    }

    static String[] getTitlesFromMetadataElements(ServiceContext _serviceContext, Element request, Element result) throws JDOMException {
        final String xpath = "*//gmd:identificationInfo/*/gmd:citation/*/gmd:title";
        final List<Namespace> theNSs = Arrays.asList(Geonet.Namespaces.GMD);
        final List<Element> nodes = (List<Element>) Xml.selectNodes(result, xpath, theNSs);

        final SettingInfo settingInfo = _serviceContext.getBean(SearchManager.class).getSettingInfo();
        final LuceneSearcher.LanguageSelection language = LuceneSearcher.determineLanguage(_serviceContext, request,settingInfo);

        String[] titles = new String[nodes.size()];
        final String langCode;
        if (language.presentationLanguage.equals("fre")) {
            langCode = "#FR";
        } else if (language.presentationLanguage.equals("eng")) {
            langCode = "#EN";
        } else {
            throw new AssertionError("Unexpected language code.  Add a new if clause for "+language);
        }
        for (int i = 0; i < titles.length; i++) {
            final String titleSelectXpath = "gmd:PT_FreeText//gmd:LocalisedCharacterString[@locale = '%s']";
            titles[i] = Xml.selectString(nodes.get(i), String.format(titleSelectXpath, langCode), theNSs);
            if (titles[i] == null || titles[i].isEmpty()) {
                final List<Text> translatedTitles = (List<Text>) Xml.selectNodes(nodes.get(i), "*//gmd:LocalisedCharacterString/text()", theNSs);
                titles[i] = translatedTitles.get(0).getText();
            }
        }
        return titles;
    }
}
