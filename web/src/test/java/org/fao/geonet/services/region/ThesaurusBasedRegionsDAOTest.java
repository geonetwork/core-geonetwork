package org.fao.geonet.services.region;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlCacheManager;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.sesame.config.ConfigurationException;

public class ThesaurusBasedRegionsDAOTest {

    private static IsoLanguagesMapper langMapper = new IsoLanguagesMapper() {
        {
            isoLanguagesMap639.put("de", "ger");
            isoLanguagesMap639.put("fr", "fre");
            isoLanguagesMap639.put("en", "eng");
        }
    };
    private static ThesaurusManager thesaurusManager;
    private static Thesaurus regionsThesaurus;
    private static String here;

    @BeforeClass
    public static void init() throws ConfigurationException, UnsupportedEncodingException {
        here = URLDecoder.decode(ThesaurusBasedRegionsDAOTest.class.getResource(".").getFile(), "UTF-8");
        String sep = File.separator;
        regionsThesaurus = new Thesaurus(langMapper, "regions", "external", "place", new File(here, "external" + sep + "thesauri" + sep
                + "place" + sep + "regions.rdf"), "");
        regionsThesaurus.initRepository();
        thesaurusManager = mock(ThesaurusManager.class);
        when(thesaurusManager.getThesaurusByName(anyString())).thenReturn(regionsThesaurus);
    }

    private XmlCacheManager cacheManager = new XmlCacheManager();
    
    @Test
    public void testListCategories() throws Exception {
        ThesaurusBasedRegionsDAO dao = createDAO();
        Collection<String> ids = dao.getRegionCategoryIds(dummyContext());
        assertTrue(ids.contains("http://geonetwork-opensource.org/regions#country"));
        assertTrue(ids.contains("http://geonetwork-opensource.org/regions#continent"));
        assertTrue(ids.contains("http://geonetwork-opensource.org/regions#ocean"));
    }

    @Test
    public void testCreateSearchRequest() throws Exception {
        Request request = createSearch();
        assertNotNull(request);
    }

    @Test
    public void testSearchByCategory() throws Exception {
        Request request = createSearch();
        request.categoryId("http://geonetwork-opensource.org/regions#country");
        Collection<Region> regions = request.execute();

        Region france = null;
        Region switzerland = null;
        for (Region region : regions) {
            assertEquals("region has wrong category", "http://geonetwork-opensource.org/regions#country", region.getCategoryId());
            if (region.getLabels().containsValue("France")) {
                france = region;
            } else if (region.getLabels().containsValue("Switzerland")) {
                switzerland = region;
            }
        }

        assertNotNull("Switzerland should have been returned", switzerland);
        assertEquals("http://geonetwork-opensource.org/regions#211", switzerland.getId());

        validateFrance(france);
    }

    @Test
    public void testSearchLabelEng() throws Exception {
        Request request = createSearch();
        String searchTerm = "France";
        request.label(searchTerm);
        Collection<Region> regions = request.execute();

        Region france = null;
        for (Region region : regions) {
            assertLabelContainsTerm(searchTerm, region);
            if (region.getLabels().containsValue(searchTerm)) {
                france = region;
            }
        }

        validateFrance(france);
    }

    private void assertLabelContainsTerm(String searchTerm, Region region) {
        boolean foundLabelMatch = false;
        Collection<String> labels = region.getLabels().values();
        for (String string : labels) {
            if (string.contains(searchTerm)) {
                foundLabelMatch = true;
            }
        }
        assertTrue("Couldn't find 'France' in any of the labels", foundLabelMatch);
    }

    @Test
    public void testSearchLabelGer() throws Exception {
        Request request = createSearch();
        String searchTerm = "Frankreich";
        request.label(searchTerm);
        Collection<Region> regions = request.execute();

        Region france = null;
        for (Region region : regions) {

            assertLabelContainsTerm(searchTerm, region);
            if (region.getLabels().containsValue("France")) {
                france = region;
            }
        }
        assertEquals(1, regions.size());
        validateFrance(france);
    }

    @Test
    public void testSearchLabelPartial() throws Exception {
        Request request = createSearch();
        String searchTerm = "Frankr";
        request.label(searchTerm);
        Collection<Region> regions = request.execute();

        Region france = null;
        for (Region region : regions) {
            assertLabelContainsTerm(searchTerm, region);
            if (region.getLabels().containsValue("France")) {
                france = region;
            }
        }

        validateFrance(france);
    }
    
    @Test
    public void testLimitSearch() throws Exception {
        Request request = createSearch();
        request.maxRecords(2);
        Collection<Region> regions = request.execute();
        assertEquals(2, regions.size());
    }
    
    @Test
    public void testSearchById() throws Exception {
        Region france = createSearch().id("http://geonetwork-opensource.org/regions#68").get();
        validateFrance(france);
    }

    private void validateFrance(Region france) {
        assertNotNull("France should have been returned", france);
        assertEquals("France", france.getLabels().get("fre"));
        assertEquals("Frankreich", france.getLabels().get("ger"));
        assertEquals("Pays", france.getCategoryLabels().get("fre"));
        assertEquals("Country", france.getCategoryLabels().get("eng"));
        assertEquals("http://geonetwork-opensource.org/regions#country", france.getCategoryId());
        assertNotNull(france.getBBox());
        assertEquals("http://geonetwork-opensource.org/regions#68", france.getId());
        assertTrue(CRS.equalsIgnoreMetadata(DefaultGeographicCRS.WGS84, france.getBBox().getCoordinateReferenceSystem()));
    }

    private Request createSearch() throws Exception {
        ThesaurusBasedRegionsDAO dao = createDAO();
        Request request = dao.createSearchRequest(dummyContext());
        return request;
    }

    private ThesaurusBasedRegionsDAO createDAO() {
        Set<String> locales = new HashSet<String>();
        locales.add("eng");
        locales.add("fre");
        locales.add("ger");

        ThesaurusBasedRegionsDAO dao = new ThesaurusBasedRegionsDAO(locales);
        return dao;
    }

    private ServiceContext dummyContext() {
        ServiceContext context = mock(ServiceContext.class);

        when(context.getAppPath()).thenReturn(here);

        GeonetContext gnContext = mock(GeonetContext.class);
        when(gnContext.getThesaurusManager()).thenReturn(thesaurusManager);
        when(context.getHandlerContext(Geonet.CONTEXT_NAME)).thenReturn(gnContext);
        when(context.getXmlCacheManager()).thenReturn(cacheManager);
        return context;
    }

}
