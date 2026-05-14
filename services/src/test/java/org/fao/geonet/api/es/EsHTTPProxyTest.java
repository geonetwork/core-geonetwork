package org.fao.geonet.api.es;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.es.processors.query.EsQueryProcessor;
import org.fao.geonet.api.es.processors.response.EsResponseProcessor;
import org.fao.geonet.api.records.model.related.RelatedItemType;
import org.fao.geonet.index.es.EsRestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EsHTTPProxyTest {

    private static class TestableEsHTTPProxy extends EsHTTPProxy {
        private final HttpURLConnection mockConnection;

        public TestableEsHTTPProxy(HttpURLConnection mockConnection) {
            this.mockConnection = mockConnection;
        }

        @Override
        protected HttpURLConnection openConnection(String sUrl) throws IOException {
            return mockConnection;
        }
    }

    @Mock
    private EsRestClient esRestClient;

    @Mock
    private EsResponseProcessor responseProcessor;

    @Mock
    private EsQueryProcessor queryPreprocessor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private ServiceContext serviceContext;

    @Mock
    private HttpURLConnection mockConnection;

    private TestableEsHTTPProxy esHTTPProxy;

    private static final String DEFAULT_INDEX = "gn-records";
    private static final String SERVER_URL = "http://localhost:9200";

    @Before
    public void setUp() {
        esHTTPProxy = new TestableEsHTTPProxy(mockConnection);
        ReflectionTestUtils.setField(esHTTPProxy, "defaultIndex", DEFAULT_INDEX);
        ReflectionTestUtils.setField(esHTTPProxy, "proxyHeadersAllowedList", new String[]{"content-type"});
        ReflectionTestUtils.setField(esHTTPProxy, "client", esRestClient);
        ReflectionTestUtils.setField(esHTTPProxy, "responseProcessor", responseProcessor);
        ReflectionTestUtils.setField(esHTTPProxy, "queryPreprocessor", queryPreprocessor);

        lenient().when(esRestClient.getServerUrl()).thenReturn(SERVER_URL);
    }

    @Test
    public void testSearchSuccessful() throws Exception {
        String body = "{\"query\":{\"match_all\":{}}}";
        String processedBody = "{\"query\":{\"match_all\":{}}, \"filter\":{}}";

        try (MockedStatic<ApiUtils> apiUtils = mockStatic(ApiUtils.class)) {
            apiUtils.when(() -> ApiUtils.createServiceContext(any())).thenReturn(serviceContext);
            when(queryPreprocessor.process(eq(serviceContext), eq(body), anyString())).thenReturn(processedBody);
            when(request.getMethod()).thenReturn("POST");
            when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.emptyList()));

            // Mock URL connection
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            when(mockConnection.getOutputStream()).thenReturn(outputStream);
            when(mockConnection.getResponseCode()).thenReturn(200);
            when(mockConnection.getContentType()).thenReturn("application/json");
            when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream("{}".getBytes()));
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("Content-Type", Collections.singletonList("application/json"));
            when(mockConnection.getHeaderFields()).thenReturn(headers);

            // Mock ServletOutputStream
            final ByteArrayOutputStream responseOut = new ByteArrayOutputStream();
            ServletOutputStream servletOutputStream = new ServletOutputStream() {
                @Override
                public boolean isReady() { return true; }
                @Override
                public void setWriteListener(WriteListener writeListener) {}
                @Override
                public void write(int b) throws IOException { responseOut.write(b); }
            };
            when(response.getOutputStream()).thenReturn(servletOutputStream);

            esHTTPProxy.search("bucket", new RelatedItemType[0], session, request, response, body);

            // Verify
            verify(queryPreprocessor).process(eq(serviceContext), eq(body), eq("bucket"));
            verify(mockConnection).connect();
            verify(responseProcessor).processResponse(eq(serviceContext), eq(session), any(), any(), eq("_search"), eq("bucket"), eq(true), any());
            assertEquals(processedBody, outputStream.toString("UTF-8"));
        }
    }

    @Test
    public void testSearchError() throws Exception {
        String body = "{\"query\":{\"match_all\":{}}}";
        String processedBody = body;

        try (MockedStatic<ApiUtils> apiUtils = mockStatic(ApiUtils.class)) {
            apiUtils.when(() -> ApiUtils.createServiceContext(any())).thenReturn(serviceContext);
            when(queryPreprocessor.process(eq(serviceContext), eq(body), anyString())).thenReturn(processedBody);
            when(request.getMethod()).thenReturn("POST");
            when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.emptyList()));

            when(mockConnection.getResponseCode()).thenReturn(400);
            when(mockConnection.getResponseMessage()).thenReturn("Bad Request");
            when(mockConnection.getErrorStream()).thenReturn(new ByteArrayInputStream("error details".getBytes()));
            when(mockConnection.getOutputStream()).thenReturn(new ByteArrayOutputStream());

            esHTTPProxy.search("bucket", new RelatedItemType[0], session, request, response, body);

            verify(response).sendError(eq(400), contains("Bad Request"));
            verify(response).sendError(eq(400), contains("error details"));
        }
    }

    @Test
    public void testMSearchSuccessful() throws Exception {
        String body = "{}\n{\"query\":{\"match_all\":{}}}";
        String processedBody = body;

        try (MockedStatic<ApiUtils> apiUtils = mockStatic(ApiUtils.class)) {
            apiUtils.when(() -> ApiUtils.createServiceContext(any())).thenReturn(serviceContext);
            when(queryPreprocessor.process(eq(serviceContext), eq(body), anyString())).thenReturn(processedBody);
            when(request.getMethod()).thenReturn("POST");
            when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.emptyList()));

            // Mock URL connection
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            when(mockConnection.getOutputStream()).thenReturn(outputStream);
            when(mockConnection.getResponseCode()).thenReturn(200);
            when(mockConnection.getContentType()).thenReturn("application/x-ndjson");
            when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream("{}".getBytes()));
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("Content-Type", Collections.singletonList("application/x-ndjson"));
            when(mockConnection.getHeaderFields()).thenReturn(headers);

            // Mock ServletOutputStream
            final ByteArrayOutputStream responseOut = new ByteArrayOutputStream();
            ServletOutputStream servletOutputStream = new ServletOutputStream() {
                @Override
                public boolean isReady() { return true; }
                @Override
                public void setWriteListener(WriteListener writeListener) {}
                @Override
                public void write(int b) throws IOException { responseOut.write(b); }
            };
            when(response.getOutputStream()).thenReturn(servletOutputStream);

            esHTTPProxy.msearch("bucket", new RelatedItemType[0], session, request, response, body);

            // Verify
            verify(queryPreprocessor).process(eq(serviceContext), eq(body), eq("bucket"));
            verify(mockConnection).connect();
            // addPermissions should be false because contentType is not application/json
            verify(responseProcessor).processResponse(eq(serviceContext), eq(session), any(), any(), eq("_msearch"), eq("bucket"), eq(false), any());
        }
    }

    @Test
    public void testCallAdmin() throws Exception {
        String body = "{\"query\":{\"match_all\":{}}}";
        String endPoint = "_count";

        try (MockedStatic<ApiUtils> apiUtils = mockStatic(ApiUtils.class)) {
            apiUtils.when(() -> ApiUtils.createServiceContext(any())).thenReturn(serviceContext);
            when(request.getMethod()).thenReturn("POST");
            when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.emptyList()));

            // Mock URL connection
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            when(mockConnection.getOutputStream()).thenReturn(outputStream);
            when(mockConnection.getResponseCode()).thenReturn(200);
            when(mockConnection.getContentType()).thenReturn("application/json");
            when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream("{}".getBytes()));
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("Content-Type", Collections.singletonList("application/json"));
            when(mockConnection.getHeaderFields()).thenReturn(headers);

            // Mock ServletOutputStream
            final ByteArrayOutputStream responseOut = new ByteArrayOutputStream();
            ServletOutputStream servletOutputStream = new ServletOutputStream() {
                @Override
                public boolean isReady() { return true; }
                @Override
                public void setWriteListener(WriteListener writeListener) {}
                @Override
                public void write(int b) throws IOException { responseOut.write(b); }
            };
            when(response.getOutputStream()).thenReturn(servletOutputStream);

            esHTTPProxy.call("bucket", endPoint, session, request, response, body);

            // Verify
            // queryPreprocessor.process should NOT be called for other endpoints
            verify(queryPreprocessor, never()).process(any(), any(), any());
            verify(mockConnection).connect();
            verify(responseProcessor).processResponse(eq(serviceContext), eq(session), any(), any(), eq(endPoint), eq("bucket"), eq(true), isNull());
            assertEquals(body, outputStream.toString("UTF-8"));
        }
    }

    @Test
    public void testHeaderCopyingAndAuth() throws Exception {
        String body = "{}";
        ReflectionTestUtils.setField(esHTTPProxy, "username", "user");
        ReflectionTestUtils.setField(esHTTPProxy, "password", "pass");

        try (MockedStatic<ApiUtils> apiUtils = mockStatic(ApiUtils.class)) {
            apiUtils.when(() -> ApiUtils.createServiceContext(any())).thenReturn(serviceContext);
            when(request.getMethod()).thenReturn("POST");
            when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.singletonList("Custom-Header")));
            when(request.getHeader("Custom-Header")).thenReturn("Custom-Value");

            lenient().when(mockConnection.getOutputStream()).thenReturn(new ByteArrayOutputStream());
            lenient().when(mockConnection.getResponseCode()).thenReturn(200);
            lenient().when(mockConnection.getContentType()).thenReturn("application/json");
            lenient().when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream("{}".getBytes()));

            Map<String, List<String>> headers = new HashMap<>();
            headers.put("content-type", Collections.singletonList("application/json"));
            lenient().when(mockConnection.getHeaderFields()).thenReturn(headers);

            lenient().when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

            esHTTPProxy.search("bucket", new RelatedItemType[0], session, request, response, body);

            // Verify request headers
            verify(mockConnection).setRequestProperty("Custom-Header", "Custom-Value");
            verify(mockConnection).setRequestProperty(eq("Authorization"), startsWith("Basic "));
        }
    }
}
