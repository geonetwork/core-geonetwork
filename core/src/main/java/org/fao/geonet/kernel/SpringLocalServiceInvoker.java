/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
package org.fao.geonet.kernel;

import jeeves.server.context.ServiceContext;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

public class SpringLocalServiceInvoker {

    public final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public final RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    private HandlerMethodArgumentResolverComposite argumentResolvers;
    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;
    private DefaultDataBinderFactory webDataBinderFactory;

    public SpringLocalServiceInvoker(RequestMappingHandlerMapping requestMappingHandlerMapping, RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
    }

    public void init() {
        argumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(requestMappingHandlerAdapter.getArgumentResolvers());
        returnValueHandlers = new HandlerMethodReturnValueHandlerComposite().addHandlers(requestMappingHandlerAdapter.getReturnValueHandlers());
        webDataBinderFactory = new DefaultDataBinderFactory(requestMappingHandlerAdapter.getWebBindingInitializer());
    }

    public Object invoke(String uri) throws Exception {
        MockHttpServletRequest request = prepareMockRequestFromUri(uri);
        MockHttpServletResponse response = new MockHttpServletResponse();
        return invoke(request, response);
    }

    public Object invoke(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ServiceContext srvContext = ServiceContext.get();
        try {
            return doInvoke(request, response);
        } finally {
            //doInvoke triggered the creation of the request's context, so we restore it
            restoreNonAnonymousSrvContextToAvoidUILanguageLoss(srvContext);
        }
    }

    private static void restoreNonAnonymousSrvContextToAvoidUILanguageLoss(ServiceContext srvContext) {
        if (srvContext != null) {
            srvContext.setAsThreadLocal();
        }
    }

    private Object doInvoke(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HandlerExecutionChain handlerExecutionChain = requestMappingHandlerMapping.getHandler(request);
        HandlerMethod handlerMethod = (HandlerMethod) handlerExecutionChain.getHandler();

        ServletInvocableHandlerMethod servletInvocableHandlerMethod = new ServletInvocableHandlerMethod(handlerMethod);
        servletInvocableHandlerMethod.setHandlerMethodArgumentResolvers(argumentResolvers);
        servletInvocableHandlerMethod.setHandlerMethodReturnValueHandlers(returnValueHandlers);
        servletInvocableHandlerMethod.setDataBinderFactory(webDataBinderFactory);

        Object o = servletInvocableHandlerMethod.invokeForRequest(new ServletWebRequest(request, response), null, new Object[0]);
        // check whether we need to further process a "forward:" response
        if (o instanceof String) {
          String checkForward = (String)o;
          if (checkForward.startsWith("forward:")) {
            //
            // if the original url ends with the first component of the fwd url, then concatenate them, otherwise
            // just invoke it and hope for the best...
            // eg. local://srv/api/records/urn:marlin.csiro.au:org:1_organisation_name
            // returns forward:urn:marlin.csiro.au:org:1_organisation_name/formatters/xml
            // so we join the original url and the forwarded url as:
            // /api/records/urn:marlin.csiro.au:org:1_organisation_name/formatters/xml and invoke it.
            //
            String fwdUrl = StringUtils.substringAfter(checkForward,"forward:");
						String lastComponent = StringUtils.substringAfterLast(request.getRequestURI(),"/");
            if (!lastComponent.isEmpty() && StringUtils.startsWith(fwdUrl, lastComponent)) {
                return invoke(request.getRequestURI() + StringUtils.substringAfter(fwdUrl, lastComponent));
						} else {
                return invoke(fwdUrl);
           	}
          }
        }
        return o;
    }

    /**
     * prepareMockRequestFromUri will search for spring services that match
     * the request and execute them. Typically used for the local:// xlink
     * speed up. Accepts urls prefixed with local://<nodename> eg.
     * local://srv/api/records/..
     * but also urls prefixed with the nodename only eg. '/srv/api/records/..'
     */
    private MockHttpServletRequest prepareMockRequestFromUri(String uri) {
        String requestURI = uri.replace("local:/","").split("\\?")[0];
        MockHttpServletRequest request = new MockHttpServletRequest("GET", requestURI);
        request.setSession(new MockHttpSession());
        String[] splits = uri.split("\\?");
        if (splits.length > 1) {
            String params = splits[1];
            for (String param : params.split("&")) {
                String[] parts = param.split("=");
                String name = parts[0];
                request.addParameter(name, parts.length == 2 ? parts[1] : "");
            }
        }
        return request;
    }
}
