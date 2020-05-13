/*
 * =============================================================================
 * ===	Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * ===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * ===	and United Nations Environment Programme (UNEP)
 * ===
 * ===	This program is free software; you can redistribute it and/or modify
 * ===	it under the terms of the GNU General Public License as published by
 * ===	the Free Software Foundation; either version 2 of the License, or (at
 * ===	your option) any later version.
 * ===
 * ===	This program is distributed in the hope that it will be useful, but
 * ===	WITHOUT ANY WARRANTY; without even the implied warranty of
 * ===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * ===	General Public License for more details.
 * ===
 * ===	You should have received a copy of the GNU General Public License
 * ===	along with this program; if not, write to the Free Software
 * ===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * ===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * ===	Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */

package org.fao.geonet.api;

import com.google.common.base.Predicate;

import com.fasterxml.classmate.TypeResolver;

import org.fao.geonet.NodeInfo;
import org.fao.geonet.domain.UserSecurity;
import org.jdom.Element;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Lists.newArrayList;
import static springfox.documentation.builders.PathSelectors.regex;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

@PropertySource("classpath:swagger.properties")
@Configuration
@ComponentScan(basePackages = {
    "org.fao.geonet.api",
    "org.fao.geonet.monitor.service"
})
@EnableSwagger2 //Loads the spring beans required by the framework
public class SwaggerConfig {
    @Bean
    public Docket api() {
        this.doc = new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(new ApiInfo(
                "GeoNetwork Api Documentation (beta)",
                "Learn how to access the catalog using the GeoNetwork REST API.",
                API.VERSION_0_1,
                "urn:tos",
                API.CONTACT_EMAIL,
                "GPL 2.0",
                "http://www.gnu.org/licenses/old-licenses/gpl-2.0.html"))
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(paths())
            .build()
            .pathMapping("/")
            .directModelSubstitute(LocalDate.class, String.class)
            .directModelSubstitute(UserSecurity.class, Object.class)
            .directModelSubstitute(Element.class, Object.class)
            .genericModelSubstitutes(ResponseEntity.class)
            .alternateTypeRules(
                newRule(typeResolver.resolve(DeferredResult.class,
                    typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
                    typeResolver.resolve(WildcardType.class)))
            .useDefaultResponseMessages(false)
            .securitySchemes(newArrayList(new BasicAuth("basicAuth")));
        return this.doc;
    }

    @Autowired
    private TypeResolver typeResolver;

    private Docket doc;

    private Predicate<String> paths() {
        return or(
            regex("/.*/api/" + API.VERSION_0_1 + "/.*")
        );
    }
}
