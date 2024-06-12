package org.fao.geonet.web;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;
import org.fao.geonet.utils.Log;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.zip.GZIPOutputStream;

public class DatahubFilter implements Filter {
    private static final Logger log = Logger.getLogger(DatahubFilter.class);
    final String PATH_TO_DIST_FOLDER = "/datahub"; // this is relative to the context root

    @Override
    public void init(FilterConfig config) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        ServletContext context = req.getServletContext();


        File fileFromDistFolder = null;
        String reqPath = req.getPathInfo();


        if (reqPath != null) {
            // compute actual file path from requested file in the URL
            String filePath = reqPath
                .replace("/static", "");
            fileFromDistFolder = new File(context.getRealPath(PATH_TO_DIST_FOLDER + filePath));
        }
        if (fileFromDistFolder != null && fileFromDistFolder.exists()) {
            res.setStatus(200);
            String extension = FilenameUtils.getExtension(fileFromDistFolder.getName()).toLowerCase();
            String contentType;
            if (extension.equals("js")) {
                contentType = "text/javascript; charset=UTF-8";
            } else {
                contentType = Files.probeContentType(fileFromDistFolder.toPath());
            }
            res.setContentType(contentType);

            // set headers for disabling cache
            res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
            res.setHeader(HttpHeaders.PRAGMA, "no-cache");
            res.setHeader(HttpHeaders.EXPIRES, "0");

            InputStream inStream = new FileInputStream(fileFromDistFolder);
            OutputStream outStream = res.getOutputStream();

            // handle gzip compression
            if(req.getHeader(HttpHeaders.ACCEPT_ENCODING).contains("gzip")) {
                res.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
                outStream = new GZIPOutputStream(outStream);
            }

            IOUtils.copy(inStream, outStream);
            outStream.close();

        } else {
            chain.doFilter(request, res);
        }
    }

    @Override
    public void destroy() {
    }
}
