package org.fao.geonet.geocat.xlink;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.xlink.ISO19139KeywordReplacer;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.junit.Assert.*;

/**
 * Created by fgravin on 10/26/17.
 */
public class ISO19139KeywordReplacerTest extends AbstractCoreIntegrationTest {

    private static final String KEYWORD_RESOURCE = "kernel/vicinityKeyword.xml";
    private static final String MD_MULTILINGUAL = "kernel/multilingual-metadata.xml";

    @Autowired
    private ThesaurusManager thesaurusManager;
    @Autowired
    public IsoLanguagesMapper isoLanguagesMapper;

    private ISO19139KeywordReplacer replacer;

    private Element getSubtemplateXml(String path) throws IOException, JDOMException {
        URL contactResource = AbstractCoreIntegrationTest.class.getResource(path);
        Element subtemplateElement = Xml.loadStream(contactResource.openStream());
        return subtemplateElement;
    }

    @Before
    public final void setup_() throws Exception {
        this.replacer = new ISO19139KeywordReplacer();
//        replacer.thesaurusManager = thesaurusManager;
//        replacer.isoLanguagesMapper = isoLanguagesMapper;
    }


    @Test
    public void list() throws Exception {
        Element keywordElt = getSubtemplateXml(KEYWORD_RESOURCE);
        Element root = new Element("descriptiveKeywords", GMD);
        root.addContent(keywordElt);

        List<Pair<Element, String>> allKeywords = this.replacer.getAllKeywords(root);
        assertEquals(3, allKeywords.size());
    }

    @Test
    public void replaceAll() throws IOException, JDOMException {
        Element md = getSubtemplateXml(MD_MULTILINGUAL);
        this.replacer.replaceAll(md);
    }

}