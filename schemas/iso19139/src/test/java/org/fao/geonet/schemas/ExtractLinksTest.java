package org.fao.geonet.schemas;

import org.fao.geonet.kernel.schema.LinkPatternStreamer.ILinkBuilder;
import org.fao.geonet.kernel.schema.LinkPatternStreamer.RawLinkPatternStreamer;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ExtractLinksTest extends XslProcessTest {

    public ExtractLinksTest() {
        super();
        this.setXmlFilename("schemas/xsl/process/input_with_url.xml");
        this.setNs(ISO19139SchemaPlugin.allNamespaces);
    }

    class TestLink  {
        public String url;

        public TestLink setUrl(String url) {
            this.url = url;
            return this;
        }
    }

    @Test
    public void urlEncounteredProcessingAMetadata() throws Exception {
        Element mdToprocess = Xml.loadFile(xmlFile);
        String ref = "metadata_reference";
        Map<String, String> persisted = new HashMap();
        RawLinkPatternStreamer<TestLink, String> toTest = new RawLinkPatternStreamer(new ILinkBuilder<TestLink, String>() {

            @Override
            public TestLink found(String url) {
                TestLink link = new TestLink();
                return link.setUrl(url);
            }

            @Override
            public void persist(TestLink link, String ref) {
                persisted.put(link.url, ref);
            }
        }, null);
        toTest.setNamespaces(ISO19139SchemaPlugin.allNamespaces.asList());
        toTest.setRawTextXPath(".//gco:CharacterString");

        toTest.processAllRawText(mdToprocess, ref);

        assertEquals(ref, persisted.get("HTTPS://acme.de/"));
        assertEquals(ref, persisted.get("ftp://mon-site.mondomaine/mon-repertoire"));
        assertEquals(ref, persisted.get("https://metawal.wallonie.be/geonetwork/srv/api/records/93e91c8f-d53d-4e36-a993-290070bf2c8c/attachments/Orthophotos_2019_Maillage_Tuilage_Rapport_conformit%C3%A9.pdf"));
        assertEquals(ref, persisted.get("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail_s.gif"));
        assertEquals(ref, persisted.get("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail.gif"));
    }

    @Test
    public void urlEncounteredProcessingAMetadataWithExcludePattern() throws Exception {
        Element mdToprocess = Xml.loadFile(xmlFile);
        String ref = "metadata_reference";
        Map<String, String> persisted = new HashMap();
        RawLinkPatternStreamer<TestLink, String> toTest = new RawLinkPatternStreamer(new ILinkBuilder<TestLink, String>() {

            @Override
            public TestLink found(String url) {
                TestLink link = new TestLink();
                return link.setUrl(url);
            }

            @Override
            public void persist(TestLink link, String ref) {
                persisted.put(link.url, ref);
            }
        }, "(https://metawal.wallonie.be/geonetwork/srv/api/records.*|http://apps.titellus.*)");
        toTest.setNamespaces(ISO19139SchemaPlugin.allNamespaces.asList());
        toTest.setRawTextXPath(".//gco:CharacterString");

        toTest.processAllRawText(mdToprocess, ref);

        assertEquals(ref, persisted.get("HTTPS://acme.de/"));
        assertEquals(ref, persisted.get("ftp://mon-site.mondomaine/mon-repertoire"));
        assertEquals(null, persisted.get("https://metawal.wallonie.be/geonetwork/srv/api/records/93e91c8f-d53d-4e36-a993-290070bf2c8c/attachments/Orthophotos_2019_Maillage_Tuilage_Rapport_conformit%C3%A9.pdf"));
        assertEquals(null, persisted.get("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail_s.gif"));
        assertEquals(null, persisted.get("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail.gif"));
    }
}






