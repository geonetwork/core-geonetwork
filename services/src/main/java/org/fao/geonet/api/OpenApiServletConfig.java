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

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

@PropertySource("classpath:springdoc.properties")
// https://springdoc.org/faq.html#how-to-integrate-open-api-3-with-spring-project-not-spring-boot
// https://springdoc.org/migrating-from-springfox.html
@EnableWebMvc
//public class OpenApiServletConfig implements WebApplicationInitializer {
public class OpenApiServletConfig {
//    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
//        WebApplicationContext context = getContext();
//        servletContext.addListener(new ContextLoaderListener(context));
//        ServletRegistration.Dynamic dispatcher =
//            servletContext.addServlet("OpenApiServlet",
//                new DispatcherServlet(context));
//        dispatcher.setLoadOnStartup(10);
//        dispatcher.addMapping("/v3/*");
    }

    private AnnotationConfigWebApplicationContext getContext() {
        AnnotationConfigWebApplicationContext context =
            new AnnotationConfigWebApplicationContext();
//        context.scan("org.fao.geonet");
        context.register(this.getClass(),
//            org.springdoc.ui.SwaggerConfig.class,
//            org.springdoc.core.SwaggerUiConfigProperties.class,
//            org.springdoc.core.SwaggerUiOAuthProperties.class,
            OpenApiConfig.class,
            org.springdoc.webmvc.core.SpringDocWebMvcConfiguration.class,
            org.springdoc.webmvc.core.MultipleOpenApiSupportConfiguration.class,
            org.springdoc.core.SpringDocConfiguration.class,
            org.springdoc.core.SpringDocConfigProperties.class,
            org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class);

        return context;
    }
}
