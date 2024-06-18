package org.fao.geonet.web;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.SourceType;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.annotation.RegEx;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

public class DatahubFilter implements Filter {
    private static final Logger log = Logger.getLogger(DatahubFilter.class);

    @Override
    public void init(FilterConfig config) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        ServletContext context = req.getServletContext();

        String reqPath = req.getPathInfo();
        if (reqPath == null || !reqPath.matches("^/[a-zA-Z0-9_\\-]+/datahub.*")) {
            chain.doFilter(request, res);
            return;
        }

        String isDatahubEnabled = getSettingManager().getValue(Settings.GEONETWORK_UI_DATAHUB_ENABLED);
        if (!isDatahubEnabled.equals("true")) {
            res.setStatus(404);
            return;
        }

        // a req path will be "/srv/datahub/bla/bla"
        String[] parts = reqPath.split("/");
        String portalName = parts[1]; // element at i=0 is empty

        SourceRepository sourceRepository = getSourceRepository();
        Boolean datahubEnabled = false;
        if (NodeInfo.DEFAULT_NODE.equals(portalName)) {
            datahubEnabled = getSettingManager().getValue(Settings.GEONETWORK_UI_DATAHUB_ENABLED).equals("true");
        } else if (sourceRepository.existsByUuidAndType(portalName, SourceType.subportal)) {
            datahubEnabled = sourceRepository.findOneByUuid(portalName).getDatahubEnabled();
        }

        if (!datahubEnabled) {
            res.setStatus(404);
            return;
        }

        String filePath = Stream.of(parts).skip(2).collect(Collectors.joining("/"));
        File actualFile = new File(context.getRealPath("/" + filePath));

        // fallback to index.html if the file doesn't exist
        if (!actualFile.exists()) {
            actualFile = new File(context.getRealPath("/datahub/index.html"));

            // disable cache only for index.html
            res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
            res.setHeader(HttpHeaders.PRAGMA, "no-cache");
            res.setHeader(HttpHeaders.EXPIRES, "0");
        }

        res.setStatus(200);
        String extension = FilenameUtils.getExtension(actualFile.getName()).toLowerCase();
        String contentType;
        if (extension.equals("js")) {
            contentType = "text/javascript; charset=UTF-8";
        } else {
            contentType = Files.probeContentType(actualFile.toPath());
        }
        res.setContentType(contentType);

        InputStream inStream;

        // the config is read from the settings
        if (actualFile.getName().equals("default.toml")) {
            inStream = readConfiguration(portalName);
        } else {
            inStream = new FileInputStream(actualFile);
        }
        OutputStream outStream = res.getOutputStream();

        // handle gzip compression
        if(req.getHeader(HttpHeaders.ACCEPT_ENCODING).contains("gzip")) {
            res.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
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

        Toml toml = new Toml().read(configuration);
        Map<String, Object> tomlMap = toml.toMap();

        // Force the "gn4_api_url" field to a value including the portal name
        if (!tomlMap.containsKey("global")) {
            tomlMap.put("global", Map.of());
        }
        Map<String, String> globalSection = (Map<String, String>) tomlMap.get("global");
        globalSection.put("geonetwork4_api_url", "/geonetwork/" + portalName + "/api");

        TomlWriter tomlWriter = new TomlWriter();
        configuration = tomlWriter.write(tomlMap);
        return new ByteArrayInputStream(configuration.getBytes());
    }

    @Override
    public void destroy() {
    }

    private SettingManager getSettingManager() {
        return ApplicationContextHolder.get().getBean(SettingManager.class);
    }
    private SourceRepository getSourceRepository() {
        return ApplicationContextHolder.get().getBean(SourceRepository.class);
    }
}
