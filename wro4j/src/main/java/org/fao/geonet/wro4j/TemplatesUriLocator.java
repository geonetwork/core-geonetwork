package org.fao.geonet.wro4j;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.UriLocator;

import javax.servlet.ServletContext;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Converts a resource to a closure goog.addDependency statement.
 * <p/>
 * Created by fgravin on 2/17/14.
 */
public class TemplatesUriLocator implements UriLocator {

    public static final String URI_PREFIX = "template://";
    public static final String URI_PREFIX_HEADER = "template://header";
    public static final String URI_PREFIX_FOOTER = "template://footer";
    public static final String URI_LOCATOR_ID = "templateURILocator";
    public static final String SRC_MAIN_RESOURCES_FOLDER = "src/main/resources/";

    @Override
    public InputStream locate(String uri) throws IOException {
        StringBuilder javascript;
        if (uri.startsWith(URI_PREFIX_HEADER)) {
            javascript = getHeader();
        } else if (uri.startsWith(URI_PREFIX_FOOTER)) {
            javascript = getFooter();
        } else {
            javascript = new StringBuilder();
            final String realPath;
            final String path = uri.substring(URI_PREFIX.length());
            final ServletContext servletContext = Context.get().getServletContext();
            if (servletContext != null) {
                realPath = servletContext.getRealPath(path);
            } else {
                realPath = path;
            }

            // Check to avoid NullPointerException
            if (realPath == null) {
                return new ByteArrayInputStream(javascript.toString().getBytes(StandardCharsets.UTF_8));
            }

            // Recursively walk each folders and add each templates found.
            Path folderToVisit = Paths.get(realPath);
            if (Files.exists(folderToVisit)) {
                Files.walkFileTree(folderToVisit, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (!Files.isDirectory(file) && file.getFileName().toString().endsWith("html")) {
                            String fileFolder = file.getParent().toUri().toString();

                            String relativeFileFolderPath = "";
                            if (fileFolder.contains(SRC_MAIN_RESOURCES_FOLDER)) {
                                relativeFileFolderPath = fileFolder.substring(
                                    fileFolder.indexOf(SRC_MAIN_RESOURCES_FOLDER)
                                        + SRC_MAIN_RESOURCES_FOLDER.length() - 1
                                );
                            } else {
                                relativeFileFolderPath =
                                    fileFolder.substring(fileFolder.indexOf(path));
                            }
                            addFileToTemplateCache(javascript, "../.."
                                + relativeFileFolderPath
                                + file.getFileName(), file.toFile());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
        return new ByteArrayInputStream(javascript.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void addFileToTemplateCache(StringBuilder javascript, String s, File file) throws IOException {
        StringBuilder template = null;

        String sCurrentLine;
        template = new StringBuilder();
        final Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        try (BufferedReader br = new BufferedReader(reader)){
            while ((sCurrentLine = br.readLine()) != null) {
                template.append(sCurrentLine).append("\n");
            }

            // Build a template in a one line String for the JS templateCache
            String sTemplate = template.toString();
            // Remove all empty content between tags but not new lines
            sTemplate = sTemplate.replaceAll(">[ \\t\\r\\f]*<", "><");
            // Replace all successions of spaces, tabs ... by a single space
            sTemplate = sTemplate.replaceAll("\\s\\s+", " ");
            // The backslash (\) is an escape character in Javascript
            sTemplate = sTemplate.replace("\\", "\\\\");
            // Escape new lines
            sTemplate = sTemplate.replaceAll("\\n", "\\\\n");
            // Escape quotes
            sTemplate = sTemplate.replace("'", "\\'");
            javascript.append(
                String.format("$templateCache.put('%s', '%s');",
                    s,
                    sTemplate));
        }
    }

    private StringBuilder getHeader() {
        StringBuilder javascript = new StringBuilder();
        javascript.append("(function() {")
            .append("angular.module('gn').run(['$templateCache', function($templateCache) {");
        return javascript;
    }

    private StringBuilder getFooter() {
        StringBuilder javascript = new StringBuilder();
        javascript.append("}]);").append("})();");
        return javascript;
    }

    @Override
    public boolean accept(String uri) {
        return uri.startsWith(URI_PREFIX);
    }
}
