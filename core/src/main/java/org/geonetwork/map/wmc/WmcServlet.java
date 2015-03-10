package org.geonetwork.map.wmc;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.fao.geonet.Constants;
import org.geonetwork.http.proxy.util.RequestUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class WmcServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static final Logger LOGGER = Logger.getLogger(WmcServlet.class);

    private final String WMC_CONTENT_TYPE = "application/vnd.ogc.context+xml";

    private final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\" ?>";

    private static final String CREATE_URL = "/create.wmc";
    private static final String LOAD_URL = "/load.wmc";

    protected static final String TEMP_FILE_PREFIX = "geonetwork-wmc";
    protected static final String TEMP_FILE_SUFFIX = ".cml";
    private static final int TEMP_FILE_PURGE_SECONDS = 600;

    private File tempDir = null;

    /**
     * Map of temporary files.
     */
    private final Map<String, TempFile> tempFiles = new HashMap<String, TempFile>();

    @Override
    public void init() throws ServletException {
        //get rid of the temporary files that where present before the applet was started.
        File dir = getTempDir();
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; ++i) {
            File file = files[i];
            final String name = file.getName();
            if (name.startsWith(TEMP_FILE_PREFIX) &&
                    name.endsWith(TEMP_FILE_SUFFIX)) {
                deleteFile(file);
            }
        }
    }


    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        //do the routing in function of the actual URL
        final String additionalPath = httpServletRequest.getPathInfo();

        if (additionalPath.startsWith("/") && additionalPath.endsWith(TEMP_FILE_SUFFIX)) {
            getWMC(httpServletResponse, additionalPath.substring(1, additionalPath.length() - 4));
        } else {
            error(httpServletResponse, "Unknown method: " + additionalPath, 404);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        final String additionalPath = request.getPathInfo();
        final PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        response.setHeader("Cache-Control", "no-cache");


        // Method call: Create Web Map Context
        if (additionalPath.equals(CREATE_URL)) {
            TempFile tempFile = null;
            try {
                tempFile = doCreateWMCFile(request);
            } catch (Throwable e) {
                deleteFile(tempFile);
                out.write("{success: true, error: '" + e.getMessage() + "'}");
                return;
            }

            final String id = generateId(tempFile);
            out.write("{success: true, url: '" + getBaseUrl(request) + "/" + id + TEMP_FILE_SUFFIX + "'}");

            addTempFile(tempFile, id);

        // Method call: Load Web Map Context
        } else if (additionalPath.equals(LOAD_URL)) {

            response.setContentType("text/html");
            response.setHeader("Cache-Control", "no-cache");

            boolean isMultipart = ServletFileUpload.isMultipartContent(request);

            if (!isMultipart) {
                out.write("{success: false, error: 'Error loading WMC'}");
                out.flush();

                return;
            }

             try {
                // Create a factory for disk-based file items
                FileItemFactory factory = new DiskFileItemFactory();

                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);

                // Parse the request
                @SuppressWarnings("unchecked")
                List<FileItem> items = upload.parseRequest(request);


                // Process the uploaded items
                for (FileItem item : items) {
                    if (!item.isFormField()) {
                        byte[] data = item.get();

                        //create a temporary file that will contain the WMC
                        TempFile tempFile = new TempFile(File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX, getTempDir()));
                        final String id = generateId(tempFile);
                        try {
                            Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), Constants.ENCODING));
                            fw.write(new String(data, Constants.ENCODING));
                            fw.close();
                            out.write("{success: true, url: '" + getBaseUrl(request) + "/" + id + TEMP_FILE_SUFFIX + "'}");

                        } catch (IOException e) {
                            deleteFile(tempFile);
                            out.write("{success: false, error: '" + e.getMessage() + "'}");

                        }
                        addTempFile(tempFile, id);
                        
                        break;
                    }
                }

            } catch (Exception e) {
               out.write("{success: false, error: '" + e.getMessage() + "'}");
            }


        // Unknow method call
        } else {
            out.write("{success: false, error: 'Unknown method: '" + additionalPath + "'}");
            //error(response, "Unknown method: " + additionalPath, 404);
        }
    }

    /**
     * To get the WMC created previously.
     */
    protected void getWMC(HttpServletResponse httpServletResponse, String id) throws IOException {
        final File file;
        synchronized (tempFiles) {
            file = tempFiles.get(id);
        }
        if (file == null) {
            error(httpServletResponse, "File with id=" + id + " unknown", 404);
            return;
        }

        sendWmcFile(httpServletResponse, file);
    }

    protected void addTempFile(TempFile tempFile, String id) {
        synchronized (tempFiles) {
            tempFiles.put(id, tempFile);
        }
    }

    /**
     * Get the ID to use in function of the filename (filename without the prefix and the extension).
     */
    protected String generateId(File tempFile) {
        final String name = tempFile.getName();
        return name.substring(
                TEMP_FILE_PREFIX.length(),
                name.length() - TEMP_FILE_SUFFIX.length());
    }

    protected String getBaseUrl(HttpServletRequest httpServletRequest) {
        final String additionalPath = httpServletRequest.getPathInfo();
        String fullUrl = httpServletRequest.getParameter("url");
        if (fullUrl != null) {
            return fullUrl.replaceFirst(additionalPath + "$", "");
        } else {
            return httpServletRequest.getRequestURL().toString().replaceFirst(additionalPath + "$", "");
        }
    }

    /**
     * Do the actual work of creating the WMC temporary file.
     */
    protected TempFile doCreateWMCFile(HttpServletRequest httpServletRequest) throws IOException {

        //create a temporary file that will contain the WMC
        TempFile tempFile = new TempFile(File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX, getTempDir()));
        try {
            String wmcContent = RequestUtil.inputStreamAsString(httpServletRequest);
            wmcContent = XML_HEADER + "\n" + wmcContent;

            Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), Constants.ENCODING));
            fw.write(wmcContent);
            fw.close();

            return tempFile;
        } catch (IOException e) {
            deleteFile(tempFile);
            throw e;
        }
    }


    /**
     * copy the WMC into the output stream
     */
    protected void sendWmcFile(HttpServletResponse httpServletResponse, File tempFile) throws IOException {
        FileInputStream pdf = null;
        try {
            pdf = new FileInputStream(tempFile);
            final OutputStream response = httpServletResponse.getOutputStream();
            httpServletResponse.setContentType(WMC_CONTENT_TYPE);
            httpServletResponse.setHeader("Content-disposition", "attachment; filename=" + tempFile.getName());
            IOUtils.copy(pdf, response);
            response.close();
        } finally {
            if(pdf != null) {
                IOUtils.closeQuietly(pdf);
            }
        }
    }

    /**
     * Send an error XXX to the client with an exception
     */
    protected void error(HttpServletResponse httpServletResponse, String message, int code) {
        try {
            httpServletResponse.setContentType("text/plain");
            httpServletResponse.setStatus(code);
            PrintWriter out = httpServletResponse.getWriter();
            out.println("Error while generating WMC:");
            out.println(message);
            out.close();
            LOGGER.error("Error while generating WMC: " + message);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    /**
     * If the file is defined, delete it.
     */
    protected void deleteFile(File file) {
        if (file != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Deleting PDF file: " + file.getName());
            }
            if (!file.delete()) {
                LOGGER.warn("Cannot delete file:" + file.getAbsolutePath());
            }
        }
    }

    /**
     * Get and cache the temporary directory to use for saving the generated WMC files.
     */
    protected File getTempDir() {
        if (tempDir == null) {
            tempDir = (File) getServletContext().
                    getAttribute("javax.servlet.context.tempdir");
            LOGGER.debug("Using '" + tempDir.getAbsolutePath() + "' as temporary directory");
        }
        return tempDir;
    }

    /**
     * Will purge all the known temporary files older than TEMP_FILE_PURGE_SECONDS.
     */
    protected void purgeOldTemporaryFiles() {
        final long minTime = System.currentTimeMillis() - TEMP_FILE_PURGE_SECONDS * 1000L;
        synchronized (tempFiles) {
            Iterator<Map.Entry<String, TempFile>> it = tempFiles.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, TempFile> entry = it.next();
                if (entry.getValue().creationTime < minTime) {
                    deleteFile(entry.getValue());
                    it.remove();
                }
            }
        }
    }

    protected static class TempFile extends File {
        private static final long serialVersionUID = 1L;
        private final long creationTime;

        public TempFile(File tempFile) {
            super(tempFile.getAbsolutePath());
            creationTime = System.currentTimeMillis();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + (int) (creationTime ^ (creationTime >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            TempFile other = (TempFile) obj;
            if (creationTime != other.creationTime)
                return false;
            return true;
        }
        
    }
}
