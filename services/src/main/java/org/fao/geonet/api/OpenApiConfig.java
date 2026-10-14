/*
 * =============================================================================
 * ===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.springdoc.core.SpringDocUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.fao.geonet.kernel.setting.Settings.SYSTEM_PLATFORM_VERSION;

@Configuration
@EnableCaching
@OpenAPIDefinition
public class OpenApiConfig {

    private static OpenAPI openAPI = null;

    private static SettingManager settingManager = null;

    static {
        // By default, Spring Doc ignores injectable parameters supported by Spring MVC
        //     https://springdoc.org/faq.html#_what_are_the_ignored_types_in_the_documentation
        // This includes the following list.
        //     https://docs.spring.io/spring-framework/docs/5.1.x/spring-framework-reference/web.html#mvc-ann-arguments
        // As java.util.Map's are uses for JSON input and output values among other types, it is best to remove java.util.Map from this list.
        // If we want to use java.util.Map as an injectable then we will need to ensure we hide the parameter.
        SpringDocUtils.getConfig().removeRequestWrapperToIgnore(Map.class);
    }

    @Bean(name = "cacheManager")
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("translations", "thesaurusSearches");
    }

    @Bean
    public OpenAPI openApi(final SettingManager settingManager) {
        return OpenApiConfig.setupOpenApiConfig(settingManager);
    }

    /**
     * Setup OpenAPI configuration.
     *
     * Using static function so that should the bean be called twice (which it does), it will reuse the same static objects.
     * During first call, the settingManager may not be properly setup (i.e. initial install) and will return null values for version and host information.
     * Subsequent calls will update the related OpenAPI information with the settingManager values.
     *
     * It is also static so that when we update the object on second call, it will also update the object returned from the first call as they will be
     * pointing to the same object.
     *
     * @param settingManager containing host and version information required.
     * @return OpenAPI object.
     */
    private static OpenAPI setupOpenApiConfig(final SettingManager settingManager) {
        OpenApiConfig.settingManager = settingManager;
        if (openAPI == null) {
            openAPI = new OpenAPI().info(new Info()
                    .description("This is the description of the GeoNetwork OpenAPI. Use this API to manage your catalog.")
                    .contact(new Contact()
                        .email("geonetwork-users@lists.sourceforge.net")
                        .name("GeoNetwork user mailing list")
                        .url("https://sourceforge.net/p/geonetwork/mailman/geonetwork-users/")
                    )
                    .license(new License()
                        .name("GPL 2.0")
                        .url("https://www.gnu.org/licenses/old-licenses/gpl-2.0.html")))
                .externalDocs(new ExternalDocumentation()
                    .description("Learn how to access the catalog using the GeoNetwork REST API."));

            setVersionRelatedInfo();
            setHostRelatedInfo();

        } else if (openAPI.getInfo() != null && openAPI.getInfo().getVersion() == null) {
            // During initial install, the version will not be set when using the JeevesApplicationContext
            // But it will be set afterward when creating WebApplicationContext.  So if the version is null but our new version is not null
            // then lets update data based on the version.
            setVersionRelatedInfo();
            // If the version was not set then the hostUrl was also not set correctly so update that as well.
            setHostRelatedInfo();
        }

        return openAPI;
    }

    /**
     * Update openAPI object with version related information.
     */
    private static void setVersionRelatedInfo() {

        String version = settingManager.getValue(SYSTEM_PLATFORM_VERSION);

        openAPI.getInfo().setVersion(version);
        openAPI.getInfo().setTitle(String.format(
            "GeoNetwork %s OpenAPI Documentation",
            version));
    }

    /**
     * Update openAPI object with host related information.
     */
    public static void setHostRelatedInfo() {
        if (settingManager == null || openAPI == null) {
            return;
        }

        List<Server> servers = new ArrayList<>();

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
        openAPI.setServers(servers);
        openAPI.getExternalDocs().setUrl(String.format("%s/doc/api", hostUrl));
    }
}
