package org.fao.geonet.kernel;

import org.mockito.Mockito;

public class LocalInvokerMockFactory {
    public SpringLocalServiceInvoker createMock() {
        return Mockito.mock(SpringLocalServiceInvoker.class);
    }
}
