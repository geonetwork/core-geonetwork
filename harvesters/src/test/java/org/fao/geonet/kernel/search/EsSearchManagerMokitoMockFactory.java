package org.fao.geonet.kernel.search;

import org.mockito.Mockito;

public class EsSearchManagerMokitoMockFactory {
    public EsSearchManager createMock() {
        return Mockito.mock(EsSearchManager.class);
    }
}
