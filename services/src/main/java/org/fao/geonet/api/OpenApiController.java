/*
 *
 *  *
 *  *  * Copyright 2019-2020 the original author or authors.
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *      https://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *
 *
 */

package org.fao.geonet.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.PathUtils;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.OpenAPI;
import org.fao.geonet.ApplicationContextHolder;
import org.springdoc.api.AbstractOpenApiResource;
import org.springdoc.core.*;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.visitor.AbstractRouterFunctionVisitor;
import org.springdoc.webmvc.api.ActuatorProvider;
import org.springdoc.webmvc.api.RouterFunctionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.springdoc.core.Constants.*;
import static org.springframework.util.AntPathMatcher.DEFAULT_PATH_SEPARATOR;

/**
 * see https://springdoc.org/faq.html#how-to-integrate-open-api-3-with-spring-project-not-spring-boot
 * see https://springdoc.org/migrating-from-springfox.html
 *
 * TODO: Would be better to load properties from
 * @PropertySource("classpath:springdoc.properties")
 * springdoc.packagesToScan=org.fao.geonet.api,org.fao.geonet.monitor.service
 * springdoc.paths-to-match=/api/**
 * springdoc.paths-to-exclude=/api/0.1/**
 * springdoc.api-docs.enabled=true
 * springdoc.api-docs.path=/api/doc
 * springdoc.cache.disabled=true
 */
@RestController
public class OpenApiController extends AbstractOpenApiResource {

    private final RequestMappingInfoHandlerMapping requestMappingHandlerMapping;

    private final Optional<ActuatorProvider> servletContextProvider;

    private final Optional<SecurityOAuth2Provider> springSecurityOAuth2Provider;

    private final Optional<RouterFunctionProvider> routerFunctionProvider;

    @Autowired
    public OpenApiController(OpenAPIBuilder openAPIBuilder, AbstractRequestBuilder requestBuilder,
                           GenericResponseBuilder responseBuilder, OperationBuilder operationParser,
                           RequestMappingInfoHandlerMapping requestMappingHandlerMapping,
                           Optional<ActuatorProvider> servletContextProvider,
                           Optional<List<OperationCustomizer>> operationCustomizers,
                           Optional<List<OpenApiCustomiser>> openApiCustomisers,
                           SpringDocConfigProperties springDocConfigProperties,
                           Optional<SecurityOAuth2Provider> springSecurityOAuth2Provider,
                           Optional<RouterFunctionProvider> routerFunctionProvider) {
        super(DEFAULT_GROUP_NAME, openAPIBuilder, requestBuilder, responseBuilder, operationParser, operationCustomizers, openApiCustomisers, springDocConfigProperties);

//        springDocConfigProperties.setPathsToMatch(Arrays.asList(new String[]{"api/**"}));
        springDocConfigProperties.setPathsToExclude(Arrays.asList(new String[]{"/0.1/**"}));
        springDocConfigProperties.setPackagesToScan(Arrays.asList(new String[]{
            "org.fao.geonet.api",
            "org.fao.geonet.monitor.service"}));
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.servletContextProvider = servletContextProvider;
        this.springSecurityOAuth2Provider = springSecurityOAuth2Provider;
        this.routerFunctionProvider = routerFunctionProvider;
    }

    @Operation(hidden = true)
    @GetMapping(value = "/{portal}/api/doc", produces = MediaType.APPLICATION_JSON_VALUE)
    public String openapiJson(HttpServletRequest request, @Value(API_DOCS_URL) String apiDocsUrl)
        throws JsonProcessingException {
        calculateServerUrl(request, apiDocsUrl);
        OpenAPI openAPI = this.getOpenApi();
        return Json.mapper().writeValueAsString(openAPI);
    }

    @Operation(hidden = true)
    @GetMapping(value = "/{portal}/api/doc.yml", produces = APPLICATION_OPENAPI_YAML)
    public String openapiYaml(HttpServletRequest request, @Value(DEFAULT_API_DOCS_URL_YAML) String apiDocsUrl)
        throws JsonProcessingException {
        calculateServerUrl(request, apiDocsUrl);
        OpenAPI openAPI = this.getOpenApi();
        return Yaml.mapper().writeValueAsString(openAPI);
    }

    @Override
    protected void getPaths(Map<String, Object> restControllers) {
        Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
        calculatePath(restControllers, map, Optional.empty());

        if (servletContextProvider.isPresent()) {
            map = servletContextProvider.get().getMethods();
            this.openAPIBuilder.addTag(new HashSet<>(map.values()), servletContextProvider.get().getTag());
            calculatePath(restControllers, map, servletContextProvider);
        }
        if (this.springSecurityOAuth2Provider.isPresent()) {
            SecurityOAuth2Provider securityOAuth2Provider = this.springSecurityOAuth2Provider.get();
            Map<RequestMappingInfo, HandlerMethod> mapOauth = securityOAuth2Provider.getHandlerMethods();
            Map<String, Object> requestMappingMapSec = securityOAuth2Provider.getFrameworkEndpoints();
            Class[] additionalRestClasses = requestMappingMapSec.values().stream().map(Object::getClass).toArray(Class[]::new);
            AbstractOpenApiResource.addRestControllers(additionalRestClasses);
            calculatePath(requestMappingMapSec, mapOauth, Optional.empty());
        }
    }

    protected void calculatePath(Map<String, Object> restControllers, Map<RequestMappingInfo, HandlerMethod> map, Optional<ActuatorProvider> actuatorProvider) {
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : map.entrySet()) {
            RequestMappingInfo requestMappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();
            PatternsRequestCondition patternsRequestCondition = requestMappingInfo.getPatternsCondition();
            Set<String> patterns = patternsRequestCondition.getPatterns();
            Map<String, String> regexMap = new LinkedHashMap<>();
            for (String pattern : patterns) {
                String operationPath = PathUtils.parsePath(pattern, regexMap)
                    .replace("/{portal}/api", "");
                if (((actuatorProvider.isPresent() && actuatorProvider.get().isRestController(operationPath))
                    || isRestController(restControllers, handlerMethod, operationPath))
                    && isPackageToScan(handlerMethod.getBeanType().getPackage())
                    && isPathToMatch(operationPath)) {
                    Set<RequestMethod> requestMethods = requestMappingInfo.getMethodsCondition().getMethods();
                    // default allowed requestmethods
                    if (requestMethods.isEmpty())
                        requestMethods = this.getDefaultAllowedHttpMethods();
                    calculatePath(handlerMethod, operationPath, requestMethods);
                }
            }
        }
//        routerFunctionProvider.ifPresent(routerFunctions -> routerFunctions.getWebMvcRouterFunctionPaths()
//            .ifPresent(routerBeans -> routerBeans.forEach(this::getRouterFunctionPaths)));
    }

    protected boolean isRestController(Map<String, Object> restControllers, HandlerMethod handlerMethod,
                                       String operationPath) {
        ResponseBody responseBodyAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), ResponseBody.class);
        if (responseBodyAnnotation == null)
            responseBodyAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), ResponseBody.class);

        return (responseBodyAnnotation != null && restControllers.containsKey(handlerMethod.getBean().toString()) || isAdditionalRestController(handlerMethod.getBeanType()))
            && operationPath.startsWith(DEFAULT_PATH_SEPARATOR)
            && (springDocConfigProperties.isModelAndViewAllowed() || !ModelAndView.class.isAssignableFrom(handlerMethod.getMethod().getReturnType()));
    }

    protected void calculateServerUrl(HttpServletRequest request, String apiDocsUrl) {
        String requestUrl = decode(request.getRequestURL().toString());
        String calculatedUrl = requestUrl.substring(0, requestUrl.length() - apiDocsUrl.length());
        openAPIBuilder.setServerBaseUrl(calculatedUrl);
    }
}
