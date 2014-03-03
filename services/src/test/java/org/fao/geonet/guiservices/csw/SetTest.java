package org.fao.geonet.guiservices.csw;

import static org.junit.Assert.*;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.repository.CswCapabilitiesInfo;
import org.fao.geonet.repository.CswCapabilitiesInfoFieldRepository;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.fao.geonet.domain.Pair.read;

/**
 * Test Csw Config Set
 * User: Jesse
 * Date: 11/7/13
 * Time: 8:24 AM
 */
public class SetTest extends AbstractServiceIntegrationTest {

    @Autowired
    CswCapabilitiesInfoFieldRepository _infoRepository;
    @Autowired
    SettingRepository _settingsRepository;
    @Test
    public void testExec() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        assertEquals("true", _settingsRepository.findOne("system/csw/enable").getValue());
        assertEquals(null, _settingsRepository.findOne("system/csw/contactId").getValue());

        final Element params = createParams(read("csw.enable", "off"),
                read("csw.contactId", "2"),
                read("csw.title_eng", "en ti"),
                read("csw.abstract_eng", "en ab"),
                read("csw.fees_eng", "en fee"),
                read("csw.accessConstraints_eng", "en acc"),
                read("csw.title_fre", "fr ti"),
                read("csw.abstract_fre", "fr ab"),
                read("csw.fees_fre", "fr fee"),
                read("csw.accessConstraints_fre", "fr acc"),
                read("csw.title_ger", "ge ti"),
                read("csw.abstract_ger", "ge ab"),
                read("csw.fees_ger", "ge fee"),
                read("csw.accessConstraints_ger", "ge acc"));

        final Element results = new Set().exec(params, context);

        assertEquals("ok", results.getText());

        assertEquals("false", _settingsRepository.findOne("system/csw/enable").getValue());
        assertEquals("2", _settingsRepository.findOne("system/csw/contactId").getValue());

        final CswCapabilitiesInfo eng = _infoRepository.findCswCapabilitiesInfo("eng");
        final CswCapabilitiesInfo fre = _infoRepository.findCswCapabilitiesInfo("fre");
        final CswCapabilitiesInfo ger = _infoRepository.findCswCapabilitiesInfo("ger");
        final CswCapabilitiesInfo ita = _infoRepository.findCswCapabilitiesInfo("ita");

        assertEquals("en ti", eng.getTitle());
        assertEquals("fr ti", fre.getTitle());
        assertEquals("ge ti", ger.getTitle());
        assertEquals("", ita.getTitle());

        assertEquals("en ab", eng.getAbstract());
        assertEquals("fr ab", fre.getAbstract());
        assertEquals("ge ab", ger.getAbstract());

        assertEquals("en fee", eng.getFees());
        assertEquals("fr fee", fre.getFees());
        assertEquals("ge fee", ger.getFees());

        assertEquals("en acc", eng.getAccessConstraints());
        assertEquals("fr acc", fre.getAccessConstraints());
        assertEquals("ge acc", ger.getAccessConstraints());
    }
}
