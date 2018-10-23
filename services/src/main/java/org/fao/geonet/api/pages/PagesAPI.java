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
package org.fao.geonet.api.pages;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.page.Page;
import org.fao.geonet.domain.page.PageIdentity;
import org.fao.geonet.repository.page.PageRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.UserSession;
import springfox.documentation.annotations.ApiIgnore;

@RequestMapping(value = { "/api/pages", "/api/" + API.VERSION_0_1 + "/pages" })
@Api(value = "pages", tags = "pages",
description = "Static pages inside GeoNetwork")
@Controller("pages")
public class PagesAPI {

    // HTTP status messages not from ApiParams
    private static final String PAGE_OK="Page found";
    private static final String PAGE_NOT_FOUND="Page not found";
    private static final String PAGE_DUPLICATE="Page already in the system: use PUT";
    private static final String PAGE_SAVED="Page saved";
    private static final String PAGE_UPDATED="Page changes saved";
    private static final String PAGE_DELETED="Page removed";
    private static final String ERROR_FILE="File not valid";

    // WRITE, EDIT, DELETE, READ Page methods

    @ApiOperation(
            value = "Add a new Page object in DRAFT section in status HIDDEN",
            notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>",
            nickname = "addPage")
    @RequestMapping(
            value = "/",
            method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = PAGE_SAVED),
            @ApiResponse(code = 404, message = PAGE_NOT_FOUND),
            @ApiResponse(code = 409, message = PAGE_DUPLICATE),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT),
            @ApiResponse(code = 500, message = ERROR_FILE)
    })
    @PreAuthorize("hasRole('Administrator')")
    public void addPage(
            @RequestParam(value = "language", required=true) final String language,
            @RequestParam(value = "pageId", required=true) final String pageId,
            @RequestParam(value = "data", required=false) MultipartFile data,
            @RequestParam(value = "format", required=true) final Page.PageFormat format,
            @ApiIgnore final HttpServletResponse response
            ) {

        final ApplicationContext appContext = ApplicationContextHolder.get();
        PageRepository pageRepository = appContext.getBean(PageRepository.class);

        if(pageRepository.findOne(new PageIdentity(language, pageId)) == null) {

            List<Page.PageSection> sections = new ArrayList<Page.PageSection>();
            sections.add(Page.PageSection.DRAFT);
            byte[] bytesData;
            try {
                bytesData = data.getBytes();
            } catch (Exception e) {
                response.setStatus(HttpStatus.CONFLICT.value());
                return;
            }

            Page page = new Page(new PageIdentity(language, pageId), bytesData, format, sections, Page.PageStatus.HIDDEN);

            pageRepository.save(page);
        } else {
            response.setStatus(HttpStatus.CONFLICT.value());
        }
    }

    // Isn't done with PUT because the multipart support as used by Spring
    // doesn't support other request method then POST
    @ApiOperation(
            value = "Edit a Page object",
            notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>",
            nickname = "editPage")
    @RequestMapping(
            value = "/{language}/{pageId}",
            method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = PAGE_UPDATED),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasRole('Administrator')")
    public void editPage(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "pageId") final String pageId,
            @RequestParam(value = "data", required=false) MultipartFile data,
            @RequestParam(value = "format", required=true) final Page.PageFormat format,
            @ApiIgnore final HttpServletResponse response
            ) {
        final ApplicationContext appContext = ApplicationContextHolder.get();
        PageRepository pageRepository = appContext.getBean(PageRepository.class);

        Page page = pageRepository.findOne(new PageIdentity(language, pageId));

        if(page == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        byte[] bytesData;
        try {
            bytesData = data.getBytes();
        } catch (Exception e) {
            response.setStatus(HttpStatus.CONFLICT.value());
            return;
        }

        page.setData(bytesData);
        page.setFormat(format);

        pageRepository.save(page);
    }

    @ApiOperation(
            value = "Delete a Page object",
            notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>",
            nickname = "deletePage")
    @RequestMapping(
            value = "/{language}/{pageId}",
            method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = PAGE_DELETED),
            @ApiResponse(code = 404, message = PAGE_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasRole('Administrator')")
    public void deletePage(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "pageId") final String pageId,
            @RequestParam(value = "format", required=true) final Page.PageFormat format,
            @ApiIgnore final HttpServletResponse response
            ) {
        final ApplicationContext appContext = ApplicationContextHolder.get();
        PageRepository pageRepository = appContext.getBean(PageRepository.class);

        Page page = pageRepository.findOne(new PageIdentity(language, pageId));

        if(page == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        pageRepository.delete(new PageIdentity(language, pageId));        
    }

    @ApiOperation(
            value = "Return the page object",
            notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>",
            nickname = "getPage")
    @RequestMapping(
            value = "/{language}/{pageId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = PAGE_OK),
            @ApiResponse(code = 404, message = PAGE_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    public ResponseEntity<Page> getPage(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "pageId") final String pageId,
            @ApiIgnore final HttpServletResponse response,
            @ApiIgnore final HttpSession session
            ) {
        final ApplicationContext appContext = ApplicationContextHolder.get();
        PageRepository pageRepository = appContext.getBean(PageRepository.class);

        Page page = pageRepository.findOne(new PageIdentity(language, pageId));

        if(page == null) {
            return new ResponseEntity<Page>(HttpStatus.NOT_FOUND);
        } else {
            UserSession us = ApiUtils.getUserSession(session);
            if(page.getStatus().equals(Page.PageStatus.HIDDEN) && us.getProfile()!= Profile.Administrator) {
                return new ResponseEntity<Page>(HttpStatus.FORBIDDEN);
            } else if(page.getStatus().equals(Page.PageStatus.PRIVATE) && (us.getProfile()== null || us.getProfile()== Profile.Guest)) {
                return new ResponseEntity<Page>(HttpStatus.FORBIDDEN);
            } else {
                return new ResponseEntity<Page>(page, HttpStatus.OK);
            }
        }
    }

    @ApiOperation(
            value = "Return the static html content identified by pageId",
            notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>",
            nickname = "getPage")
    @RequestMapping(
            value = "/{language}/{pageId}/data",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_HTML_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = PAGE_OK),
            @ApiResponse(code = 404, message = PAGE_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    public ResponseEntity<String> getPageContent(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "pageId") final String pageId,
            @ApiIgnore final HttpServletResponse response,
            @ApiIgnore final HttpSession session
            ) {
        final ApplicationContext appContext = ApplicationContextHolder.get();
        PageRepository pageRepository = appContext.getBean(PageRepository.class);

        Page page = pageRepository.findOne(new PageIdentity(language, pageId));

        if(page == null) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        } else {
            UserSession us = ApiUtils.getUserSession(session);
            if(page.getStatus().equals(Page.PageStatus.HIDDEN) && us.getProfile()!= Profile.Administrator) {
                return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
            } else if(page.getStatus().equals(Page.PageStatus.PRIVATE) && (us.getProfile()== null || us.getProfile()== Profile.Guest)) {
                return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
            } else {
                String content = "";
                try {
                    content = new String(page.getData(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    content = new String(page.getData());
                }

                return new ResponseEntity<String>(content, HttpStatus.OK);
            }
        }
    }


    // SECTION OPERATIONS methods

    @ApiOperation(
            value = "Adds the page to a section. This means that the link to the page will be shown in the list associated to that section.",
            notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>",
            nickname = "addPageToSection")
    @RequestMapping(
            value = "/{language}/{pageId}/{section}",
            method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = PAGE_UPDATED),
            @ApiResponse(code = 404, message = PAGE_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasRole('Administrator')")
    public void addPageToSection(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "pageId") final String pageId,
            @PathVariable(value = "section") final Page.PageSection section,
            @ApiIgnore final HttpServletResponse response
            ) {
        final ApplicationContext appContext = ApplicationContextHolder.get();
        PageRepository pageRepository = appContext.getBean(PageRepository.class);

        Page page = pageRepository.findOne(new PageIdentity(language, pageId));

        if(page == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        Page.PageSection sectionToAdd = section;

        if(sectionToAdd.equals(Page.PageSection.ALL)) {
            page.setSections(new ArrayList<Page.PageSection>());
            page.getSections().add(sectionToAdd);
        } else if(!page.getSections().contains(sectionToAdd)) {
            page.getSections().add(sectionToAdd);
        }

        pageRepository.save(page);
    }

    @ApiOperation(
            value = "Removes the page from a section. This means that the link to the page will not be shown in the list associated to that section.",
            notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>",
            nickname = "removePageFromSection")
    @RequestMapping(
            value = "/{language}/{pageId}/{section}",
            method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = PAGE_UPDATED),
            @ApiResponse(code = 404, message = PAGE_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasRole('Administrator')")
    public void removePageFromSection(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "pageId") final String pageId,
            @PathVariable(value = "section") final Page.PageSection section,
            @ApiIgnore final HttpServletResponse response
            ) {
        final ApplicationContext appContext = ApplicationContextHolder.get();
        PageRepository pageRepository = appContext.getBean(PageRepository.class);

        Page page = pageRepository.findOne(new PageIdentity(language, pageId));

        if(page == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        if(section.equals(Page.PageSection.ALL)) {
            page.setSections(new ArrayList<Page.PageSection>());
            page.getSections().add(Page.PageSection.DRAFT);
        } else if(section.equals(Page.PageSection.DRAFT)) {
            // Cannot remove a page from DRAFT section
        } else if(page.getSections().contains(section)) {
            page.getSections().remove(section);
        }

        pageRepository.save(page);
    }

    // STATUS OPERATION methods

    @ApiOperation(
            value = "Removes the page from a section. This means that the link to the page will not be shown in the list associated to that section.",
            notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>",
            nickname = "removePageFromSection")
    @RequestMapping(
            value = "/{language}/{pageId}/{status}",
            method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = PAGE_UPDATED),
            @ApiResponse(code = 404, message = PAGE_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasRole('Administrator')")
    public void changePageStatus(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "pageId") final String pageId,
            @PathVariable(value = "status") final Page.PageStatus status,
            @ApiIgnore final HttpServletResponse response
            ) {

        final ApplicationContext appContext = ApplicationContextHolder.get();
        PageRepository pageRepository = appContext.getBean(PageRepository.class);

        Page page = pageRepository.findOne(new PageIdentity(language, pageId));

        if(page == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        page.setStatus(status);

        pageRepository.save(page);
    }

    // LIST PAGES methods

    @ApiOperation(
            value = "List all pages according to the filters",
            notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>",
            nickname = "listPages")
    @RequestMapping(
            value = "/list",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    public ResponseEntity<List<Page>> listPages(
            @RequestParam(value = "language", required = false) final String language,
            @RequestParam(value = "section", required = false) final Page.PageSection section,
            @RequestParam(value = "format", required = false) final Page.PageFormat format,
            @ApiIgnore final HttpServletResponse response,
            @ApiIgnore final HttpSession session
            ) {
        final ApplicationContext appContext = ApplicationContextHolder.get();
        PageRepository pageRepository = appContext.getBean(PageRepository.class);

        UserSession us = ApiUtils.getUserSession(session);
        List<Page> unfilteredResult = null;

        if(language == null) {
            unfilteredResult = pageRepository.findAll();
        } else {
            unfilteredResult = pageRepository.findByPageIdentityLanguage(language);
        }

        List<Page> filteredResult = new ArrayList<Page>();

        for (Page page : unfilteredResult) {
            if((page.getStatus().equals(Page.PageStatus.HIDDEN) && us.getProfile()== Profile.Administrator) ||
                    (page.getStatus().equals(Page.PageStatus.PRIVATE) && us.getProfile()!= null && us.getProfile()!= Profile.Guest ) ||
                    page.getStatus().equals(Page.PageStatus.PUBLIC)) {
                if(section == null || Page.PageSection.ALL.equals(section)) {
                    filteredResult.add(page);
                } else {
                    List<Page.PageSection> sections = page.getSections();
                    boolean containsALL = sections.contains(Page.PageSection.ALL);
                    boolean containsRequestedSection = sections.contains(section);
                    if(containsALL || containsRequestedSection) {
                        filteredResult.add(page);
                    }
                }
            }
        }

        return new ResponseEntity<List<Page>>(filteredResult, HttpStatus.OK);
    }

}

