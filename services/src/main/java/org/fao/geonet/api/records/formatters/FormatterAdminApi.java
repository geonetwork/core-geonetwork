//==============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.api.records.formatters;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FileUtils;
import org.apache.commons.jcs3.access.exception.InvalidArgumentException;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.fao.oaipmh.exceptions.BadArgumentException;
import org.jdom.Element;
import org.locationtech.jts.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.fao.geonet.api.ApiParams.API_CLASS_FORMATTERS_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_FORMATTERS_TAG;
import static org.fao.geonet.api.records.formatters.FormatterConstants.SCHEMA_PLUGIN_FORMATTER_DIR;
import static org.fao.geonet.api.records.formatters.FormatterConstants.VIEW_XSL_FILENAME;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * List all formatters
 *
 * @author jeichar
 */
@Tag(name = API_CLASS_FORMATTERS_TAG,
    description = API_CLASS_FORMATTERS_OPS)
@Controller("formattersList")
public class FormatterAdminApi extends AbstractFormatService {

    @Autowired
    GeonetworkDataDirectory dataDirectory;

    @Autowired
    SchemaManager schemaManager;

    private final static String[] extensions = {"properties", "xml", "xsl", "css", "js"};

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get formatter files"
    )
    @RequestMapping(
        method = GET,
        value = "/{portal}/api/formatters/{schema:.+}/{formatter}/files",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    @PreAuthorize("hasAuthority('UserAdmin')")
    public String getFormatterFiles(
        @PathVariable final String formatter,
        @PathVariable final String schema) throws Exception {

        Path schemaDir = null;
        if (schema != null && !"null".equals(schema)) {
            schemaDir = schemaManager.getSchemaDir(schema);
        }

        Path formatDir = getAndVerifyFormatDir(
            dataDirectory,
            Params.ID,
            formatter, schemaDir).toRealPath();

        Element result = new Element("bundleFiles");
        makeTree("", formatDir, result);

        return Xml.getJSON(result);
    }

    private void makeTree(String parentId, Path dir, Element result) throws IOException {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dir)) {
            for (Path file : files) {
                String name = URLEncoder.encode(file.getFileName().toString(), Constants.ENCODING);
                Element element;
                String id = parentId + "/" + file.getFileName();
                if (Files.isDirectory(file) && legalFile(file)) {
                    element = new Element("dir");
                    makeTree(id, file, element);
                    if (element.getChildren().size() > 0) {
                        element.setAttribute("leaf", "false");
                        element.setAttribute("text", file.getFileName().toString()).setAttribute("path", id).setAttribute("name", name);
                        result.addContent(element);
                    }
                } else if (isEditibleFileType(file) && legalFile(file)) {
                    element = new Element("file");
                    element.setAttribute("leaf", "true");
                    element.setAttribute("text", file.getFileName().toString()).setAttribute("path", id).setAttribute("name", name);
                    result.addContent(element);
                }
            }
        }
    }

    private boolean isEditibleFileType(Path f) {
        String fileName = f.getFileName().toString();
        for (String ext : extensions) {
            if (fileName.endsWith("." + ext)) return true;
        }
        return fileName.equalsIgnoreCase("README");
    }

    private boolean legalFile(Path f) throws IOException {
        return !f.getFileName().startsWith(".") && !Files.isHidden(f) && Files.isReadable(f) && Files.isWritable(f);
    }

    private void addFormatters(String schema, FormatterDataResponse response, Path root, Path file, boolean isSchemaPluginFormatter,
                               boolean publishedOnly)
        throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(file, IO.DIRECTORIES_FILTER)) {
            for (Path formatter : paths) {
                boolean add = true;
                if (FORMATTER_FILTER.accept(formatter)) {
                    ConfigFile config = new ConfigFile(formatter, true, null);
                    if (publishedOnly && !config.isPublished()) {
                        continue;
                    }

                    List<String> applicableSchemas = config.listOfApplicableSchemas();

                    if (!schema.equalsIgnoreCase("all") && !isSchemaPluginFormatter) {
                        if (!applicableSchemas.contains(schema)) {
                            add = false;
                        }
                    }

                    if (add) {
                        String path = root.relativize(formatter).toString().replace("\\", "/");
                        if (path.startsWith("/")) {
                            path = path.substring(1);
                        }
                        final FormatterData formatterData;
                        if (isSchemaPluginFormatter) {
                            formatterData = new FormatterData(schema, path);
                        } else {
                            formatterData = new FormatterData(null, path);
                        }
                        response.add(formatterData);
                    }
                } else {
                    addFormatters(schema, response, root, formatter, isSchemaPluginFormatter, publishedOnly);
                }
            }
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get formatters"
    )
    @RequestMapping(
        method = GET,
        value = "/{portal}/api/formatters",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    @PreAuthorize("hasAuthority('UserAdmin')")
    public FormatterDataResponse listFormatters(
        @RequestParam(required = false) final String id,
        @RequestParam(required = false) final String uuid,
        @RequestParam(defaultValue = "all") String schema,
        @RequestParam(defaultValue = "false") boolean pluginOnly,
        @RequestParam(defaultValue = "true") boolean publishedOnly
    ) throws Exception {
        final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        if (id != null || uuid != null) {
            try {
                loadMetadata(applicationContext.getBean(IMetadataUtils.class), Integer.parseInt(resolveId(id, uuid)));
            } catch (Throwable e) {
                // its ok.  just can't use metadata
            }
        }

        if (schema == null)
            schema = "all";

        schema = schema.trim();

        FormatterDataResponse response = new FormatterDataResponse();
        if (!pluginOnly) {
            Path userXslDir = applicationContext.getBean(GeonetworkDataDirectory.class).getFormatterDir();
            addFormatters(schema, response, userXslDir, userXslDir, false, publishedOnly);
        }

        final Set<String> schemas = applicationContext.getBean(SchemaManager.class).getSchemas();
        for (String schemaName : schemas) {
            if (schema.equals("all") || schema.equals(schemaName)) {
                final Path schemaDir = applicationContext.getBean(SchemaManager.class).getSchemaDir(schemaName);
                final Path formatterDir = schemaDir.resolve(SCHEMA_PLUGIN_FORMATTER_DIR);
                addFormatters(schemaName, response, formatterDir, formatterDir, true, publishedOnly);
            }
        }
        return response;
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Download a formatter as ZIP"
    )
    @RequestMapping(
        method = GET,
        value = "/{portal}/api/formatters/{schema:.+}/{formatter}",
        produces = {
            "application/zip"
        })
    @ResponseBody
    @PreAuthorize("hasAuthority('UserAdmin')")
    public void downloadFormatter(
        @PathVariable final String formatter,
        @PathVariable final String schema,
        @Parameter(hidden = true)
            HttpServletRequest request,
        @Parameter(hidden = true)
            HttpServletResponse response) throws Exception {
        Path schemaDir = null;
        if (schema != null && !"null".equals(schema)) {
            schemaDir = schemaManager.getSchemaDir(schema);
        }
        Path formatDir = getAndVerifyFormatDir(dataDirectory, Params.ID, formatter, schemaDir);

        Path tmpDir = null;
        Path zippedFile = null;
        try {
            tmpDir = Files.createTempDirectory("gn-formatters-");
            zippedFile = Files.createTempFile(tmpDir, formatter, ".zip");

            try (FileSystem zipFs = ZipUtil.createZipFs(zippedFile);
                 DirectoryStream<Path> paths = Files.newDirectoryStream(formatDir)) {
                Path root = zipFs.getRootDirectories().iterator().next();
                for (Path path : paths) {
                    IO.copyDirectoryOrFile(path, root, true);
                }
            }

            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format(
                "inline; filename=\"%s.zip\"",
                schema != null && !"null".equals(schema)
                    ? schema + "-" + formatter : formatter
            ));
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(Files.size(zippedFile)));
            FileUtils.copyFile(zippedFile.toFile(), response.getOutputStream());

        } catch (IOException e) {
            throw new RuntimeException(String.format(
                "Error occurred while trying to download formatter bundle %s/%s.",
                schema, formatter));
        }
        finally {
            if (zippedFile != null) {
                FileUtils.deleteQuietly(zippedFile.toFile());
            }
            if (tmpDir != null) {
                FileUtils.deleteQuietly(tmpDir.toFile());
            }
        }
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Delete a formatter"
    )
    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/{portal}/api/formatters/{schema:.+}/{formatter}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteFormatter(
        @PathVariable final String formatter,
        @PathVariable final String schema
    ) throws Exception {
        Path schemaDir = null;
        if (schema != null && !"null".equals(schema)) {
            schemaDir = schemaManager.getSchemaDir(schema);
        }
        Path formatDir = getAndVerifyFormatDir(
            dataDirectory, Params.ID, formatter, schemaDir);

        try {
            IO.deleteFileOrDirectory(formatDir);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format(
                "Error occured while trying to remove the formatter %s/%s. Incorrect ID?",
                schema, formatter));
        }
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Import a XSLT formatter (usually a zip named 'schema-formatter.zip')"
    )
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/{portal}/api/formatters",
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    public void addFormatter(
        @RequestParam("file")
            MultipartFile[] file
    ) throws Exception {
        for (MultipartFile f : file) {
            String fileName = f.getOriginalFilename();

            String fileWithoutExtension = fileName.substring(0, fileName.indexOf("."));
            String[] tokens = fileWithoutExtension.split("-");
            String schema = tokens.length == 2 ? tokens[0] : null;
            String formatter = tokens.length == 2 ? tokens[1] : fileWithoutExtension;

            FilePathChecker.verify(schema);
            FilePathChecker.verify(formatter);
            FilePathChecker.verify(fileName);

            checkLegalId(Params.ID, formatter);
            Path newBundle;
            if (schema != null && !"null".equals(schema)) {
                Path schemaDir = schemaManager.getSchemaDir(schema);
                newBundle = schemaDir.resolve(SCHEMA_PLUGIN_FORMATTER_DIR).resolve(formatter);
                if (Files.exists(newBundle)) {
                    throw new InvalidArgumentException(String.format(
                        "Formatter %s/%s already exists. Delete it first.",
                        schema, formatter
                    ));
                }
            } else {
                Path userXslDir = dataDirectory.getFormatterDir();
                newBundle = userXslDir.resolve(formatter);
            }

            Path uploadedFile = dataDirectory.getUploadDir().resolve(fileName);
            byte[] data = ByteStreams.toByteArray(f.getInputStream());
            Files.write(uploadedFile, data);

            try {
                Files.createDirectories(newBundle);

                try (FileSystem zipFs = ZipUtil.openZipFs(uploadedFile)) {
                    Path viewFile = findViewFile(zipFs);
                    if (viewFile == null) {
                        throw new BadArgumentException(
                            "A formatter zip file must contain a " + VIEW_XSL_FILENAME + " file as one of its root files");
                    }

                    Path viewXslContainerDir = null;
                    for (Path root : zipFs.getRootDirectories()) {
                        viewXslContainerDir = findViewXslContainerDir(root);
                        if (viewXslContainerDir != null) {
                            break;
                        }
                    }

                    if (viewXslContainerDir == null) {
                        throw new IllegalArgumentException(
                            uploadedFile + " does not have a view.xsl file within it");
                    }

                    IO.copyDirectoryOrFile(viewXslContainerDir, newBundle, false);
                } catch (IllegalArgumentException | UnsupportedOperationException e) {
                    handleRawXsl(uploadedFile, newBundle);
                } catch (Exception e) {
                    IO.deleteFileOrDirectory(newBundle);
                    throw e;
                }

                addOptionalFiles(newBundle);
            } finally {
                IO.deleteFile(uploadedFile, false, Geonet.FORMATTER);
            }
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get formatter file content"
    )
    @RequestMapping(
        method = GET,
        value = "/{portal}/api/formatters/{schema:.+}/{formatter}/files/{file:.+}",
        produces = {
            MediaType.TEXT_PLAIN_VALUE
        })
    @ResponseBody
    @PreAuthorize("hasAuthority('UserAdmin')")
    public String getFormatterFileContent(
        @PathVariable final String formatter,
        @PathVariable final String schema,
        @PathVariable final String file
    ) throws Exception {

        Path schemaDir = null;
        if (schema != null && !"null".equals(schema)) {
            schemaDir = schemaManager.getSchemaDir(schema);
        }

        FilePathChecker.verify(file);

        Path formatDir = getAndVerifyFormatDir(
            dataDirectory, Params.ID, formatter, schemaDir);

        // Check that the requested file is actually nested into formatDir
        String absformatDir = formatDir.toAbsolutePath().toString();
        String absFile = formatDir.resolve(file).toAbsolutePath().toString();
        if (!absFile.startsWith(absformatDir)) {
            throw new BadParameterEx("file", file);
        }

        Path filePath = formatDir.resolve(file);
        if (!Files.exists(filePath)) {
            throw new BadParameterEx("file", file);
        }

        return new String(Files.readAllBytes(formatDir.resolve(file)), Constants.ENCODING);
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Update formatter file"
    )
    @RequestMapping(
        value = "/{portal}/api/formatters/{schema:.+}/{formatter}/files/{file:.+}",
        produces = {
            MediaType.TEXT_PLAIN_VALUE
        },
        method = RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('UserAdmin')")
    public void updateFormatterFile(
        @PathVariable final String formatter,
        @PathVariable final String schema,
        @PathVariable final String file,
        @RequestParam(required = true)
            String data
    ) throws Exception {
        Path schemaDir = null;
        if (schema != null && !"null".equals(schema)) {
            schemaDir = schemaManager.getSchemaDir(schema);
        }

        Path formatDir = getAndVerifyFormatDir(
            dataDirectory, Params.ID, formatter, schemaDir);

        FilePathChecker.verify(file);
        Path toUpdate = formatDir.resolve(file);

        Files.write(toUpdate, Collections.singleton(data), Constants.CHARSET);
    }

    private Path findViewXslContainerDir(Path dir) throws IOException {
        if (Files.exists(dir.resolve(FormatterConstants.VIEW_XSL_FILENAME))) {
            return dir;
        }

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(dir, IO.DIRECTORIES_FILTER)) {
            for (Path childDir : paths) {
                Path container = findViewXslContainerDir(childDir);
                if (container != null) {
                    return container;
                }
            }
        }
        return null;
    }

    private Path findViewFile(FileSystem zipFs) throws IOException {
        Path rootView = zipFs.getPath(VIEW_XSL_FILENAME);
        if (Files.exists(rootView)) {
            return rootView;
        }
        final Path rootDir = zipFs.getRootDirectories().iterator().next();
        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(rootDir, IO.DIRECTORIES_FILTER)) {
            Iterator<Path> dirIter = dirs.iterator();
            if (dirIter.hasNext()) {
                Path next = dirIter.next();
                Assert.isTrue(!dirIter.hasNext(),
                    "The formatter/view zip file must either have a single root directory which contains the view file or " +
                        "it must have all formatter resources at the root of the directory");
                rootView = next.resolve(VIEW_XSL_FILENAME);
                if (Files.exists(rootView)) {
                    return rootView;
                }
            }
        }
        return null;
    }

    private void addOptionalFiles(Path file) throws IOException {
        ConfigFile.generateDefault(file);

        final Path locDir = file.resolve("loc");
        if (!Files.exists(locDir)) {
            Files.createDirectories(locDir);
            try (PrintStream out = new PrintStream(Files.newOutputStream(locDir.resolve("README")), true, Constants.ENCODING)) {
                out.println("If a formatter requires localization that cannot be found in strings or schema ");
                out.println("localization the format bundle can have a loc subfolder containing translations.");
                out.println();
                out.println("The xml document created will have the xml files from loc/<currentLoc>/ added to");
                out.println("xml documentation under the /root/resources tag.");
                out.println();
                out.println("If a localization folder is not found then the default language will be used.  ");
                out.println("if the default language also does not exist then the first localization will be used");
                out.println("but it is recommended to always have the default language localization");
                out.println("(unless language is fixed in the config.properties)");
            }
        }
    }

    private void handleRawXsl(Path uploadedFile, Path dir) throws IOException {
        Files.createDirectories(dir);
        IO.moveDirectoryOrFile(uploadedFile, dir.resolve(VIEW_XSL_FILENAME), false);
    }


    @XmlRootElement(name = "formatters")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class FormatterDataResponse implements Serializable {
        private static final long serialVersionUID = 8674269207113596010L;

        @XmlElement(name = "formatter")
        private final List<FormatterData> formatters = Lists.newArrayList();

        public void add(FormatterData formatterData) {
            this.formatters.add(formatterData);
        }

        @Override
        public String toString() {
            return "FormatterDataResponse{" + formatters + '}';
        }

        public List<FormatterData> getFormatters() {
            return formatters;
        }
    }

    @XmlRootElement(name = "formatter")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class FormatterData implements Serializable {
        private static final long serialVersionUID = 2015204126746590712L;
        @XmlElement(name = "schema")
        private final String schema;
        private final String id;

        public FormatterData(String schema, String id) {
            this.schema = schema;
            this.id = id;
        }

        @Override
        public String toString() {
            return "FormatterData{" + "schema ='" + schema + '\'' + ", id='" + id + '\'' + '}';
        }


        public String getSchema() {
            return schema;
        }

        public String getId() {
            return id;
        }
    }

}
