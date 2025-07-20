package org.fao.geonet.kernel.harvest;

import org.mockito.Mockito;

public class EsSearchManagerMockFactory {
    public org.fao.geonet.kernel.search.EsSearchManager createMock() {
        return Mockito.mock(org.fao.geonet.kernel.search.EsSearchManager.class);
    }
}
