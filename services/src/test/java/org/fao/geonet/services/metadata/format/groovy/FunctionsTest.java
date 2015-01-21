package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import groovy.util.slurpersupport.GPathResult;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlCacheManager;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.Constants;
import org.fao.geonet.domain.IsoLanguage;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.services.metadata.format.AbstractFormatterTest;
import org.fao.geonet.services.metadata.format.ConfigFile;
import org.fao.geonet.services.metadata.format.Format;
import org.fao.geonet.services.metadata.format.FormatterParams;
import org.fao.geonet.utils.IO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FunctionsTest {
    private final String schema = "iso19139";
    private FormatterParams fparams;
    private Functions functions;
    private Path schemaDir;

    @Before
    public void setUp() throws Exception {

        final IsoLanguageRepository repository = Mockito.mock(IsoLanguageRepository.class);
        Mockito.when(repository.findAllByCode("ger")).thenReturn(Arrays.asList(isoLang("German")));
        Mockito.when(repository.findAllByCode("eng")).thenReturn(Arrays.asList(isoLang("English")));
        Mockito.when(repository.findAllByShortCode("en")).thenReturn(Arrays.asList(isoLang("English")));
        Mockito.when(repository.findAllByShortCode("de")).thenReturn(Arrays.asList(isoLang("German")));

        this.schemaDir = IO.toPath(FunctionsTest.class.getResource("translation-test/schema-dir/formatter/config.properties").toURI())
                .getParent().getParent();
        SchemaManager schemaManager = Mockito.mock(SchemaManager.class);
        Mockito.when(schemaManager.getSchemaDir("parent-schema")).thenReturn(schemaDir.getParent().resolve("parent-schema"));

        final ConfigurableApplicationContext appContext = Mockito.mock(ConfigurableApplicationContext.class);
        Mockito.when(appContext.getBean(SchemaManager.class)).thenReturn(schemaManager);
        Mockito.when(appContext.getBean(IsoLanguageRepository.class)).thenReturn(repository);
        GeonetworkDataDirectory dataDir = Mockito.mock(GeonetworkDataDirectory.class);
        Mockito.when(dataDir.getWebappDir()).thenReturn(AbstractCoreIntegrationTest.getWebappDir(FunctionsTest.class));
        Mockito.when(dataDir.getSystemDataDir()).thenReturn(AbstractCoreIntegrationTest.getWebappDir(FunctionsTest.class));
        Mockito.when(appContext.getBean(GeonetworkDataDirectory.class)).thenReturn(dataDir);
        Mockito.when(appContext.getBean(XmlCacheManager.class)).thenReturn(new XmlCacheManager());

        fparams = new FormatterParams() {
            @Override
            public boolean isDevMode() {
                return false;
            }
        };
        fparams.schema = schema;
        fparams.context = new ServiceContext("test", appContext, Maps.<String, Object>newHashMap(), null);
        fparams.context.setAsThreadLocal();
        fparams.context.setLanguage("eng");
        fparams.config = new ConfigFile(IO.toPath("."), false, IO.toPath(".").getParent());

        fparams.schema = "schema";
        fparams.schemaDir = schemaDir;
        fparams.formatDir = schemaDir.getParent().resolve("formatter-dir");

        fparams.format = new Format() {
            @Override
            protected boolean isDevMode(ServiceContext context) {
                return false;
            }
        };

        Environment env = Mockito.mock(Environment.class);
        Mockito.when(env.getLang3()).thenReturn("eng");
        Mockito.when(env.getLang2()).thenReturn("#EN");

        functions = new Functions(fparams, env){
            @Override
            protected ConfigFile getConfigFile(SchemaManager schemaManager, String schema) throws IOException {
                return Mockito.mock(ConfigFile.class);
            }
        };
    }

    private static IsoLanguage isoLang(String engTranslation) {
        final IsoLanguage isoLanguage = new IsoLanguage();
        isoLanguage.getLabelTranslations().put("eng", engTranslation);
        return isoLanguage;
    }


    @Test
    public void testTranslate() throws Exception {

        assertEquals("FormatterStrings", functions.translate("formatter-strings"));
        final String inBoth = functions.translate("in-both");
        assertTrue(inBoth.equals("In Both strings") || inBoth.equals("In Both other-strings"));
        assertEquals("In Both other-strings", functions.translate("in-both", "other-strings"));
        assertEquals("In Both strings", functions.translate("in-both", "strings"));
        assertEquals("Schema Normal Strings XML", functions.translate("schema-strings"));
        assertEquals("Parent Schema Normal Strings XML", functions.translate("parent-schema-strings"));
        assertEquals("Parent Schema More Strings XML", functions.translate("parent-schema-more"));
        final String inBothParent = functions.translate("in-both-parent");
        assertTrue(inBothParent.equals("In Both Parent Strings XML") || inBothParent.equals("In Both Parent More Strings XML"));
        assertEquals("In Both Parent Strings XML", functions.translate("in-both-parent", "strings"));
        assertEquals("In Both Parent More Strings XML", functions.translate("in-both-parent", "more-strings"));
        assertEquals("This String is here for testing don't delete", functions.translate("testString"));
    }

    @Test
    public void testTranslateNoLocDir() throws Exception {
        fparams.formatDir = schemaDir.getParent().resolve("doesnotexist");

        // no exception? good
    }

    @Test
    public void testGetXPath() throws Exception {
        final URL resource = AbstractCoreIntegrationTest.class.getResource("kernel/valid-metadata.iso19139.xml");
        final String sampleMetadataXml = Resources.toString(resource, Constants.CHARSET);
        final GPathResult xml = AbstractFormatterTest.parseXml(sampleMetadataXml);

        GPathResult polygon = null, bbox = null;
        final Iterator iterator = xml.depthFirst();
        while (iterator.hasNext()) {
            Object elem = iterator.next();
            if (elem instanceof GPathResult) {
                GPathResult result = (GPathResult) elem;
                if (result.name().equals("gmd:EX_BoundingPolygon")) {
                    polygon = result;
                }
                if (result.name().equals("gmd:EX_GeographicBoundingBox")) {
                    bbox = result;
                }
            }
        }

        assertEquals("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[2]/gmd:EX_Extent/gmd:geographicElement[2]/gmd:EX_GeographicBoundingBox", functions.getXPathFrom(bbox));
        assertEquals("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[2]/gmd:EX_Extent/gmd:geographicElement[1]/gmd:EX_BoundingPolygon", functions.getXPathFrom(polygon));

    }
}
