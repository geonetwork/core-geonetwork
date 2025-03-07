package org.fao.geonet.datahub;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.SourceType;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static org.fao.geonet.kernel.schema.SchemaPlugin.LOGGER_NAME;

@RequestMapping(value = { "/{geonetworkPath:[a-zA-Z0-9_\\-]+}" })
@Controller("datahub")
public class DatahubController {

    @Autowired
    SourceRepository sourceRepository;

    @GetMapping("/datahub")
    public RedirectView redirectDatahub(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        if (!uri.endsWith("/")) {
            uri += "/";
        }
        return new RedirectView(uri + "index.html");
    }

    @GetMapping("/{locale:[a-z]{2,3}}/datahub")
    public RedirectView redirectLocalizedDatahub(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        if (!uri.endsWith("/")) {
            uri += "/";
        }
        return new RedirectView(uri + "index.html");
    }

    @RequestMapping("/datahub/**")
    public void handleDatahubWithFilepath(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleDatahubRequest(request, response);
    }

    @RequestMapping("/{locale:[a-z]{2,3}}/datahub/**")
    public void handleLocalizedDatahubWithFilepath(HttpServletRequest request, HttpServletResponse response,
            @PathVariable String locale) throws IOException {
        handleDatahubRequest(request, response);
    }

    void handleDatahubRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String portalName = getPortalName(request);
        if (!isPortalDatahubEnabled(portalName)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File actualFile = getRequestedFile(request);
        if (!actualFile.exists()) {
            actualFile = getFallbackFile();
            disableCacheForIndex(response);
        }

        setResponseHeaders(response, actualFile);
        writeResponseContent(request, response, actualFile, portalName);
    }

    private String getPortalName(HttpServletRequest request) {
        String reqPath = request.getPathInfo();
        String[] parts = reqPath.split("/");
        return parts[1];
    }

    private boolean isPortalDatahubEnabled(String portalName) {
        if (NodeInfo.DEFAULT_NODE.equals(portalName)) {
            return true;
        } else if (sourceRepository.existsByUuidAndType(portalName, SourceType.subportal)) {
            return Objects.requireNonNull(sourceRepository.findOneByUuid(portalName)).getDatahubEnabled();
        }
        return false;
    }

    private File getRequestedFile(HttpServletRequest request) {
        String reqPath = request.getPathInfo();

        String filePath = Stream.of(reqPath.split("/datahub/")).skip(1).collect(Collectors.joining("/"));
        try {
            return FileUtils.getFileFromJar("/datahub/" + filePath);
        } catch (IOException e) {
            return new File(filePath);// return file doesn't exist in jar to go back to main menu
        }
    }

    private File getFallbackFile() {
        String indexPath = "/datahub/index.html";
        try {
            return FileUtils.getFileFromJar(indexPath);
        } catch (IOException e) {
            Log.error(LOGGER_NAME, e.getMessage());
            return new File(indexPath);
        }
    }

    private void disableCacheForIndex(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setHeader(HttpHeaders.EXPIRES, "0");
    }

    private void setResponseHeaders(HttpServletResponse response, File actualFile) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        String extension = actualFile.getName().toLowerCase();
        String contentType = extension.equals("js") ? "text/javascript; charset=UTF-8"
                : Files.probeContentType(actualFile.toPath());
        response.setContentType(contentType);
    }

    void writeResponseContent(HttpServletRequest request, HttpServletResponse response, File actualFile,
            String portalName) throws IOException {
        InputStream inStream = actualFile.getName().equals("default.toml") ? readConfiguration(portalName)
                : new FileInputStream(actualFile);
        OutputStream outStream = response.getOutputStream();

        if (request.getHeader(HttpHeaders.ACCEPT_ENCODING).contains("gzip")) {
            response.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
            outStream = new GZIPOutputStream(outStream);
        }

        IOUtils.copy(inStream, outStream);
        outStream.close();
    }

    InputStream readConfiguration(String portalName) {
        String configuration = getPortalConfiguration(portalName);
        configuration = configuration.replaceAll("\ngeonetwork4_api_url\\s?=.+", "\n")
                .replace("[global]", "[global]\ngeonetwork4_api_url = \"/geonetwork/" + portalName + "/api\"");
        return new ByteArrayInputStream(configuration.getBytes());
    }

    private String getPortalConfiguration(String portalName) {
        Source portal = Objects.requireNonNull(sourceRepository.findByType(SourceType.portal, null)).get(0);
        if (isNotDefaultPortal(portalName)) {
            portal = sourceRepository.findOneByUuid(portalName);
        }

        if (datahubConfigurationExist(portal)) {
            return portal.getDatahubConfiguration();
        } else {
            return this.getPortalConfiguration(NodeInfo.DEFAULT_NODE);
        }

    }

    private boolean isNotDefaultPortal(String portalName) {
        return !portalName.equals(NodeInfo.DEFAULT_NODE);
    }

    private boolean datahubConfigurationExist(Source portal) {
        return portal != null && !portal.getDatahubConfiguration().isEmpty();
    }
}
