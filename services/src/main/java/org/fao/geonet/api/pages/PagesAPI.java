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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.exception.WebApplicationException;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.page.Page;
import org.fao.geonet.domain.page.PageIdentity;
import org.fao.geonet.repository.page.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.UserSession;
import springfox.documentation.annotations.ApiIgnore;

@RequestMapping(value = { "/{portal}/api/pages", "/{portal}/api/" + API.VERSION_0_1 + "/pages" })
@Api(value = "pages", tags = "pages", description = "Static pages inside GeoNetwork")
@Controller("pages")
public class PagesAPI {

    @Autowired
    private PageRepository pageRepository;

    @ApiOperation(value = "Add a new Page object in DRAFT section in status HIDDEN", notes = "<p>Is not possible to load a link and a file at the same time.</p> <a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>", nickname = "addPage")
    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = PAGE_SAVED),
            @ApiResponse(code = 404, message = PAGE_NOT_FOUND),
            @ApiResponse(code = 400, message = ERROR_CREATE),
            @ApiResponse(code = 409, message = PAGE_DUPLICATE),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT),
            @ApiResponse(code = 500, message = ERROR_FILE) })
    @PreAuthorize("hasRole('Administrator')")
    public void addPage(
            @RequestParam(value = "language", required = true) final String language,
            @RequestParam(value = "pageId", required = true) final String pageId,
            @RequestParam(value = "data", required = false) final MultipartFile data,
            @RequestParam(value = "link", required = false) final String link,
            @RequestParam(value = "format", required = true) final Page.PageFormat format,
            @ApiIgnore final HttpServletResponse response) throws ResourceAlreadyExistException {


        checkMandatoryContent(data, link);

        checkUniqueContent(data, link);

        checkCorrectFormat(data, link, format);

        if (pageRepository.findOne(new PageIdentity(language, pageId)) == null) {

            Page page = getEmptyHiddenDraftPage(language, pageId, format);

            fillContent(data, link, page);

            pageRepository.save(page);

        } else {
            throw new ResourceAlreadyExistException();
        }
    }


    // Isn't done with PUT because the multipart support as used by Spring
    // doesn't support other request method then POST
    @ApiOperation(value = "Edit a Page content and format", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>", nickname = "editPage")
    @RequestMapping(value = "/{language}/{pageId}", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = PAGE_UPDATED),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @PreAuthorize("hasRole('Administrator')")
    public void editPage(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "pageId") final String pageId,
            @RequestParam(value = "data", required = false) final MultipartFile data,
            @RequestParam(value = "link", required = false) final String link,
            @RequestParam(value = "format", required = true) final Page.PageFormat format,
            @ApiIgnore final HttpServletResponse response) throws ResourceNotFoundException {


        checkUniqueContent(data, link);

        checkCorrectFormat(data, link, format);

        final Page page = searchPage(language, pageId, pageRepository);

        fillContent(data, link, page);

        page.setFormat(format);
        pageRepository.save(page);
    }


    @ApiOperation(value = "Edit a Page name and language", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>", nickname = "editPage")
    @RequestMapping(value = "/{language}/{pageId}", method = RequestMethod.PUT)
    @ApiResponses(value = { @ApiResponse(code = 200, message = PAGE_UPDATED),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @PreAuthorize("hasRole('Administrator')")
    public void editPageName(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "pageId") final String pageId,
            @RequestParam(value = "newLanguage", required = false) final String newLanguage,
            @RequestParam(value = "newPageId", required = false) final String newPageId,
            @ApiIgnore final HttpServletResponse response) throws ResourceNotFoundException, ResourceAlreadyExistException {

        final Page page = pageRepository.findOne(new PageIdentity(language, pageId));

        if (page == null) {
            throw new ResourceNotFoundException("Page " + pageId + " not found.");
        }

        if (!language.equals(newLanguage) || !pageId.equals(newPageId)) {
            String updatedLanguage = StringUtils.isBlank(newLanguage) ? language : newLanguage;
            String updatedPageId = StringUtils.isBlank(newPageId) ? pageId : newPageId;

            Page newPage = pageRepository.findOne(new PageIdentity(updatedLanguage, updatedPageId));
            if (newPage != null) {
                throw new ResourceAlreadyExistException();
            }

            PageIdentity newId = new PageIdentity(updatedLanguage, updatedPageId);
            newPage = new Page(newId, page.getData(), page.getLink(), page.getFormat(), page.getSections(), page.getStatus());

            newPage.setSections(new ArrayList<Page.PageSection>());

            for (Page.PageSection p : page.getSections()) {
                newPage.getSections().add(p);
            }

            pageRepository.save(newPage);
            pageRepository.delete(page);
        }
    }


    @ApiOperation(value = "Delete a Page object", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>", nickname = "deletePage")
    @RequestMapping(value = "/{language}/{pageId}", method = RequestMethod.DELETE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = PAGE_DELETED),
            @ApiResponse(code = 404, message = PAGE_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @PreAuthorize("hasRole('Administrator')")
    public void deletePage(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "pageId") final String pageId,
            @RequestParam(value = "format", required = true) final Page.PageFormat format,
            @ApiIgnore final HttpServletResponse response) throws ResourceNotFoundException {

        searchPage(language, pageId, pageRepository);

        pageRepository.delete(new PageIdentity(language, pageId));
    }


    @ApiOperation(value = "Return the page object details except the content", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>", nickname = "getPage")
    @RequestMapping(value = "/{language}/{pageId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = PAGE_OK),
            @ApiResponse(code = 404, message = PAGE_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW) })
    @ResponseBody
    public ResponseEntity<PageJSONWrapper> getPage(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "pageId") final String pageId,
            @ApiIgnore final HttpServletResponse response,
            @ApiIgnore final HttpSession session) {


        final Page page = pageRepository.findOne(new PageIdentity(language, pageId));

        return checkPermissionsOnSinglePageAndReturn(session, page);
    }


    @ApiOperation(value = "Return the static html content identified by pageId", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>", nickname = "getPage")
    @RequestMapping(value = "/{language}/{pageId}/content", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    @ApiResponses(value = { @ApiResponse(code = 200, message = PAGE_OK),
            @ApiResponse(code = 404, message = PAGE_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW) })
    @ResponseBody
    public ResponseEntity<String> getPageContent(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "pageId") final String pageId,
            @ApiIgnore final HttpServletResponse response,
            @ApiIgnore final HttpSession session) {


        final Page page = pageRepository.findOne(new PageIdentity(language, pageId));

        if (page == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            final UserSession us = ApiUtils.getUserSession(session);
            if (page.getStatus().equals(Page.PageStatus.HIDDEN) && us.getProfile() != Profile.Administrator) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else if (page.getStatus().equals(Page.PageStatus.PRIVATE) && (us.getProfile() == null || us.getProfile() == Profile.Guest)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                String content = "";
                if (page.getData() != null && page.getData().length > 0) {
                    try {
                        content = new String(page.getData(), "UTF-8");
                    } catch (final UnsupportedEncodingException e) {
                        content = new String(page.getData());
                    }
                } else {
                    content = page.getLink();
                }

                return new ResponseEntity<>(content, HttpStatus.OK);
            }
        }
    }


    @ApiOperation(value = "Adds the page to a section. This means that the link to the page will be shown in the list associated to that section.", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>", nickname = "addPageToSection")
    @RequestMapping(value = "/{language}/{pageId}/{section}", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = PAGE_UPDATED),
            @ApiResponse(code = 404, message = PAGE_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @PreAuthorize("hasRole('Administrator')")
    public void addPageToSection(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "pageId") final String pageId,
            @PathVariable(value = "section") final Page.PageSection section,
            @ApiIgnore final HttpServletResponse response) throws ResourceNotFoundException {

        final Page page = pageRepository.findOne(new PageIdentity(language, pageId));

        if (page == null) {
            throw new ResourceNotFoundException("Page " + pageId + " not found.");
        }

        final Page.PageSection sectionToAdd = section;

        if (sectionToAdd.equals(Page.PageSection.ALL)) {
            page.setSections(new ArrayList<Page.PageSection>());
            page.getSections().add(sectionToAdd);
        } else if (!page.getSections().contains(sectionToAdd)) {
            page.getSections().add(sectionToAdd);
        }

        pageRepository.save(page);
    }


    @ApiOperation(value = "Removes the page from a section. This means that the link to the page will not be shown in the list associated to that section.", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>", nickname = "removePageFromSection")
    @RequestMapping(value = "/{language}/{pageId}/{section}", method = RequestMethod.DELETE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = PAGE_UPDATED),
            @ApiResponse(code = 404, message = PAGE_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @PreAuthorize("hasRole('Administrator')")
    public void removePageFromSection(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "pageId") final String pageId,
            @PathVariable(value = "section") final Page.PageSection section,
            @ApiIgnore final HttpServletResponse response) throws ResourceNotFoundException {

        final Page page = pageRepository.findOne(new PageIdentity(language, pageId));

        if (page == null) {
            throw new ResourceNotFoundException("Page " + pageId + " not found.");
        }

        if (section.equals(Page.PageSection.ALL)) {
            page.setSections(new ArrayList<Page.PageSection>());
            page.getSections().add(Page.PageSection.DRAFT);
        } else if (section.equals(Page.PageSection.DRAFT)) {
            // Cannot remove a page from DRAFT section
        } else if (page.getSections().contains(section)) {
            page.getSections().remove(section);
        }

        pageRepository.save(page);
    }


    @ApiOperation(value = "Changes the status of a page.", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>", nickname = "removePageFromSection")
    @RequestMapping(value = "/{language}/{pageId}/{status}", method = RequestMethod.PUT)
    @ApiResponses(value = { @ApiResponse(code = 200, message = PAGE_UPDATED),
            @ApiResponse(code = 404, message = PAGE_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @PreAuthorize("hasRole('Administrator')")
    public void changePageStatus(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "pageId") final String pageId,
            @PathVariable(value = "status") final Page.PageStatus status,
            @ApiIgnore final HttpServletResponse response) throws ResourceNotFoundException {

        final Page page = pageRepository.findOne(new PageIdentity(language, pageId));

        if (page == null) {
            throw new ResourceNotFoundException("Page " + pageId + " not found.");
        }

        page.setStatus(status);

        pageRepository.save(page);
    }


    @ApiOperation(value = "List all pages according to the filters", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html'>More info</a>", nickname = "listPages")
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW) })
    @ResponseBody
    public ResponseEntity<List<PageJSONWrapper>> listPages(
            @RequestParam(value = "language", required = false) final String language,
            @RequestParam(value = "section", required = false) final Page.PageSection section,
            @RequestParam(value = "format", required = false) final Page.PageFormat format,
            @ApiIgnore final HttpServletResponse response,
            @ApiIgnore final HttpSession session) {

        final UserSession us = ApiUtils.getUserSession(session);
        List<Page> unfilteredResult = null;

        if (language == null) {
            unfilteredResult = pageRepository.findAll();
        } else {
            unfilteredResult = pageRepository.findByPageIdentityLanguage(language);
        }

        final List<PageJSONWrapper> filteredResult = new ArrayList<>();

        for (final Page page : unfilteredResult) {
            if (page.getStatus().equals(Page.PageStatus.HIDDEN) && us.getProfile() == Profile.Administrator
                    || page.getStatus().equals(Page.PageStatus.PRIVATE) && us.getProfile() != null && us.getProfile() != Profile.Guest
                    || page.getStatus().equals(Page.PageStatus.PUBLIC)
                    || page.getStatus().equals(Page.PageStatus.PUBLIC_ONLY) && !us.isAuthenticated()) {
                if (section == null || Page.PageSection.ALL.equals(section)) {
                    filteredResult.add(new PageJSONWrapper(page));
                } else {
                    final List<Page.PageSection> sections = page.getSections();
                    final boolean containsALL = sections.contains(Page.PageSection.ALL);
                    final boolean containsRequestedSection = sections.contains(section);
                    if (containsALL || containsRequestedSection) {
                        filteredResult.add(new PageJSONWrapper(page));
                    }
                }
            }
        }

        return new ResponseEntity<>(filteredResult, HttpStatus.OK);
    }


    /* Local Utility and constants */

    /**
     * Check correct format.
     *
     * @param data the data
     * @param link the link
     * @param format the format
     */
    private void checkCorrectFormat(final MultipartFile data, final String link, final Page.PageFormat format) {
        // Cannot set format to LINK and upload a file
        if (Page.PageFormat.LINK.equals(format) && data != null && !data.isEmpty()) {
            throw new IllegalArgumentException("Wrong format.");
        }

        // Cannot set a link without setting format to LINK
        if (!Page.PageFormat.LINK.equals(format) && !StringUtils.isBlank(link)) {
            throw new IllegalArgumentException("Wrong format.");
        }
    }
    
    /**
     * Check that link or a file is defined.
     *
     * @param data the data
     * @param link the link
     */
    private void checkMandatoryContent(final MultipartFile data, final String link) {
        // Cannot set both: link and file
        if (StringUtils.isBlank(link) && (data == null || data.isEmpty())) {
            throw new IllegalArgumentException("A content associated to the page, a link or a file, is mandatory.");
        }
    }

    /**
     * Check unique content.
     *
     * @param data the data
     * @param link the link
     */
    private void checkUniqueContent(final MultipartFile data, final String link) {
        // Cannot set both: link and file
        if (!StringUtils.isBlank(link) && data != null && !data.isEmpty()) {
            throw new IllegalArgumentException("A content associated to the page, a link or a file, is mandatory. But is not possible to associate both to the same page.");
        }
    }

    /**
     * Check file type.
     *
     * @param data the data
     */
    private void checkFileType(final MultipartFile data) {
        if (data != null) {
            String extension = FilenameUtils.getExtension(data.getOriginalFilename());
            final String[] supportedExtensions = { "html", "HTML", "txt", "TXT", "md", "MD" };

            if (!ArrayUtils.contains(supportedExtensions, extension)) {
                throw new MultipartException("Unsuppoted file type (only html, txt and md are allowed).");
            }
        }
    }

    /**
     * Check permissions on single page and return.
     *
     * @param session the session
     * @param page the page
     * @return the response entity
     */
    private ResponseEntity<PageJSONWrapper> checkPermissionsOnSinglePageAndReturn(final HttpSession session, final Page page) {
        if (page == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            final UserSession us = ApiUtils.getUserSession(session);
            if (page.getStatus().equals(Page.PageStatus.HIDDEN) && us.getProfile() != Profile.Administrator) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else if (page.getStatus().equals(Page.PageStatus.PRIVATE) && (us.getProfile() == null || us.getProfile() == Profile.Guest)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                return new ResponseEntity<>(new PageJSONWrapper(page), HttpStatus.OK);
            }
        }
    }

    /**
     * Search page.
     *
     * @param language the language
     * @param pageId the page id
     * @param pageRepository the page repository
     * @return the page
     * @throws ResourceNotFoundException the resource not found exception
     */
    private Page searchPage(final String language, final String pageId, final PageRepository pageRepository)
            throws ResourceNotFoundException {
        final Page page = pageRepository.findOne(new PageIdentity(language, pageId));

        if (page == null) {
            throw new ResourceNotFoundException("Page " + pageId + " not found");
        }
        return page;
    }


    /**
     *
     * @param language
     * @param pageId
     * @param format
     * @return An empty hidden draft Page
     */
    private Page getEmptyHiddenDraftPage(final String language, final String pageId, final Page.PageFormat format) {
        final List<Page.PageSection> sections = new ArrayList<>();
        sections.add(Page.PageSection.DRAFT);
        Page page = new Page(new PageIdentity(language, pageId), null, null, format, sections, Page.PageStatus.HIDDEN);
        return page;
    }


    /**
     * Set the content with file or with provided link
     *
     * @param data the file
     * @param link the link
     * @param page the page to set content
     */
    private void fillContent(final MultipartFile data, final String link, final Page page) {
        byte[] bytesData = null;
        if (data != null && !data.isEmpty()) {
            checkFileType(data);
            try {
                bytesData = data.getBytes();
            } catch (final Exception e) {
                // Wrap into managed exception
                throw new WebApplicationException(e);
            }
            page.setData(bytesData);
        }

        if (link != null && !UrlUtils.isValidRedirectUrl(link)) {
            throw new IllegalArgumentException("The link provided is not valid");
        } else {
            page.setLink(link);
        }
    }

    /* HTTP status messages not from ApiParams */

    private static final String PAGE_OK = "Page found";

    private static final String PAGE_NOT_FOUND = "Page not found";

    private static final String PAGE_DUPLICATE = "Page already in the system: use PUT";

    private static final String PAGE_SAVED = "Page saved";

    private static final String PAGE_UPDATED = "Page changes saved";

    private static final String PAGE_DELETED = "Page removed";

    private static final String ERROR_FILE = "File not valid";

    private static final String ERROR_CREATE = "Wrong parameters are provided";

}
