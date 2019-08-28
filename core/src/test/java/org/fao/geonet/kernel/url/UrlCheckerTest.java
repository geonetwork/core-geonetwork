package org.fao.geonet.kernel.url;

import org.apache.http.client.methods.HttpUriRequest;
import org.fao.geonet.domain.LinkStatus;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.AbstractClientHttpResponse;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UrlCheckerTest {

    @Test
    public void status451() throws IOException {
        UrlChecker toTest = new UrlChecker();
        GeonetHttpRequestFactory mockResquetFactory = mock(GeonetHttpRequestFactory.class);
        ClientHttpResponse response = createUnknowStatus();
        when(mockResquetFactory.execute(any(HttpUriRequest.class), anyObject())).thenReturn(response);
        toTest.requestFactory = mockResquetFactory;

        LinkStatus linkStatus = toTest.getUrlStatus("dummy");

        assertEquals((451) + "", linkStatus.getStatusValue());
        assertEquals(true, linkStatus.isFailing().booleanValue());
        assertEquals("dummy 451", linkStatus.getStatusInfo());
    }

    private AbstractClientHttpResponse createUnknowStatus() {
        return new AbstractClientHttpResponse() {
            @Override
            public int getRawStatusCode() throws IOException {
                return 451;
            }

            @Override
            public String getStatusText() throws IOException {
                return "dummy 451";
            }

            @Override
            public void close() {

            }

            @Override
            public InputStream getBody() throws IOException {
                return null;
            }

            @Override
            public HttpHeaders getHeaders() {
                return null;
            }
        };
    }
}
