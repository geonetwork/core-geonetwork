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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PatchMapping;

@Configuration
@OpenAPIDefinition
public class OpenApiConfig {
    @Bean
    public OpenAPI OpenApiConfig() {
        String packagesToscan[] = {"org.fao.geonet.api", "org.fao.geonet.monitor.service"};

        return new OpenAPI()
            .info(new Info().title("GeoNetwork OpenApi Documentation (beta)")
                .description("Learn how to access the catalog using the GeoNetwork REST API.")
                .version(API.VERSION_0_1)
                .license(new License()
                    .name("GPL 2.0")
                    .url("http://www.gnu.org/licenses/old-licenses/gpl-2.0.html")))
            .externalDocs(new ExternalDocumentation()
                .description("Learn how to access the catalog using the GeoNetwork REST API.")
                .url("https://localhost:8080/docs"));
    }

    @Bean
    public GroupedOpenApi userOpenApi() {
        String packagesToscan[] = {"org.fao.geonet.api"};
        return GroupedOpenApi.builder().setGroup("geonetwork").packagesToScan(packagesToscan)
            .build();
    }
}
