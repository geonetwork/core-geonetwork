package org.fao.geonet;


import org.fao.geonet.harvester.wfsfeatures.CamelWorkers;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class CamelWorkersTest {

    @Test
    public void startWhenEnabled() throws Exception {
        Geonetwork toTest = new Geonetwork();

        CamelWorkers workers = mock(CamelWorkers.class);
        SettingManager setting = mock(SettingManager.class);
        when(setting.getValueAsBool(Settings.SYSTEM_CAMELWORKERS_ENABLED)).thenReturn(true);
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(any(Class.class))).thenReturn(workers);

        toTest.startCamelWorkersIfEnabled(context, setting);

        verify(workers).start();
    }

    @Test
    public void notStartWhenNotEnabled() throws Exception {
        Geonetwork toTest = new Geonetwork();

        CamelWorkers workers = mock(CamelWorkers.class);
        SettingManager setting = mock(SettingManager.class);
        when(setting.getValueAsBool(Settings.SYSTEM_CAMELWORKERS_ENABLED)).thenReturn(false);
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(any(Class.class))).thenReturn(workers);

        toTest.startCamelWorkersIfEnabled(context, setting);

        verify(workers, never()).start();
    }

    @Test
    public void stopForwrded() throws Exception {
        Geonetwork toTest = new Geonetwork();

        CamelWorkers workers = mock(CamelWorkers.class);
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(any(Class.class))).thenReturn(workers);

        toTest.stopCamelWorkers(context);

        verify(workers).stop();
    }
}
