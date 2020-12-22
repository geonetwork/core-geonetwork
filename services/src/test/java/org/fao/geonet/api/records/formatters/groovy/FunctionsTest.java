/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.records.formatters.groovy;

import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import groovy.util.slurpersupport.GPathResult;

import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.api.records.formatters.AbstractFormatterTest;
import org.fao.geonet.api.records.formatters.ConfigFile;
import org.fao.geonet.api.records.formatters.FormatterApi;
import org.fao.geonet.api.records.formatters.FormatterParams;
import org.fao.geonet.api.records.formatters.GroovyFormatter;
import org.fao.geonet.api.records.formatters.XsltFormatter;
import org.fao.geonet.api.records.formatters.cache.CacheConfig;
import org.fao.geonet.api.records.formatters.cache.FormatterCache;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.IsoLanguage;
import org.fao.geonet.domain.Language;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.SpringLocalServiceInvoker;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.IO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.servlet.ServletContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockingDetails;

@ContextConfiguration(loader= AnnotationConfigContextLoader.class, classes = {FunctionsTest.ContextConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class FunctionsTest {

    static Path schemaDir;

    static class ContextConfiguration {

        public ContextConfiguration() throws URISyntaxException {
            schemaDir = IO.toPath(FunctionsTest.class.getResource("translation-test/schema-dir/formatter/config.properties").toURI()).getParent().getParent();
        }

        @Bean MockBeanFactory mockBeanFactory() {
            return new MockBeanFactory();
        }

        private static class MockBeanFactory extends InstantiationAwareBeanPostProcessorAdapter {
            @Override
            public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
                return !mockingDetails(bean).isMock();
            }
        }

        @Bean
        public SchemaManager schemaManager() {
            SchemaManager schemaManager = Mockito.mock(SchemaManager.class);
            Mockito.when(schemaManager.getSchemaDir("parent-schema")).thenReturn(schemaDir.getParent().resolve("parent-schema"));
            return schemaManager;
        }

        @Bean
        public IsoLanguageRepository isoLanguageRepository() {
            IsoLanguageRepository repository = Mockito.mock(IsoLanguageRepository.class);
            Mockito.when(repository.findAllByCode("ger")).thenReturn(Arrays.asList(isoLang("German")));
            Mockito.when(repository.findAllByCode("eng")).thenReturn(Arrays.asList(isoLang("English")));
            Mockito.when(repository.findAllByShortCode("en")).thenReturn(Arrays.asList(isoLang("English")));
            Mockito.when(repository.findAllByShortCode("de")).thenReturn(Arrays.asList(isoLang("German")));
            return repository;
        }

        @Bean
        public GeonetworkDataDirectory dataDir() {
            GeonetworkDataDirectory dataDir = Mockito.mock(GeonetworkDataDirectory.class);
            Mockito.when(dataDir.getWebappDir()).thenReturn(AbstractCoreIntegrationTest.getWebappDir(FunctionsTest.class));
            Mockito.when(dataDir.getSystemDataDir()).thenReturn(AbstractCoreIntegrationTest.getWebappDir(FunctionsTest.class));
            return dataDir;
        }

        @Bean
        public XmlCacheManager xmlCacheManager() {
            return new XmlCacheManager();
        }

        @Bean
        public IsoLanguagesMapper isoLanguagesMapper() {
            IsoLanguagesMapper languagesMapper = Mockito.mock(IsoLanguagesMapper.class);
            Mockito.when(languagesMapper.iso639_1_to_iso639_2("en")).thenReturn("eng");
            Mockito.when(languagesMapper.iso639_1_to_iso639_2("en", "en")).thenReturn("eng");
            return languagesMapper;
        }

        @Bean
        public FormatterApi formatterApi() {
            return new FormatterApi() {
                @Override
                protected boolean isDevMode() {
                    return false;
                }
            };
        }

        @Bean
        public LanguageUtils languageUtils() {
            return  Mockito.mock(LanguageUtils.class);
        }

        @Bean
        public FormatterCache formatterCache() {
            return  Mockito.mock(FormatterCache.class);
        }

        @Bean
        public IMetadataUtils metadataUtils() {
            return  Mockito.mock(IMetadataUtils.class);
        }

        @Bean
        public CacheConfig cacheConfig() {
            return  Mockito.mock(CacheConfig.class);
        }

        @Bean
        public MetadataRepository metadataRepository() {
            return  Mockito.mock(MetadataRepository.class);
        }

        @Bean
        public AccessManager accessManager() {
            return  Mockito.mock(AccessManager.class);
        }

        @Bean
        public SearchManager searchManager() {
            return  Mockito.mock(SearchManager.class);
        }

        @Bean
        public GeonetHttpRequestFactory geonetHttpRequestFactory() {
            return  Mockito.mock(GeonetHttpRequestFactory.class);
        }

        @Bean
        public SettingManager settingManager() {
            SettingManager mock = Mockito.mock(SettingManager.class);
            Mockito.doNothing().when(mock).init();
            return mock;
        }

        @Bean
        public ServiceManager serviceManager() {
            return  Mockito.mock(ServiceManager.class);
        }

        @Bean
        public XsltFormatter xsltFormatter() {
            return  Mockito.mock(XsltFormatter.class);
        }

        @Bean
        public GroovyFormatter groovyFormatter() {
            return  Mockito.mock(GroovyFormatter.class);
        }

        @Bean
        public XmlSerializer xmlSerializer() {
            return  Mockito.mock(XmlSerializer.class);
        }

        @Bean
        public OperationAllowedRepository operationAllowedRepository() {
            return  Mockito.mock(OperationAllowedRepository.class);
        }

        @Bean
        public SystemInfo systemInfo() {
            return  Mockito.mock(SystemInfo.class);
        }

        @Bean
        public ServletContext servletContext() {
            return  Mockito.mock(ServletContext.class);
        }
    }

    @Autowired
    protected ConfigurableApplicationContext _applicationContext;

    @Autowired
    protected FormatterApi formatterApi;

    private final String schema = "iso19139";
    private FormatterParams fparams;
    private Functions functions;


    private static IsoLanguage isoLang(String engTranslation) {
        final IsoLanguage isoLanguage = new IsoLanguage();
        isoLanguage.getLabelTranslations().put("eng", engTranslation);
        return isoLanguage;
    }

    @Before
    public void setUp() throws Exception {

        fparams = new FormatterParams() {
            @Override
            public boolean isDevMode() {
                return false;
            }
        };
        fparams.schema = schema;
        fparams.context = new ServiceContext("test", _applicationContext, Maps.<String, Object>newHashMap(), null);
        fparams.context.setAsThreadLocal();
        fparams.context.setLanguage("eng");
        fparams.config = new ConfigFile(IO.toPath("."), false, IO.toPath(".").getParent());

        fparams.schema = "schema";
        fparams.schemaDir = schemaDir;
        fparams.formatDir = schemaDir.getParent().resolve("formatter-dir");

        fparams.format = formatterApi;

        Environment env = Mockito.mock(Environment.class);
        Mockito.when(env.getLang3()).thenReturn("eng");
        Mockito.when(env.getLang2()).thenReturn("#EN");

        functions = new Functions(fparams, env) {
            @Override
            protected ConfigFile getConfigFile(SchemaManager schemaManager, String schema) throws IOException {
                return Mockito.mock(ConfigFile.class);
            }
        };
    }

    @Test
    public void testTranslate() throws Exception {

        assertEquals("FormatterStrings", functions.translate("formatter-strings"));
        final String inBoth = functions.translate("in-both");
        assertTrue(inBoth, inBoth.equals("In Both strings") || inBoth.equals("In Both other-strings"));
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
