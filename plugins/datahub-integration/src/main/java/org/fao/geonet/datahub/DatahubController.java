package org.fao.geonet.datahub;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.SourceType;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.utils.Log;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static org.fao.geonet.kernel.schema.SchemaPlugin.LOGGER_NAME;

@RequestMapping(value = {"/{geonetworkPath:[a-zA-Z0-9_\\-]+}"})
@Controller("datahub")
public class DatahubController {

    @RequestMapping("/datahub")
    public void handleDatahub(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleDatahubRequest(request, response);
    }

    @RequestMapping("/{locale:[a-z]{2,3}}/datahub")
    public void handleLocalizedDatahub(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleDatahubRequest(request, response);
    }

    @RequestMapping("/datahub/**")
    public void handleDatahubWithFilepath(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleDatahubRequest(request, response);
    }

    @RequestMapping("/{locale:[a-z]{2,3}}/datahub/**")
    public void handleLocalizedDatahubWithFilepath(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleDatahubRequest(request, response);
    }

    private HttpServletResponse handleDatahubRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Log.debug(LOGGER_NAME, "enter in datahub");

        if (!isDatahubEnabled()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return response;
        }

        String portalName = getPortalName(request);
        if (!isPortalDatahubEnabled(portalName)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return response;
        }

        File actualFile = getRequestedFile(request);
        if (!actualFile.exists()) {
            actualFile = getFallbackFile(request);
            disableCacheForIndex(response);
        }

        setResponseHeaders(response, actualFile);
        writeResponseContent(request, response, actualFile, portalName);

        return response;
    }

    private boolean isDatahubEnabled() {
        return Objects.equals(getSettingManager().getValue(Settings.GEONETWORK_UI_DATAHUB_ENABLED), "true");
    }

    private String getPortalName(HttpServletRequest request) {
        String reqPath = request.getPathInfo();
        String[] parts = reqPath.split("/");
        return parts[1];
    }

    private boolean isPortalDatahubEnabled(String portalName) {
        SourceRepository sourceRepository = getSourceRepository();
        if (NodeInfo.DEFAULT_NODE.equals(portalName)) {
            return isDatahubEnabled();
        } else if (sourceRepository.existsByUuidAndType(portalName, SourceType.subportal)) {
            return Objects.requireNonNull(sourceRepository.findOneByUuid(portalName)).getDatahubEnabled();
        }
        return false;
    }

    private File getRequestedFile(HttpServletRequest request) {
        ServletContext context = request.getServletContext();
        String reqPath = request.getPathInfo();
        String filePath = Stream.of(reqPath.split("/")).skip(2).collect(Collectors.joining("/"));
        return new File(context.getRealPath("/" + filePath));
    }

    private File getFallbackFile(HttpServletRequest request) {
        ServletContext context = request.getServletContext();
        return new File(context.getRealPath("/datahub/index.html"));
    }

    private void disableCacheForIndex(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setHeader(HttpHeaders.EXPIRES, "0");
    }

    private void setResponseHeaders(HttpServletResponse response, File actualFile) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        String extension = FilenameUtils.getExtension(actualFile.getName()).toLowerCase();
        String contentType = extension.equals("js") ? "text/javascript; charset=UTF-8" : Files.probeContentType(actualFile.toPath());
        response.setContentType(contentType);
    }

    private void writeResponseContent(HttpServletRequest request, HttpServletResponse response, File actualFile, String portalName) throws IOException {
        InputStream inStream = actualFile.getName().equals("default.toml") ? readConfiguration(portalName) : new FileInputStream(actualFile);
        OutputStream outStream = response.getOutputStream();

        if (request.getHeader(HttpHeaders.ACCEPT_ENCODING).contains("gzip")) {
            response.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
            outStream = new GZIPOutputStream(outStream);
        }

        IOUtils.copy(inStream, outStream);
        outStream.close();
    }
    private InputStream readConfiguration(String portalName) {
        String configuration = getSettingManager().getValue(Settings.GEONETWORK_UI_DATAHUB_CONFIGURATION);

        if (!portalName.equals(NodeInfo.DEFAULT_NODE)) {
            Source portal = getSourceRepository().findOneByUuid(portalName);
            if (portal != null && !portal.getDatahubConfiguration().isEmpty()) {
                configuration = portal.getDatahubConfiguration();
            }
        }

        // remove url & add new one
        configuration = configuration.replaceAll("\ngeonetwork4_api_url\\s?=.+", "\n")
            .replace("[global]", "[global]\ngeonetwork4_api_url = \"/geonetwork/" + portalName + "/api\"");
        return new ByteArrayInputStream(configuration.getBytes());
    }

    private SettingManager getSettingManager() {
        return ApplicationContextHolder.get().getBean(SettingManager.class);
    }
    private SourceRepository getSourceRepository() {
        return ApplicationContextHolder.get().getBean(SourceRepository.class);
    }

}
