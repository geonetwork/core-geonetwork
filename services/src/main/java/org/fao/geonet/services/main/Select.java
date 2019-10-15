/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.services.main;

import com.google.common.collect.Sets;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;

import org.fao.geonet.kernel.SelectionManager;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.Set;

/**
 * Select a list of elements stored in session Returns status
 */
@Deprecated
@Controller("selection")
@ReadWriteController
public class Select implements ApplicationContextAware {
    private ApplicationContext context;

    public synchronized void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Select one or more metadata
     *
     * @param id       One or more id parameters. Use metadata UUID.
     * @param selected Type of action on selection ({@link SelectionManager#updateSelection(String,
     *                 UserSession, Element, ServiceContext)})
     * @param type     Only metadata can be selected. Unused for now. Leave blank
     * @return The number of select records
     */
    @RequestMapping(value = {"/{portal}/{lang}/metadata.select", "/{portal}/{lang}/md.select"},
        produces = {MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    String[] select(@RequestParam(required = false) String[] id,
                    @RequestParam(required = false) String selected,
                    @RequestParam(required = false, defaultValue = "metadata") String type)
        throws Exception {
        ServiceContext serviceContext = ServiceContext.get();

        int nbSelected = SelectionManager.updateSelection(type,
            serviceContext.getUserSession(),
            selected, id != null ? Arrays.asList(id) : null,
            serviceContext);

        return new String[]{nbSelected + ""};
    }

    /**
     *
     * @param type
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{portal}/{lang}/md.selected",
        produces = {MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    Set<String> getCurrentSelection(
        @RequestParam(required = false, defaultValue = "metadata") String type)
        throws Exception {
        ServiceContext serviceContext = ServiceContext.get();

        SelectionManager selectionManager =
            SelectionManager.getManager(serviceContext.getUserSession());

        synchronized (selectionManager.getSelection("metadata")) {
            return selectionManager.getSelection(SelectionManager.SELECTION_METADATA);
        }
    }

    /**
     * @deprecated Use the service with JSON format as output
     */
    @RequestMapping(value = "/{portal}/{lang}/metadata.select",
        produces = {MediaType.APPLICATION_XML_VALUE})
    public
    @ResponseBody
    SelectServiceResponse selectAsXML(@RequestParam(required = false) String[] id,
                                      @RequestParam(required = false) String selected,
                                      @RequestParam(required = false, defaultValue = "metadata") String type)
        throws Exception {
        ServiceContext serviceContext = ServiceContext.get();

        int nbSelected = SelectionManager.updateSelection(type,
            serviceContext.getUserSession(),
            selected, id != null ? Arrays.asList(id) : null,
            serviceContext);

        return new SelectServiceResponse().setSelected("" + nbSelected);
    }


}
