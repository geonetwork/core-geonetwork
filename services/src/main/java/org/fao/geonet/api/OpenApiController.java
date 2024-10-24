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
import com.fasterxml.jackson.databind.SerializationFeature;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.PathUtils;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.OpenAPI;

import org.springdoc.api.AbstractOpenApiResource;
import org.springdoc.core.*;
import org.springdoc.core.customizers.SpringDocCustomizers;
import org.springdoc.core.providers.ActuatorProvider;
import org.springdoc.core.providers.SecurityOAuth2Provider;
import org.springdoc.core.providers.SpringWebProvider;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import javax.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springdoc.core.Constants.*;
import static org.springframework.util.AntPathMatcher.DEFAULT_PATH_SEPARATOR;

/**
 * see https://springdoc.org/faq.html#how-to-integrate-open-api-3-with-spring-project-not-spring-boot
 * see https://springdoc.org/migrating-from-springfox.html
 *
 * TODO: Would be better to load properties from
 * @PropertySource("classpath:springdoc.properties")
 * springdoc.packagesToScan=org.fao.geonet.api,org.fao.geonet.services.inspireatom,org.fao.geonet.monitor.service
 * springdoc.paths-to-match=/api/**
 * springdoc.paths-to-exclude=/api/0.1/**
 * springdoc.api-docs.enabled=true
 * springdoc.api-docs.path=/api/doc
 * springdoc.cache.disabled=true
 * springdoc.writer-with-order-by-keys=true
 * springdoc.writer-with-default-pretty-printer=true
 */

/* This class is based off of org.springdoc.webmvc.api.OpenApiResource
 */
@RestController
public class OpenApiController extends AbstractOpenApiResource {

    private final RequestMappingInfoHandlerMapping requestMappingHandlerMapping;

    private final Optional<ActuatorProvider> servletContextProvider;

    private final Optional<SecurityOAuth2Provider> springSecurityOAuth2Provider;

    @Autowired
    public OpenApiController(ObjectFactory<OpenAPIService> openAPIBuilderObjectFactory,
                             AbstractRequestService requestBuilder,
                             GenericResponseService responseBuilder,
                             OperationService operationParser,
                             RequestMappingInfoHandlerMapping requestMappingHandlerMapping,
                             SpringDocConfigProperties springDocConfigProperties,
                             SpringDocProviders springDocProviders,
                             SpringDocCustomizers springDocCustomizers,
                             Optional<ActuatorProvider> servletContextProvider,
                             Optional<SecurityOAuth2Provider> springSecurityOAuth2Provider) {
        super(DEFAULT_GROUP_NAME, openAPIBuilderObjectFactory, requestBuilder, responseBuilder, operationParser, springDocConfigProperties, springDocProviders, springDocCustomizers);
        springDocConfigProperties.setPathsToExclude(Arrays.asList("/0.1/**"));
        springDocConfigProperties.setPackagesToScan(Arrays.asList("org.fao.geonet.api",
                "org.fao.geonet.services.inspireatom",
                "org.fao.geonet.monitor.service"));

        // Ensure open api document is consistently orders to make it easier to compare changes later.
        springDocConfigProperties.setWriterWithOrderByKeys(true);
        springDocConfigProperties.setWriterWithDefaultPrettyPrinter(true);

        // remove default response
        springDocConfigProperties.setOverrideWithGenericResponse(false);

        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.servletContextProvider = servletContextProvider;
        this.springSecurityOAuth2Provider = springSecurityOAuth2Provider;

        // Ensure all enums are written based on the enum name.
        Json.mapper().configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false);
        Yaml.mapper().configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false);
    }

    @Operation(hidden = true)
    @GetMapping(value = "/{portal}/api/doc", produces = MediaType.APPLICATION_JSON_VALUE)
    public String openapiJson(HttpServletRequest request, Locale locale)
        throws JsonProcessingException {
        calculateServerUrl(request, locale);
        OpenAPI openAPI = this.getOpenApi(locale);
        return new String(this.writeJsonValue(openAPI), StandardCharsets.UTF_8);
    }

    @Operation(hidden = true)
    @GetMapping(value = "/{portal}/api/doc.yml", produces = APPLICATION_OPENAPI_YAML)
    public String openapiYaml(HttpServletRequest request, Locale locale)
        throws JsonProcessingException {
        calculateServerUrl(request, locale);
        OpenAPI openAPI = this.getOpenApi(locale);
        return new String(this.writeYamlValue(openAPI), StandardCharsets.UTF_8);
    }

    @Override
    protected void getPaths(Map<String, Object> restControllers, Locale locale, OpenAPI openAPI) {
        Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
        calculatePath(restControllers, map, locale, openAPI);

        if (servletContextProvider.isPresent()) {
            map = servletContextProvider.get().getMethods();
            calculatePath(restControllers, map, locale, openAPI);
        }
        if (this.springSecurityOAuth2Provider.isPresent()) {
            SecurityOAuth2Provider securityOAuth2Provider = this.springSecurityOAuth2Provider.get();
            Map<RequestMappingInfo, HandlerMethod> mapOauth = securityOAuth2Provider.getHandlerMethods();
            Map<String, Object> requestMappingMapSec = securityOAuth2Provider.getFrameworkEndpoints();
            Class[] additionalRestClasses = requestMappingMapSec.values().stream().map(Object::getClass).toArray(Class[]::new);
            AbstractOpenApiResource.addRestControllers(additionalRestClasses);
            calculatePath(requestMappingMapSec, mapOauth, locale, openAPI);
        }
    }


    protected void calculatePath(Map<String, Object> restControllers,
                                 Map<RequestMappingInfo, HandlerMethod> map,
                                 Locale locale,
                                 OpenAPI openAPI) {
        TreeMap<RequestMappingInfo, HandlerMethod> methodTreeMap = new TreeMap<>(byReversedRequestMappingInfos());
        methodTreeMap.putAll(map);
        Optional<SpringWebProvider> springWebProviderOptional = springDocProviders.getSpringWebProvider();
        springWebProviderOptional.ifPresent(springWebProvider -> {
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : map.entrySet()) {
                RequestMappingInfo requestMappingInfo = entry.getKey();
                HandlerMethod handlerMethod = entry.getValue();
                Set<String> patterns = springWebProvider.getActivePatterns(requestMappingInfo);
                if (!CollectionUtils.isEmpty(patterns)) {
                    Map<String, String> regexMap = new LinkedHashMap<>();
                    for (String pattern : patterns) {
                        String operationPath = PathUtils.parsePath(pattern, regexMap)
                            .replace("/{portal}/api", "");
                        String[] produces = requestMappingInfo.getProducesCondition().getProducibleMediaTypes().stream().map(MimeType::toString).toArray(String[]::new);
                        String[] consumes = requestMappingInfo.getConsumesCondition().getConsumableMediaTypes().stream().map(MimeType::toString).toArray(String[]::new);
                        String[] headers = requestMappingInfo.getHeadersCondition().getExpressions().stream().map(Object::toString).toArray(String[]::new);
                        String[] params = requestMappingInfo.getParamsCondition().getExpressions().stream().map(Object::toString).toArray(String[]::new);
                        if ((isRestController(restControllers, handlerMethod, operationPath) || isActuatorRestController(operationPath, handlerMethod))
                            && isFilterCondition(handlerMethod, operationPath, produces, consumes, headers)) {
                            Set<RequestMethod> requestMethods = requestMappingInfo.getMethodsCondition().getMethods();
                            // default allowed requestmethods
                            if (requestMethods.isEmpty())
                                requestMethods = this.getDefaultAllowedHttpMethods();
                            calculatePath(handlerMethod, operationPath, requestMethods, consumes, produces, headers, params, locale, openAPI);
                        }
                    }
                }
            }
        });
    }

    /**
     * By reversed request mapping infos comparator.
     *
     * @return the comparator
     */
    private Comparator<RequestMappingInfo> byReversedRequestMappingInfos() {
        return (o2, o1) -> o1.toString().compareTo(o2.toString());
    }

    @Override
    protected boolean isRestController(Map<String, Object> restControllers,
                                       HandlerMethod handlerMethod,
                                       String operationPath) {
        return (restControllers.containsKey(handlerMethod.getBean().toString())
                || isAdditionalRestController(handlerMethod.getBeanType()))
            && operationPath.startsWith(DEFAULT_PATH_SEPARATOR)
            && (springDocConfigProperties.isModelAndViewAllowed()
                || !ModelAndView.class.isAssignableFrom(handlerMethod.getMethod().getReturnType()));
    }

    protected void calculateServerUrl(HttpServletRequest request, Locale locale) {
        super.initOpenAPIBuilder(locale);
        String calculatedUrl = this.getServerUrl(request);
        this.openAPIService.setServerBaseUrl(calculatedUrl);
    }

    private String getServerUrl(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        StringBuffer requestURL = request.getRequestURL();
        String serverBaseUrl = requestURL.substring(0, requestURL.indexOf(contextPath) + contextPath.length());
        return serverBaseUrl;
    }
}
