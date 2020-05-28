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

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

//
@PropertySource("classpath:springdoc.properties")
//@Configuration
//@ComponentScan(basePackages = {
//    "org.fao.geonet.api",
//    "org.fao.geonet.monitor.service"
//})
// https://springdoc.org/faq.html#how-to-integrate-open-api-3-with-spring-project-not-spring-boot
// https://springdoc.org/migrating-from-springfox.html
@Configuration
public class ApiConfig implements ServletContextInitializer {
    public OpenAPI ApiConfig() {
        return new OpenAPI()
            .info(new Info().title("GeoNetwork Api Documentation (beta)")
                .description("Learn how to access the catalog using the GeoNetwork REST API.")
                .version(API.VERSION_0_1)
                .license(new License().name("GPL 2.0").url("http://www.gnu.org/licenses/old-licenses/gpl-2.0.html")))
            .externalDocs(new ExternalDocumentation()
                .description("Learn how to access the catalog using the GeoNetwork REST API.")
                .url("https://localhost:8080/docs"));
    }


    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        WebApplicationContext context = getContext();
        servletContext.addListener(new ContextLoaderListener(context));
        ServletRegistration.Dynamic dispatcher =
            servletContext.addServlet("OpenApiServlet",
            new DispatcherServlet(context));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/v3/*");
    }

    private AnnotationConfigWebApplicationContext getContext() {
        AnnotationConfigWebApplicationContext context =
            new AnnotationConfigWebApplicationContext();
        context.scan("org.fao.geonet.api");
        context.register(this.getClass(),
//            org.springdoc.ui.SwaggerConfig.class,
            org.springdoc.core.SwaggerUiConfigProperties.class,
            org.springdoc.core.SwaggerUiOAuthProperties.class,
            org.springdoc.webmvc.core.SpringDocWebMvcConfiguration.class,
            org.springdoc.webmvc.core.MultipleOpenApiSupportConfiguration.class,
            org.springdoc.core.SpringDocConfiguration.class,
            org.springdoc.core.SpringDocConfigProperties.class,
            org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class);

        return context;
    }
}
//
//public class ApiConfig {
////    @Bean
////    public GroupedOpenApi api() {
////        this.doc = GroupedOpenApi.builder()
////            .setGroup("public")
////            .pathsToMatch("/**/api/**")
////            .build();
//////            .apiInfo(new ApiInfo(
//////                "GeoNetwork Api Documentation (beta)",
//////                "Learn how to access the catalog using the GeoNetwork REST API.",
//////                API.VERSION_0_1,
//////                "urn:tos",
//////                API.CONTACT_EMAIL,
//////                "GPL 2.0",
//////                "http://www.gnu.org/licenses/old-licenses/gpl-2.0.html"))
//////            .select()
//////            .apis(RequestHandlerSelectors.any())
//////            .paths(paths())
//////            .build()
//////            .pathMapping("/")
//////            .directModelSubstitute(LocalDate.class, String.class)
//////            .directModelSubstitute(UserSecurity.class, Object.class)
//////            .directModelSubstitute(Element.class, Object.class)
//////            .genericModelSubstitutes(ResponseEntity.class)
//////            .alternateTypeRules(
//////                newRule(typeResolver.resolve(DeferredResult.class,
//////                    typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
//////                    typeResolver.resolve(WildcardType.class)))
//////            .useDefaultResponseMessages(false)
//////            .securitySchemes(newArrayList(new BasicAuth("basicAuth")));
////        return this.doc;
//    }
//
////    @Autowired
////    private TypeResolver typeResolver;
//
////    private GroupedOpenApi doc;
//
////    private Predicate<String> paths() {
////        return or(
////            regex("/.*/api/" + API.VERSION_0_1 + "/.*")
////        );
////    }
//}
