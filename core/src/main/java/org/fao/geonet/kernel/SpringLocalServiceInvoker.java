package org.fao.geonet.kernel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.bind.support.DefaultDataBinderFactory;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

public class SpringLocalServiceInvoker {

    @Autowired
    public RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    public RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    private HandlerMethodArgumentResolverComposite argumentResolvers;
    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;
    private DefaultDataBinderFactory webDataBinderFactory;

    public void init() {
        argumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(requestMappingHandlerAdapter.getArgumentResolvers());
        returnValueHandlers = new HandlerMethodReturnValueHandlerComposite().addHandlers(requestMappingHandlerAdapter.getReturnValueHandlers());
        webDataBinderFactory = new DefaultDataBinderFactory(requestMappingHandlerAdapter.getWebBindingInitializer());
    }

    public Object invoke(String uri) throws Exception {
        MockHttpServletRequest request = prepareMockRequestFromUri(uri);
        MockHttpServletResponse response = new MockHttpServletResponse();

        HandlerExecutionChain handlerExecutionChain = requestMappingHandlerMapping.getHandler(request);
        HandlerMethod handlerMethod = (HandlerMethod) handlerExecutionChain.getHandler();

        ServletInvocableHandlerMethod servletInvocableHandlerMethod = new ServletInvocableHandlerMethod(handlerMethod);
        servletInvocableHandlerMethod.setHandlerMethodArgumentResolvers(argumentResolvers);
        servletInvocableHandlerMethod.setHandlerMethodReturnValueHandlers(returnValueHandlers);
        servletInvocableHandlerMethod.setDataBinderFactory(webDataBinderFactory);

        return servletInvocableHandlerMethod.invokeForRequest(new ServletWebRequest(request, response), null, new Object[0]);
    }

    private MockHttpServletRequest prepareMockRequestFromUri(String uri) {
        String requestURI = uri.replace("local://srv", "").split("\\?")[0];
        MockHttpServletRequest request = new MockHttpServletRequest("GET", requestURI);
        request.setSession(new MockHttpSession());
        boolean doesUriContainsParams = uri.indexOf("\\?") > 0;
        if (doesUriContainsParams) {
            String params = uri.split("\\?")[1];
            for (String param : params.split("&")) {
                String[] parts = param.split("=");
                String name = parts[0];
                String value = parts[1];
                request.addParameter(name, value);
            }
        }
        return request;
    }
}
