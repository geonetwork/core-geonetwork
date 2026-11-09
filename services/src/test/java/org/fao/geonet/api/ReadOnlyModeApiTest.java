/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
package org.fao.geonet.api;

import jeeves.config.springutil.ReadOnlyMvcInterceptor;
import jeeves.services.ReadWriteController;
import org.fao.geonet.NodeInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReadOnlyModeApiTest {

    @Controller
    @ReadWriteController
    public static class TestApi {
        @GetMapping("/test")
        @ResponseBody
        public String read() {
            return "ok";
        }

        @PostMapping("/test")
        @ResponseBody
        public String write() {
            return "ok";
        }
    }

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestApi())
            .setControllerAdvice(new GlobalExceptionController())
            .addInterceptors(new ReadOnlyMvcInterceptor())
            .build();
    }

    @After
    public void resetReadOnly() {
        NodeInfo.setReadOnly(false);
    }

    @Test
    public void writesReturn503InReadOnlyMode() throws Exception {
        mockMvc.perform(post("/test")).andExpect(status().isOk());

        NodeInfo.setReadOnly(true);

        mockMvc.perform(get("/test")).andExpect(status().isOk());
        mockMvc.perform(post("/test").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.code", is("service_unavailable")));
    }
}
