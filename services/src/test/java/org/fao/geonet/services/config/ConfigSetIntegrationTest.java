package org.fao.geonet.services.config;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.Assert.assertEquals;

/**
 * Test updating geonetwork settings via the Config Set service.
 *
 * User: Jesse
 * Date: 10/17/13
 * Time: 7:55 AM
 */
public class ConfigSetIntegrationTest extends AbstractServiceIntegrationTest {
    @Autowired
    SettingManager _settingManager;
    @Test
    public void testExecBatch() throws Exception {

        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        Element params = new Element("config");
        final Element onlyEl = new Element("system.requestedLanguage.only").setText("prefer_locale");
        params.addContent(onlyEl);
        final Element sortedEl = new Element("system.requestedLanguage.sorted").setText("true");
        params.addContent(sortedEl);
        final Element maxthreadsEl = new Element("system.threadedindexing.maxthreads").setText("123");
        params.addContent(maxthreadsEl);

        new Set().exec(params, serviceContext);

        assertSetting(onlyEl.getName(), onlyEl.getText());
        assertSetting(sortedEl.getName(), sortedEl.getText());
        assertSetting(maxthreadsEl.getName(), maxthreadsEl.getText());
    }

    private void assertSetting(String setting, String expected) {
        final String newValue = _settingManager.getValue(setting.replace('.', '/'));
        assertEquals(expected, newValue);
    }

    @Test
    public void testExecSingle() throws Exception {

        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        assertSet(serviceContext, "system.requestedLanguage.only", "prefer_locale");
        assertSet(serviceContext, "system.requestedLanguage.only", "prefer_docLocale");
        assertSet(serviceContext, "system.requestedLanguage.only", "only_docLocale");
        assertSet(serviceContext, "system.requestedLanguage.only", "only_locale");
        assertSet(serviceContext, "system.requestedLanguage.only", "off");
    }

    private void assertSet(ServiceContext serviceContext, String setting, String value) throws Exception {
        Element params = new Element("config").addContent(new Element(setting).setText(value));
        new Set().exec(params, serviceContext);

        final String newValue = _settingManager.getValue(setting.replace('.','/'));

        assertEquals(value, newValue);
    }
}
