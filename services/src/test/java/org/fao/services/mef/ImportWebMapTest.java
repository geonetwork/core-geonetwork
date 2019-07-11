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

package org.fao.services.mef;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerFactory;

import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.services.mef.ImportWebMap;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.jdom.Document;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


public class ImportWebMapTest {

    private static String testWmcString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ViewContext xmlns=\"http://www.opengis.net/context\" version=\"1.1.0\" id=\"e77dfc89\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/context http://schemas.opengis.net/context/1.1.0/context.xsd\"><General><Window width=\"1373\" height=\"810\"/><BoundingBox minx=\"-1363723.31702789990\" miny=\"4981331.59563689958\" maxx=\"1994613.95770959998\" maxy=\"6962579.36878869962\" SRS=\"EPSG:3857\"/><Title>aaa</Title><Abstract>ddd</Abstract><Extension><ol:maxExtent xmlns:ol=\"http://openlayers.org/context\" minx=\"-20037508.3399999999\" miny=\"-20037508.3399999999\" maxx=\"20037508.3399999999\" maxy=\"20037508.3399999999\"/></Extension></General><LayerList><Layer queryable=\"0\" hidden=\"0\"><Server service=\"OGC:WMS\" version=\"1.1.1\"><OnlineResource xlink:type=\"simple\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"http://osm.geobretagne.fr/service/wms\"/></Server><Name>osm:google</Name><Title>OpenStreetMap</Title><MetadataURL><OnlineResource xlink:type=\"simple\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"http://wiki.openstreetmap.org/wiki/FR:OpenStreetMap_License\"/></MetadataURL><sld:MinScaleDenominator xmlns:sld=\"http://www.opengis.net/sld\">266.5911979812228</sld:MinScaleDenominator><sld:MaxScaleDenominator xmlns:sld=\"http://www.opengis.net/sld\">559082264.0287180</sld:MaxScaleDenominator><FormatList><Format current=\"1\">image/png</Format></FormatList><StyleList><Style><Name/><Title>Default</Title></Style></StyleList><Extension><ol:maxExtent xmlns:ol=\"http://openlayers.org/context\" minx=\"-20037508.3399999999\" miny=\"-20037508.3399999999\" maxx=\"20037508.3399999999\" maxy=\"20037508.3399999999\"/><ol:tileSize xmlns:ol=\"http://openlayers.org/context\" width=\"256\" height=\"256\"/><ol:numZoomLevels xmlns:ol=\"http://openlayers.org/context\">22</ol:numZoomLevels><ol:units xmlns:ol=\"http://openlayers.org/context\">m</ol:units><ol:isBaseLayer xmlns:ol=\"http://openlayers.org/context\">false</ol:isBaseLayer><ol:displayInLayerSwitcher xmlns:ol=\"http://openlayers.org/context\">true</ol:displayInLayerSwitcher><ol:singleTile xmlns:ol=\"http://openlayers.org/context\">false</ol:singleTile><ol:transitionEffect xmlns:ol=\"http://openlayers.org/context\">resize</ol:transitionEffect><ol:attribution xmlns:ol=\"http://openlayers.org/context\"><Title>GéoBretagne / OSM</Title><OnlineResource xlink:type=\"simple\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"http://www.openstreetmap.org/\"/><LogoURL width=\"100\" height=\"100\" format=\"image/png\"><OnlineResource xlink:type=\"simple\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"http://upload.wikimedia.org/wikipedia/commons/thumb/b/b0/Openstreetmap_logo.svg/100px-Openstreetmap_logo.svg.png\"/></LogoURL></ol:attribution></Extension></Layer></LayerList></ViewContext>";
    private static String testWmcUrl = "http://sdi.georchestra.org/mapfishapp/ws/wmc/geodoc939c9df8121e7953b23a39c22f5b2bdb.wmc";
    private static String testWmcViewerUrl = "http://sdi.georchestra.org/mapfishapp/?wmc=ws/wmc/geodoc939c9df8121e7953b23a39c22f5b2bdb.wmc";
    private ImportWebMap importWmcService;
    private ServiceConfig serviceConfig = Mockito.mock(ServiceConfig.class);
    private ServiceContext serviceContext = Mockito.mock(ServiceContext.class);
    private UserSession userSession = Mockito.mock(UserSession.class);
    private GeonetContext geonetContext = Mockito.mock(GeonetContext.class);
    private DataManager dataManager = Mockito.mock(DataManager.class);
    private IMetadataManager metadataManager = Mockito.mock(IMetadataManager.class);
    private SettingManager sm = Mockito.mock(SettingManager.class);

    @Before
    public void setUp() throws Exception {
        String webDirectory = this.getClass().getResource("/").getPath() + "../../../web/src/main/webapp";
        assumeTrue(new File(webDirectory).exists());

        importWmcService = new ImportWebMap();
        importWmcService.init(new File(webDirectory).toPath(), serviceConfig);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testImportWmc() throws Exception {

        //Mocking a User session
        Mockito.when(serviceContext.getUserSession()).thenReturn(userSession);
        Mockito.when(userSession.getEmailAddr()).thenReturn("pierre.mauduit@example.com");
        Mockito.when(userSession.getSurname()).thenReturn("Pierre");
        Mockito.when(userSession.getName()).thenReturn("Mauduit");
        Mockito.when(userSession.getOrganisation()).thenReturn("Camptocamp France SAS");
        //Mockito.when(userSession.getPhone()).thenReturn("+33.4.56.78.90.12");

        Mockito.when(serviceContext.getHandlerContext(Mockito.anyString())).thenReturn(geonetContext);
        Mockito.when(serviceContext.getBean(SettingManager.class)).thenReturn(sm);
        Mockito.when(geonetContext.getBean(SettingManager.class)).thenReturn(sm);
        Mockito.when(geonetContext.getBean(DataManager.class)).thenReturn(dataManager);
        Mockito.when(geonetContext.getBean(IMetadataManager.class)).thenReturn(metadataManager);
        Mockito.when(sm.getSiteId()).thenReturn("1234");
        Mockito.when(sm.getSiteName()).thenReturn("geonetwork-testor");

        // The stylesheet should exist in the filesystem
        Field styleSheet = ImportWebMap.class.getDeclaredField("styleSheetWmc");
        styleSheet.setAccessible(true);
        String xslPath = (String) styleSheet.get(importWmcService);
        assertTrue(new File(xslPath).exists());

        Document doc = new Document();
        Element reqElem = new Element("request");
        reqElem.addContent(new Element("map_string").setText(testWmcString));
        reqElem.addContent(new Element("map_url").setText(testWmcUrl));
        reqElem.addContent(new Element("viewer_url").setText(testWmcViewerUrl));
        doc.addContent(reqElem);

        // Eclipse embedded JUnit will select an incompatible transformer factory
        // We force it to use net.sf.saxon
        TransformerFactoryFactory.setTransformerFactory(TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null));

        Element ret = importWmcService.serviceSpecificExec(reqElem, serviceContext);
        assertTrue(Xml.getString(ret).contains("uuid"));
    }

    @Test
    public final void testReproject() {
        // BoundingBox minx=\"-1363723.31702789990\" miny=\"4981331.59563689958\" maxx=\"1994613.95770959998\" maxy=\"6962579.36878869962\" SRS=\"EPSG:3857\"
        String minx = "-1363723.31702789990", miny = "4981331.59563689958",
            maxx = "1994613.95770959998", maxy = "6962579.36878869962",
            SRS = "EPSG:3857";

        String returnedBbox = XslUtil.reprojectCoords(minx, miny, maxx, maxy, SRS);

        // Never a good idea to parse XML using regex, but for a unit test, I shall allow myself.
        Matcher m = Pattern.compile("<gco:Decimal xmlns:gco=\"http://www.isotc211.org/2005/gco\">(.*)</gco:Decimal>").matcher(returnedBbox);

        List<String> reprL = new ArrayList<String>();

        while (m.find()) {
            reprL.add(m.group(1));
        }

        assertTrue(reprL.size() == 4);
        boolean forceXY = Boolean.parseBoolean(System.getProperty("org.geotools.referencing.forceXY", "false"));
        if (!forceXY) {
            assertTrue(new Double(reprL.get(2)) > 40 && new Double(reprL.get(2)) < 41
                && new Double(reprL.get(0)) > -13 && new Double(reprL.get(0)) < -12
                && new Double(reprL.get(3)) > 52 && new Double(reprL.get(3)) < 53
                && new Double(reprL.get(1)) > 17 && new Double(reprL.get(1)) < 18
            );
        } else {
            assertTrue(new Double(reprL.get(0)) > 40 && new Double(reprL.get(0)) < 41
                && new Double(reprL.get(2)) > -13 && new Double(reprL.get(2)) < -12
                && new Double(reprL.get(1)) > 52 && new Double(reprL.get(1)) < 53
                && new Double(reprL.get(3)) > 17 && new Double(reprL.get(3)) < 18
            );
        }
    }
}
