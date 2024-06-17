package org.fao.geonet.web;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

public class DatahubFilter implements Filter {
    private static final Logger log = Logger.getLogger(DatahubFilter.class);
//    final String PATH_TO_DIST_FOLDER = "/"; // this is relative to the context root

    @Override
    public void init(FilterConfig config) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        ServletContext context = req.getServletContext();

        String reqPath = req.getPathInfo();
        if (reqPath == null || !reqPath.startsWith("/srv/datahub")) {
            chain.doFilter(request, res);
            return;
        }

        // a req path will be "/srv/datahub/bla/bla"
        String[] parts = reqPath.split("/");
        String portalName = parts[1]; // element at i=0 is empty
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

        InputStream inStream = new FileInputStream(actualFile);
        OutputStream outStream = res.getOutputStream();

        // handle gzip compression
        if(req.getHeader(HttpHeaders.ACCEPT_ENCODING).contains("gzip")) {
            res.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
            outStream = new GZIPOutputStream(outStream);
        }

        IOUtils.copy(inStream, outStream);
        outStream.close();
    }

    @Override
    public void destroy() {
    }
}
