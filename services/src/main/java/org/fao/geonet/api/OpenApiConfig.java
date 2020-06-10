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
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import org.fao.geonet.NodeInfo;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.ArrayList;
import java.util.List;

@Configuration
@OpenAPIDefinition
public class OpenApiConfig {
    @Bean
    public OpenAPI OpenApiConfig() {
        List<Server> servers = new ArrayList<>();

        String localhostUrl = "http://localhost:8080/geonetwork";
        ServerVariable catalogVariable = new ServerVariable()
            .description("The GeoNetwork catalog to contact")
            .addEnumItem(localhostUrl)
            .addEnumItem("../..")
            .addEnumItem("https://vanilla.geocat.net/geonetwork");
        catalogVariable.setDefault(localhostUrl);

        ServerVariable portalVariable = new ServerVariable()
            .description("The thematic portal")
            .addEnumItem(NodeInfo.DEFAULT_NODE);
        portalVariable.setDefault(NodeInfo.DEFAULT_NODE);

        servers.add(new Server()
            .description("My GeoNetwork")
            .url("{catalog}/{portal}/api")
            .variables(new ServerVariables()
                .addServerVariable("catalog", catalogVariable)
                .addServerVariable("portal", portalVariable)
            )
        );

        return new OpenAPI()
            .info(new Info()
                .title("GeoNetwork OpenAPI Documentation (beta)")
                .description("This is the description of the GeoNetwork OpenAPI. Use this API to manage your catalog.")
                .contact(new Contact()
                    .email("geonetwork-users@lists.sourceforge.net")
                    .name("GeoNetwork user mailing list")
                    .url("https://sourceforge.net/p/geonetwork/mailman/geonetwork-users/")
                )
                .version(API.VERSION_0_1)
                .license(new License()
                    .name("GPL 2.0")
                    .url("http://www.gnu.org/licenses/old-licenses/gpl-2.0.html")))
            .externalDocs(new ExternalDocumentation()
                .description("Learn how to access the catalog using the GeoNetwork REST API.")
                .url("https://localhost:8080/geonetwork/doc/api"))
            .servers(servers);
    }
}
