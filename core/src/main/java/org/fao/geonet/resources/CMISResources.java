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

package org.fao.geonet.resources;

import jeeves.config.springutil.JeevesDelegatingFilterProxy;
import jeeves.server.context.ServiceContext;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.DocumentImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.commons.io.FilenameUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

public class CMISResources extends Resources {
    @Autowired
    CMISConfiguration CMISConfiguration;

    private Path resourceBaseDir = null;

    @Override
    public Path locateResourcesDir(final ServletContext context, final ApplicationContext applicationContext) {
        if (this.resourceBaseDir == null) {
            Path systemFullDir = applicationContext.getBean(GeonetworkDataDirectory.class).getSystemDataDir();
            Path resourceFullDir = applicationContext.getBean(GeonetworkDataDirectory.class).getResourcesDir();

            // If the metadata full dir is relative from the system dir then use system dir as the base dir.
            if (resourceFullDir.toString().startsWith(systemFullDir.toString())) {
                this.resourceBaseDir = systemFullDir;
            } else {
                // If the metadata full dir is an absolute folder then use that as the base dir.
                if (resourceFullDir.isAbsolute()) {
                    this.resourceBaseDir = resourceFullDir.getRoot();
                } else {
                    // use it as a relative url.
                    this.resourceBaseDir = Paths.get(".");
                }
            }

            if (this.resourceBaseDir.toString().equals(".")) {
                this.resourceBaseDir = Paths.get(CMISConfiguration.getBaseRepositoryPath()).resolve(resourceFullDir);
            } else {
                this.resourceBaseDir = Paths.get(CMISConfiguration.getBaseRepositoryPath()).resolve(this.resourceBaseDir.relativize(resourceFullDir));
            }
        }
        return this.resourceBaseDir;
    }

    private String getKey(final Path dir, final String name) {
        return getKey(dir.resolve(name));
    }

    private String getKey(final Path path) {

        // Get keyPath as a relative path from /.
        Path keyPath;
        if (path.startsWith(Paths.get("/"))) {
            keyPath = Paths.get("/").relativize(path);
        } else {
            keyPath = path;
        }


        if (resourceBaseDir != null) {
            // If it starts with resource folder then it is missing the basePath so add it.
            if (keyPath.startsWith(Paths.get(CMISConfiguration.getBaseRepositoryPath()).relativize(resourceBaseDir))) {
                keyPath = Paths.get(CMISConfiguration.getBaseRepositoryPath()).resolve(keyPath);
            } else {
                Path resourceDir = Paths.get(CMISConfiguration.getBaseRepositoryPath()).resolve(resourceBaseDir);
                // If it starts with the resource dir by not starting with a "/" then add the "/"
                if (keyPath.startsWith(Paths.get("/").relativize(resourceDir))) {
                    keyPath = Paths.get("/").resolve(keyPath);
                } else {
                    // If it does not start with resource folder then it is missing so add it.
                    if (!keyPath.startsWith(resourceDir)) {
                        keyPath = resourceDir.resolve(keyPath);
                    }
                }
            }
        }

        String key;
        // For windows it may be "\" in which case we need to change it to folderDelimiter which is normally "/"
        if (keyPath.getFileSystem().getSeparator().equals(CMISConfiguration.getFolderDelimiter())) {
            key = keyPath.toString();
        } else {
            key = keyPath.toString().replace(keyPath.getFileSystem().getSeparator(), CMISConfiguration.getFolderDelimiter());
        }
        // For Windows, the pathString may start with // so remove one if this is the case.
        if (key.startsWith("//")) {
            key = key.substring(1);
        }

        // Make sure the key that is returns starts with "/"
        if (key.startsWith(CMISConfiguration.getFolderDelimiter())) {
            return key;
        } else {
            return CMISConfiguration.getFolderDelimiter() + key;
        }
    }

    private Path getKeyPath(String key) {
        // Keypath should not reference the base path so it should be removed.
        return Paths.get(key.substring(CMISConfiguration.getBaseRepositoryPath().length()));
    }

    @Nullable
    @Override
    protected Path findImagePath(final String imageName, final Path logosDir) {
        String key = getKey(logosDir, imageName);
        if (imageName.indexOf('.') > -1) {
            if (CMISConfiguration.getClient().existsPath(key)) {
                return getKeyPath(key);
            } else {
                Log.warning(Geonet.RESOURCES,
                        String.format("Unable to locate image resource '%s'.", key));
            }
        } else {
            try {
                CmisObject cmisObject = CMISConfiguration.getClient().getObjectByPath(key);
                Folder folder = (Folder) cmisObject;

                OperationContext operationContext = CMISConfiguration.getClient().createOperationContext();
                ItemIterable<CmisObject> children = folder.getChildren(operationContext);

                for (CmisObject object : children) {
                    if (object instanceof Document) {
                        String ext = FilenameUtils.getExtension(object.getName());
                        if (IMAGE_EXTENSIONS.contains(ext.toLowerCase())) {
                            //Todo not sure if name will be correct.
                            return getKeyPath(((Document) object).getName());
                        }

                    }
                }
            } catch (CmisObjectNotFoundException e) {
                Log.warning(Geonet.RESOURCES,
                        String.format("Unable to locate image resource '%s'.", key));
            }
        }
        return null;
    }

    @Nullable
    @Override
    public ResourceHolder getImage(final ServiceContext context, final String imageName,
                                   final Path logosDir) {
        Path path = findImagePath(imageName, logosDir);
        if (path != null) {
            String key = getKey(path);
            return new CMISResourceHolder(key, false);
        } else {
            return null;
        }
    }

    @Override
    public ResourceHolder getWritableImage(final ServiceContext context, final String imageName,
                                           final Path logosDir) {
        return new CMISResourceHolder(getKey(logosDir, imageName), true);
    }

    @Override
    Pair<byte[], Long> loadResource(final Path resourcesDir, final ServletContext context,
                                    final Path appPath, final String filename, final byte[] defaultValue,
                                    final long loadSince) throws IOException {
        final Path file = locateResource(resourcesDir, context, appPath, filename);
        final String key = getKey(file, filename);
        try {
            final CmisObject object = CMISConfiguration.getClient().getObjectByPath(key);
            if (object != null) {
                final long lastModified = object.getLastModificationDate().toInstant().toEpochMilli();
                try (InputStream in = ((Document) object).getContentStream().getStream()) {
                    if (loadSince < 0 || lastModified > loadSince) {
                        byte[] content = new byte[(int) ((Document) object).getContentStreamLength()];
                        new DataInputStream(in).readFully(content);
                        return Pair.read(content, lastModified);
                    } else {
                        return Pair.read(defaultValue, loadSince);
                    }
                }
            } else {
                Log.info(Log.RESOURCES, "Error loading resource " + CMISConfiguration.getRepositoryId() + ":" + key);
            }
        } catch (CmisObjectNotFoundException e) {
            Log.warning(Geonet.RESOURCES,
                    String.format("Unable to locate resource '%s'.", key));
            // Ignore not found error.
        }
        return Pair.read(defaultValue, -1L);
    }

    @Override
    protected Path locateResource(@Nullable final Path resourcesDir, final ServletContext context,
                                  final Path appPath, @Nonnull String filename) throws IOException {
        if (filename.charAt(0) == '/' || filename.charAt(0) == '\\') {
            filename = filename.substring(1);
        }

        final String key;
        if (resourcesDir != null) {
            key = getKey(resourcesDir, filename);
        } else {
            key = CMISConfiguration.getFolderDelimiter() +  filename;
        }

        boolean keyExists=false;
        // Use getObjectByPath as it does caching while existsPath does not.
        try {
            CMISConfiguration.getClient().getObjectByPath(key);
            keyExists = true;
        } catch (CmisObjectNotFoundException e) {
            keyExists=false;
        }

        if (!keyExists) {
            Path webappCopy = null;
            if (context != null) {
                final String realPath = context.getRealPath(filename);
                if (realPath != null) {
                    webappCopy = IO.toPath(realPath);
                }
            }

            if (webappCopy == null) {
                webappCopy = appPath.resolve(filename);
            }
            if (!Files.isReadable(webappCopy)) {
                final ConfigurableApplicationContext applicationContext =
                        JeevesDelegatingFilterProxy.getApplicationContextFromServletContext(context);
                if (resourcesDir.equals(locateResourcesDir(context, applicationContext))) {
                    webappCopy = super.locateResourcesDir(context, applicationContext).resolve(filename);
                }
            }
            if (Files.isReadable(webappCopy)) {
                try (ResourceHolder holder = new CMISResourceHolder(key, true)) {
                    Log.info(Log.RESOURCES, "Copying " + webappCopy + " to " + holder.getPath() + " for resource " + key);
                    Files.copy(webappCopy, holder.getPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } else {

                final String suffix = FilenameUtils.getExtension(key);

                // find a different format and convert it to our desired format
                if (IMAGE_WRITE_SUFFIXES.contains(suffix.toLowerCase())) {
                    final String suffixless = FilenameUtils.removeExtension(key);
                    final String suffixlessKeyFilename = FilenameUtils.getName(suffixless);
                    final String suffixlessKeyFolder = getKey(Paths.get(FilenameUtils.getFullPath(suffixless)));

                    OperationContext oc = CMISConfiguration.getClient().createOperationContext();

                    try {
                        Folder resourceFolder = (Folder) CMISConfiguration.getClient().getObjectByPath(suffixlessKeyFolder, oc);
                        Map<String, Document> documentMap = getCmisObjectMap(resourceFolder, null, suffixlessKeyFilename);

                        for (Map.Entry<String,Document> entry : documentMap.entrySet()) {
                            Document object = entry.getValue();
                            String cmisFilePath = entry.getKey();
                            final String ext = FilenameUtils.getExtension(object.getName()).toLowerCase();
                            if (IMAGE_READ_SUFFIXES.contains(ext)) {
                                try (ResourceHolder in = new CMISResourceHolder(object.getName(), true);
                                     ResourceHolder out = new CMISResourceHolder(key, true)) {
                                    try (InputStream inS = IO.newInputStream(in.getPath());
                                         OutputStream outS = Files.newOutputStream(out.getPath())) {
                                        Log.info(Log.RESOURCES, "Converting " + cmisFilePath + " to " + key);
                                        BufferedImage image = ImageIO.read(inS);
                                        ImageIO.write(image, suffix, outS);
                                        break;
                                    } catch (IOException e) {
                                        if (context != null) {
                                            context.log("Unable to convert image from " + in.getPath() + " to " +
                                                    out.getPath(), e);
                                        } else {
                                            Log.warning(Log.RESOURCES, "Unable to convert image from " +
                                                    in.getPath() + " to " + out.getPath(), e);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (CmisObjectNotFoundException e) {
                        Log.warning(Geonet.RESOURCES,
                                String.format("Unable to locate resource folder '%s'.", suffixlessKeyFolder));
                        // Ignore not found error.
                    }
                }

            }
        }

        return getKeyPath(key);
    }

    @Override
    protected void addFiles(final DirectoryStream.Filter<Path> iconFilter, final Path webappDir,
                            final HashSet<Path> result) {

        // Don't use caching for this process.
        OperationContext oc = CMISConfiguration.getClient().createOperationContext();
        oc.setCacheEnabled(false);

        String keyFolder = getKey(webappDir);
        CmisObject cmisObject = CMISConfiguration.getClient().getObjectByPath(keyFolder, oc);
        Folder folder = (Folder) cmisObject;

        ItemIterable<CmisObject> children = folder.getChildren(oc);

        for (CmisObject object : children) {
            if (object instanceof Document) {
                final Path curPath = getKeyPath(((DocumentImpl) object).getPaths().get(0));
                try {
                    if (iconFilter.accept(curPath)) {
                        result.add(curPath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Nullable
    @Override
    public FileTime getLastModified(final Path resourcesDir, final ServletContext context,
                                    final Path appPath, final String filename) throws IOException {
        final Path file = locateResource(resourcesDir, context, appPath, filename);
        final String key = getKey(file);
        try {
            final CmisObject object = CMISConfiguration.getClient().getObjectByPath(key);
            return FileTime.from(object.getLastModificationDate().toInstant());
        } catch (CmisObjectNotFoundException e) {
            // Ignore not found error.
        }
        // key does not exist
        return null;
    }

    @Override
    public void deleteImageIfExists(final String image, final Path dir) {
        Path icon = findImagePath(image, dir);
        if (icon != null) {
            CMISConfiguration.getClient().deleteByPath(getKey(icon));
        }
    }

    private class CMISResourceHolder implements ResourceHolder {
        private final String key;
        private Path path = null;
        private Path tempFolderPath = null;
        private boolean writeOnClose = false;

        private CMISResourceHolder(final String key, boolean writeOnClose) {
            this.key = key;
            this.writeOnClose = writeOnClose;
        }

        @Override
        public Path getPath() {
            if (path != null) {
                return path;
            }
            final String[] splittedKey = key.split(CMISConfiguration.getFolderDelimiter());
            try {
                // Preserve filename by putting the files into a temporary folder and using the same filename.
                tempFolderPath = Files.createTempDirectory("gn-res-" + splittedKey[splittedKey.length - 2] + "-");
                tempFolderPath.toFile().deleteOnExit();
                path = tempFolderPath.resolve(splittedKey[splittedKey.length - 1]);

                try {
                    final CmisObject object = CMISConfiguration.getClient().getObjectByPath(key);
                    try (InputStream in = ((Document) object).getContentStream().getStream()) {
                        Files.copy(in, path,
                            StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (CmisObjectNotFoundException e) {
                    // As there is no cmis file, we will also remove the path if it exists so that the current file does not get saved on close.
                    if (writeOnClose && Files.exists(path)) {
                        Files.delete(path);
                    }
                }
            } catch (IOException e) {
                Log.error(Geonet.RESOURCES, String.format(
                    "Error getting path for resource '%s'.", key), e);
                throw new RuntimeException(e);
            }

            return path;
        }

        @Override
        public String getRelativePath() {
            return key;
        }

        @Override
        public FileTime getLastModifiedTime() {
            try {
                final CmisObject object = CMISConfiguration.getClient().getObjectByPath(key);
                return FileTime.from(object.getLastModificationDate().toInstant());
            } catch (CmisObjectNotFoundException e) {
                Log.error(Geonet.RESOURCES,
                    String.format("Unable to locate resource '%s'.", key), e);
                return null;
            }
        }

        @Override
        public void abort() {
            writeOnClose = false;
        }

        @Override
        public void close() throws IOException {
            if (path == null) {
                return;
            }
            try {
                if (writeOnClose && Files.isReadable(path)) {
                    // Don't use caching for this process.
                    OperationContext oc = CMISConfiguration.getClient().createOperationContext();
                    oc.setCacheEnabled(false);

                    // Split the filename and parent folder from the key.
                    int lastFolderDelimiterKeyIndex = key.lastIndexOf(CMISConfiguration.getFolderDelimiter());
                    String filenameKey = key.substring(lastFolderDelimiterKeyIndex + 1);
                    String parentKey = key.substring(0, lastFolderDelimiterKeyIndex);

                    Map<String, Object> properties = new HashMap<String, Object>();
                    properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
                    properties.put(PropertyIds.NAME, filenameKey);

                    InputStream stream = Files.newInputStream(path);
                    ContentStream contentStream = CMISConfiguration.getClient().getObjectFactory().createContentStream(key, Files.size(path), Files.probeContentType(path), stream);

                    if (CMISConfiguration.getClient().existsPath(key)) {
                        try {
                            // If the document is found then we are updating the existing document.
                            Document doc = (Document) CMISConfiguration.getClient().getObjectByPath(key, oc);
                            doc.updateProperties(properties, true);
                            doc.setContentStream(contentStream, true, true);

                            Log.info(Geonet.RESOURCES,
                                String.format("Updated resource '%s'. Current version '%s'.", key, doc.getVersionLabel()));
                        } catch (CmisPermissionDeniedException ex) {
                            Log.warning(Geonet.RESOURCES, String.format(
                                "No permissions to update resource '%s'.", key));
                            throw new NotAllowedException(String.format(
                                "No permissions to update resource '%s'.", key));
                        }
                    } else {
                        // Get parent folder.
                        try {
                            Folder parentFolder;
                            try {
                                parentFolder = (Folder) CMISConfiguration.getClient().getObjectByPath(parentKey, oc);
                            } catch (CmisObjectNotFoundException e) {
                                // Create parent folder if it does not exists.
                                ObjectId objectId = CMISConfiguration.getClient().createPath(parentKey, "cmis:folder");
                                parentFolder = (Folder) CMISConfiguration.getClient().getObject(objectId, oc);
                            }

                            Document doc = parentFolder.createDocument(properties, contentStream, VersioningState.MAJOR);

                            Log.info(Geonet.RESOURCES,
                                String.format("Added resource '%s'.", doc.getPaths().get(0)));
                        } catch (CmisPermissionDeniedException ex) {
                            Log.warning(Geonet.RESOURCES, String.format(
                                "No permissions to add resource '%s'.", key));
                            throw new NotAllowedException(String.format(
                                "No permissions to add resource '%s'.", key));
                        }
                    }
                }
            } finally {
                // Delete temporary file and folder.
                IO.deleteFileOrDirectory(tempFolderPath, true);
                path=null;
                tempFolderPath = null;
            }
        }
    }

    private Map<String, Document> getCmisObjectMap(Folder folder, String baseFolder, String suffixlessKeyFilename) {
        if (baseFolder == null) {
            baseFolder="";
        }
        Map<String, Document> documentMap = new HashMap<>();
        for (CmisObject cmisObject : folder.getChildren()) {
            if (cmisObject instanceof Folder) {
                documentMap.putAll(getCmisObjectMap((Folder)cmisObject, baseFolder + CMISConfiguration.getFolderDelimiter() + cmisObject.getName(), suffixlessKeyFilename));
                return documentMap;
            } else {
                if (cmisObject instanceof Document && cmisObject.getName().startsWith(suffixlessKeyFilename)) {
                    documentMap.put(baseFolder + CMISConfiguration.getFolderDelimiter()  + cmisObject.getName(), (Document)cmisObject);
                }
            }
        }
        return documentMap;
    }
}
