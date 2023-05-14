/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.exception.WebApplicationException;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.page.Page;
import org.fao.geonet.domain.page.PageIdentity;
import org.fao.geonet.repository.page.PageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@RequestMapping(value = {"/{portal}/api/pages"})
@Tag(name = "pages", description = "Static pages inside GeoNetwork")
@Controller("pages")
public class PagesAPI {

    private static final String PAGE_OK = "Page found";
    private static final String PAGE_NOT_FOUND = "Page not found";
    private static final String PAGE_DUPLICATE = "Page already in the system: use PUT";
    private static final String PAGE_SAVED = "Page saved";
    private static final String PAGE_UPDATED = "Page changes saved";
    private static final String PAGE_DELETED = "Page removed";
    private static final String ERROR_FILE = "File not valid";
    private static final String ERROR_CREATE = "Wrong parameters are provided";

    private final PageRepository pageRepository;

    private final LanguageUtils languageUtils;

    public PagesAPI(PageRepository pageRepository, LanguageUtils languageUtils) {
        this.pageRepository = pageRepository;
        this.languageUtils = languageUtils;
    }

    private static void setDefaultLink(Page page) {
        page.setLink(
            "../api/pages/"
                + page.getPageIdentity().getLanguage()
                + "/"
                + page.getPageIdentity().getLinkText()
                + "/content");
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add a page",
        description = "<p>Is not possible to load a link and a file at the same time.</p> <a href='https://geonetwork-opensource.org/manuals/4.0.x/en/customizing-application/adding-static-pages.html'>More info</a>")
    @PutMapping(
        consumes = {
            MediaType.APPLICATION_JSON_VALUE}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = PAGE_SAVED),
        @ApiResponse(responseCode = "400", description = ERROR_CREATE),
        @ApiResponse(responseCode = "409", description = PAGE_DUPLICATE),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT),
        @ApiResponse(responseCode = "500", description = ERROR_FILE)})
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    public ResponseEntity<String> addPageWithoutUploadingFile(
        @RequestBody
        PageProperties pageProperties)
        throws ResourceAlreadyExistException {
        return createPage(pageProperties, null);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add a page by uploading a file",
        description = "<p>Is not possible to load a link and a file at the same time.</p> <a href='https://geonetwork-opensource.org/manuals/4.0.x/en/customizing-application/adding-static-pages.html'>More info</a>")
    @PostMapping(
        consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = PAGE_SAVED),
        @ApiResponse(responseCode = "400", description = ERROR_CREATE),
        @ApiResponse(responseCode = "409", description = PAGE_DUPLICATE),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT),
        @ApiResponse(responseCode = "500", description = ERROR_FILE)})
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    public ResponseEntity<String> addPageUploadingFile(
        @RequestParam(value = "language", required = true) final String language,
        @RequestParam(value = "pageId", required = true) final String pageId,
        @RequestParam(value = "sections", required = false) final List<Page.PageSection> sections,
        @RequestParam(value = "status", required = false) final Page.PageStatus status,
        @RequestParam(value = "data", required = true) final MultipartFile data,
        @RequestParam(value = "format", required = false) Page.PageFormat format)
        throws ResourceAlreadyExistException {

        PageProperties page = new PageProperties();
        page.setPageId(pageId);
        page.setLanguage(language);
        page.setFormat(format);
        page.setStatus(status);
        page.setSections(sections);
        return createPage(page, data);
    }

    private ResponseEntity<String> createPage(PageProperties pageProperties,
                                            MultipartFile data) throws ResourceAlreadyExistException {

        String link = pageProperties.getLink();
        String content = pageProperties.getContent();
        List<Page.PageSection> section = pageProperties.getSections();
        Page.PageStatus status = pageProperties.getStatus();
        String language = pageProperties.getLanguage();
        String pageId = pageProperties.getPageId();
        Page.PageFormat format = pageProperties.getFormat();

        if (language != null) {
            checkValidLanguage(language);
        }

        if (!StringUtils.isBlank(link)) {
            format = Page.PageFormat.LINK;
        }

        checkMandatoryContent(data, link, content);
        checkUniqueContent(data, link, content);
        checkCorrectFormat(data, format);

        Optional<Page> page = pageRepository.findById(new PageIdentity(language, pageId));

        if (!page.isPresent()) {
            Page newPage = getEmptyHiddenDraftPage(pageProperties.getLanguage(), pageProperties.getPageId(), pageProperties.getLabel(), format);
            fillContent(data, link, content, newPage);

            if (section != null) {
                newPage.setSections(section);
            }

            if (status != null) {
                newPage.setStatus(status);
            }

            pageRepository.save(newPage);
            return ResponseEntity.status(HttpStatus.CREATED).body("{}");
        } else {
            throw new ResourceAlreadyExistException();
        }
    }

    private ResponseEntity<Void> updatePageInternal(@NotNull String language,
                                                    String pageId,
                                                    PageProperties pageProperties) throws ResourceNotFoundException, ResourceAlreadyExistException {

        String link = pageProperties.getLink();
        String content = pageProperties.getContent();
        String newLanguage = pageProperties.getLanguage();
        String newPageId = pageProperties.getPageId();
        Page.PageFormat format = pageProperties.getFormat();
        String newLabel = pageProperties.getLabel();

        checkValidLanguage(language);

        if (newLanguage != null) {
            checkValidLanguage(newLanguage);
        }

        checkMandatoryContent(null, link, content);
        checkUniqueContent(null, link, content);
        checkCorrectFormat(null, format);

        Optional<Page> page = pageRepository.findById(new PageIdentity(language, pageId));

        if (!page.isPresent()) {
            throw new ResourceNotFoundException("Can't update non existing page " + pageId + ".");
        }
        Page pageToUpdate = page.get();

        String updatedLanguage = StringUtils.isBlank(newLanguage) ? language : newLanguage;
        String updatedPageId = StringUtils.isBlank(newPageId) ? pageId : newPageId;

        boolean isChangingKey = !updatedLanguage.equals(language)
            && !updatedPageId.equals(pageId);

        if (isChangingKey) {
            checkValidLanguage(updatedLanguage);

            Optional<Page> newPage = pageRepository.findById(new PageIdentity(updatedLanguage, updatedPageId));
            if (newPage.isPresent()) {
                throw new ResourceAlreadyExistException();
            }
            PageIdentity newId = new PageIdentity(updatedLanguage, updatedPageId);
            Page pageCopy = new Page(newId, pageToUpdate.getData(),
                link != null ? link : pageToUpdate.getLink(),
                format != null ? format : pageToUpdate.getFormat(),
                pageProperties.getSections() != null ? pageProperties.getSections() : pageToUpdate.getSections(),
                pageProperties.getStatus() != null ? pageProperties.getStatus() : pageToUpdate.getStatus(),
                newLabel != null ? newLabel : pageToUpdate.getLabel());

            pageRepository.save(pageCopy);
            pageRepository.delete(pageToUpdate);
        } else {
            pageToUpdate.setFormat(format != null ? format : pageToUpdate.getFormat());
            fillContent(null, link, content, pageToUpdate);
            pageToUpdate.setSections(pageProperties.getSections() != null ? pageProperties.getSections() : pageToUpdate.getSections());
            pageToUpdate.setStatus(pageProperties.getStatus() != null ? pageProperties.getStatus() : pageToUpdate.getStatus());
            pageRepository.save(pageToUpdate);
        }

        return ResponseEntity.noContent().build();
    }

    // Isn't done with PUT because the multipart support as used by Spring
    // doesn't support other request method than POST
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Update a page",
        description = "<a href='https://geonetwork-opensource.org/manuals/4.0.x/en/customizing-application/adding-static-pages.html'>More info</a>")
    @PutMapping(
        value = "/{language}/{pageId}",
        consumes = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = PAGE_UPDATED),
            @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
        })
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<Void> updatePage(
        @PathVariable(value = "language") final String language,
        @PathVariable(value = "pageId") final String pageId,
        @RequestBody
        PageProperties pageProperties
    ) throws ResourceNotFoundException, ResourceAlreadyExistException {
        return updatePageInternal(language, pageId, pageProperties);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Delete a page",
        description = "<a href='https://geonetwork-opensource.org/manuals/4.0.x/en/customizing-application/adding-static-pages.html'>More info</a>")
    @DeleteMapping(
        value = "/{language}/{pageId}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = PAGE_DELETED),
        @ApiResponse(responseCode = "404", description = PAGE_NOT_FOUND),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    public void deletePage(
        @PathVariable(value = "language") final String language,
        @PathVariable(value = "pageId") final String pageId) throws ResourceNotFoundException {

        searchPage(language, pageId, pageRepository);
        pageRepository.deleteById(new PageIdentity(language, pageId));
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Return the page object details except the content",
        description = "<a href='https://geonetwork-opensource.org/manuals/4.0.x/en/customizing-application/adding-static-pages.html'>More info</a>")
    @GetMapping(
        value = "/{language}/{pageId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = PAGE_OK),
        @ApiResponse(responseCode = "404", description = PAGE_NOT_FOUND),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)})
    @ResponseBody
    public ResponseEntity<org.fao.geonet.api.pages.PageProperties> getPage(
        @PathVariable(value = "language") final String language,
        @PathVariable(value = "pageId") final String pageId,
        @Parameter(hidden = true) final HttpSession session) throws ResourceNotFoundException {

        Page page = searchPage(language, pageId, pageRepository);
        return checkPermissionsOnSinglePageAndReturn(session, page);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Return the static html content identified by pageId",
        description = "<a href='https://geonetwork-opensource.org/manuals/4.0.x/en/customizing-application/adding-static-pages.html'>More info</a>")
    @GetMapping(
        value = "/{language}/{pageId}/content")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = PAGE_OK),
        @ApiResponse(responseCode = "404", description = PAGE_NOT_FOUND),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)})
    @ResponseBody
    public ResponseEntity<String> getPageContent(
        @PathVariable(value = "language") final String language,
        @PathVariable(value = "pageId") final String pageId,
        @Parameter(hidden = true) final HttpSession session,
        @Parameter(hidden = true) final HttpServletResponse response) {


        final Optional<Page> page = pageRepository.findById(new PageIdentity(language, pageId));

        if (!page.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        final UserSession us = ApiUtils.getUserSession(session);
        if (page.get().getStatus().equals(Page.PageStatus.HIDDEN) && us.getProfile() != Profile.Administrator) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } else if (page.get().getStatus().equals(Page.PageStatus.PRIVATE) && (us.getProfile() == null || us.getProfile() == Profile.Guest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } else {
            String content;
            if (page.get().getData() != null && page.get().getData().length > 0) {
                content = new String(page.get().getData(), StandardCharsets.UTF_8);
            } else {
                content = page.get().getLink();
            }

            response.setHeader(CONTENT_TYPE,
                (page.get().getFormat().equals(Page.PageFormat.HTML)
                    || page.get().getFormat().equals(Page.PageFormat.HTMLPAGE)
                    ? MediaType.TEXT_HTML_VALUE : MediaType.TEXT_PLAIN_VALUE)
                    + "; charset=utf-8");
            return new ResponseEntity<>(content, HttpStatus.OK);
        }

    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "List all pages according to the filters",
        description = "<a href='https://geonetwork-opensource.org/manuals/4.0.x/en/customizing-application/adding-static-pages.html'>More info</a>")
    @GetMapping
    @ApiResponses(value = {
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)})
    @ResponseBody
    public ResponseEntity<List<org.fao.geonet.api.pages.PageProperties>> listPages(
        @RequestParam(value = "language", required = false) final String language,
        @RequestParam(value = "section", required = false) final Page.PageSection section,
        @RequestParam(value = "format", required = false) final Page.PageFormat format,
        @Parameter(hidden = true) final HttpSession session) {

        final UserSession us = ApiUtils.getUserSession(session);
        List<Page> unfilteredResult;

        if (language == null) {
            unfilteredResult = pageRepository.findAll();
        } else {
            unfilteredResult = pageRepository.findByPageIdentityLanguage(language);
        }

        final List<org.fao.geonet.api.pages.PageProperties> filteredResult = new ArrayList<>();

        for (final Page page : unfilteredResult) {
            if (page.getStatus().equals(Page.PageStatus.HIDDEN) && us.getProfile() == Profile.Administrator
                || page.getStatus().equals(Page.PageStatus.PRIVATE) && us.getProfile() != null && us.getProfile() != Profile.Guest
                || page.getStatus().equals(Page.PageStatus.PUBLIC)
                || page.getStatus().equals(Page.PageStatus.PUBLIC_ONLY) && !us.isAuthenticated()) {
                if (section == null) {
                    filteredResult.add(new org.fao.geonet.api.pages.PageProperties(page));
                } else {
                    final List<Page.PageSection> sections = page.getSections();
                    final boolean containsRequestedSection = sections.contains(section);
                    if (containsRequestedSection) {
                        filteredResult.add(new org.fao.geonet.api.pages.PageProperties(page));
                    }
                }
            }
        }

        return new ResponseEntity<>(filteredResult, HttpStatus.OK);
    }

    @GetMapping(
        value = "/config/formats",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = PAGE_OK)})
    @ResponseBody
    public Page.PageFormat[] getPageFormats() {
        return Page.PageFormat.values();
    }

    @GetMapping(
        value = "/config/sections",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = PAGE_OK)})
    @ResponseBody
    public Page.PageSection[] getPageSections() {
        return Page.PageSection.values();
    }

    @GetMapping(
        value = "/config/status",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = PAGE_OK)})
    @ResponseBody
    public Page.PageStatus[] getPageStatus() {
        return Page.PageStatus.values();
    }

    private void checkCorrectFormat(final MultipartFile data, final Page.PageFormat format) {
        if (Page.PageFormat.LINK.equals(format) && data != null && !data.isEmpty()) {
            throw new IllegalArgumentException("Wrong format. Cannot set format to LINK and upload a file.");
        }
    }

    private void checkMandatoryContent(final MultipartFile data, final String link, final String content) {
        if (StringUtils.isBlank(link)
            && (data == null || data.isEmpty())
            && StringUtils.isEmpty(content)) {
            throw new IllegalArgumentException("A content associated to the page is required, Use a link, a file or text content.");
        }
    }

    private void checkUniqueContent(final MultipartFile data, final String link, final String content) {
        int options = 0;
        if (StringUtils.isNotBlank(link) && StringUtils.isEmpty(content)) {
            options++;
        }
        if (data != null && !data.isEmpty()) {
            options++;
        }
        if (StringUtils.isNotEmpty(content)) {
            options++;
        }
        if (options > 1) {
            throw new IllegalArgumentException("Either add page content using a link, a file or text content.");
        }
    }

    private void checkFileType(final MultipartFile data) {
        if (data != null) {
            String extension = StringUtils.defaultIfBlank(FilenameUtils.getExtension(data.getOriginalFilename()), "");
            if (Arrays.stream(Page.PageExtension.values()).noneMatch(t -> t.name().equals(extension.toUpperCase()))) {
                throw new MultipartException(String.format(
                    "Unsupported file type (only %s are allowed).",
                    Arrays.toString(Page.PageExtension.values())));
            }
        }
    }

    private void checkValidLanguage(String language) {
        if (!languageUtils.getUiLanguages().contains(language)) {
            throw new IllegalArgumentException(
                String.format("Language value is not valid: %s. A valid application language is mandatory: %s.",
                    language,
                    String.join(",", languageUtils.getUiLanguages())));
        }
    }

    /**
     * Check permissions on single page and return.
     *
     * @param session the session
     * @param page    the page
     * @return the response entity
     */
    private ResponseEntity<org.fao.geonet.api.pages.PageProperties> checkPermissionsOnSinglePageAndReturn(final HttpSession session, final Page page) {
        if (page == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            final UserSession us = ApiUtils.getUserSession(session);
            if (page.getStatus().equals(Page.PageStatus.HIDDEN) && us.getProfile() != Profile.Administrator) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else if (page.getStatus().equals(Page.PageStatus.PRIVATE) && (us.getProfile() == null || us.getProfile() == Profile.Guest)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                return new ResponseEntity<>(new org.fao.geonet.api.pages.PageProperties(page), HttpStatus.OK);
            }
        }
    }

    /**
     * Search page.
     *
     * @param language       the language
     * @param pageId         the page id
     * @param pageRepository the page repository
     * @return the page
     * @throws ResourceNotFoundException the resource not found exception
     */
    private Page searchPage(final String language, final String pageId, final PageRepository pageRepository)
        throws ResourceNotFoundException {
        final Optional<Page> page = pageRepository.findById(new PageIdentity(language, pageId));

        if (!page.isPresent()) {
            throw new ResourceNotFoundException("Page " + pageId + " not found");
        }
        return page.get();
    }

    /**
     * @return An empty hidden draft Page
     */
    protected Page getEmptyHiddenDraftPage(final String language, final String pageId, final String label, final Page.PageFormat format) {
        final List<Page.PageSection> sections = new ArrayList<>();
        return new Page(new PageIdentity(language, pageId), null, null, format, sections, Page.PageStatus.HIDDEN, label);
    }

    /**
     * Set the page content
     */
    private void fillContent(final MultipartFile data,
                             final String link,
                             String content, final Page page) {
        byte[] bytesData;
        if (data != null && !data.isEmpty()) {
            checkFileType(data);
            try {
                bytesData = data.getBytes();
            } catch (final Exception e) {
                // Wrap into managed exception
                throw new WebApplicationException(e);
            }
            page.setData(bytesData);
            setDefaultLink(page);
        } else if (StringUtils.isNotEmpty(content)) {
            setDefaultLink(page);
            page.setData(content.getBytes());
        } else if (page.getData() == null) {
            // Check the link, unless it refers to a file uploaded to the page, that contains the original file name.
            if (StringUtils.isNotBlank(link) && !UrlUtils.isValidRedirectUrl(link)) {
                throw new IllegalArgumentException("The link provided is not valid");
            } else {
                page.setLink(link);
            }
        }

    }
}
