package org.fao.geonet.kernel;

import jeeves.xlink.Processor;
import org.fao.geonet.AbstractCoreIntegrationTest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

public abstract class AbstractIntegrationTestWithMockedSingletons extends AbstractCoreIntegrationTest {

    private static SpringLocalServiceInvoker mockInvoker;

    public SpringLocalServiceInvoker resetAndGetMockInvoker() {
        if (mockInvoker == null) {
            mockInvoker = mock(SpringLocalServiceInvoker.class);
            _applicationContext.getBeanFactory().registerSingleton(SpringLocalServiceInvoker.class.getCanonicalName(), mockInvoker);
            Processor.setLocalServiceInvoker(mockInvoker);
        }
        reset(mockInvoker);
        return mockInvoker;
    }
}
