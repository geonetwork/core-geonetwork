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
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static org.fao.geonet.kernel.setting.Settings.SYSTEM_PLATFORM_VERSION;

@Configuration
@EnableCaching
@OpenAPIDefinition
public class OpenApiConfig  {

    @Bean(name = "cacheManager")
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("translations", "thesaurusSearches");
    }

    @Bean
    @Autowired
    public OpenAPI OpenApiConfig(SettingManager settingManager) {
        List<Server> servers = new ArrayList<>();

        String version = settingManager.getValue(SYSTEM_PLATFORM_VERSION);
        String hostUrl = settingManager.getBaseURL().replaceAll("/+$", "");

        ServerVariable catalogVariable = new ServerVariable()
            .description("The GeoNetwork catalog to contact")
            .addEnumItem(hostUrl)
            .addEnumItem("../..");
        catalogVariable.setDefault(hostUrl);

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

        return new OpenAPI().info(new Info()
                .title(String.format(
                    "GeoNetwork %s OpenAPI Documentation",
                    version))
                .description("This is the description of the GeoNetwork OpenAPI. Use this API to manage your catalog.")
                .contact(new Contact()
                    .email("geonetwork-users@lists.sourceforge.net")
                    .name("GeoNetwork user mailing list")
                    .url("https://sourceforge.net/p/geonetwork/mailman/geonetwork-users/")
                )
                .version(version)
                .license(new License()
                    .name("GPL 2.0")
                    .url("http://www.gnu.org/licenses/old-licenses/gpl-2.0.html")))
            .externalDocs(new ExternalDocumentation()
                .description("Learn how to access the catalog using the GeoNetwork REST API.")
                .url(String.format("%s/doc/api", hostUrl)))
            .servers(servers);
    }
}
