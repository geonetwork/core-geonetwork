//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
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
package org.fao.geonet.api.mapservers;

import org.apache.commons.io.FilenameUtils;
import org.fao.geonet.ZipUtil;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.LinkedList;
import java.util.zip.ZipFile;

/**
 * Instances of this class represent geographic files. A geographic file can be a ZIP file including
 * ESRI Shapefiles, GeoPackage, and GeoTIFF files, or an individual GeoTIFF file or individual GeoPackage file.
 *
 * @author Éric Lemoine, Camptocamp France SAS
 * @author Francois Prunayre
 */
public class GeoFile implements Closeable {
    private FileSystem zipFile = null;
    private Path file = null;
    private String format = null;
    private boolean _containsSld = false;
    private String _sldBody;

    /**
     * Constructs a <code>GeoFile</code> object from a <code>File</code> object.
     *
     * @param f the file from wich the <code>GeoFile</code> object is constructed
     * @throws java.io.IOException if an input/output exception occurs while opening a ZIP file
     */
    public GeoFile(Path f) throws IOException {
        file = f;
        try {
            if (FilenameUtils.getExtension(file.getFileName().toString())
                .equalsIgnoreCase("zip")) {
                zipFile = ZipUtil.openZipFs(file);
            } else {
                zipFile = null;
            }
        } catch (IOException | URISyntaxException e) {
            zipFile = null;
        }
    }

    private static String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1
        );
    }

    public static Boolean fileIsGeotif(String fileName) {
        String extension = getExtension(fileName);
        return extension.equalsIgnoreCase("tif")
            || extension.equalsIgnoreCase("tiff")
            || extension.equalsIgnoreCase("geotif")
            || extension.equalsIgnoreCase("geotiff");
    }
//
//	/**
//	 * Returns a file for a given layer, a ZIP file if the layer is a Shapefile,
//	 * a GeoTIFF file if the layer is a GeoTIFF.
//	 *
//	 * @param id
//	 *            the name of the layer, as returned by the getVectorLayer and
//	 *            getRasterLayers methods
//	 * @return the file
//	 * @throws java.io.IOException
//	 *             if an input/output exception occurs while constructing a ZIP
//	 *             file
//	 */
//	public File getLayerFile(String id) throws IOException {
//		File f = null;
//		if (zipFile != null) {
//			ZipOutputStream out = null;
//			byte[] buf = new byte[1024];
//			for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
//				ZipEntry ze = e.nextElement();
//				String baseName = getBase(ze.getName());
//				if (baseName.equals(id)) {
//					if (out == null) {
//						f = File.createTempFile("layer_", ".zip");
//						out = new ZipOutputStream(new FileOutputStream(f));
//					}
//					InputStream in = zipFile.getInputStream(ze);
//					out.putNextEntry(new ZipEntry(ze.getName()));
//					int len;
//					while ((len = in.read(buf)) > 0) {
//						out.write(buf, 0, len);
//					}
//					out.closeEntry();
//					in.close();
//				}
//			}
//			if (out != null) {
//				out.close();
//			}
//		} else {
//			f = file;
//		}
//		return f;
//	}

    public static Boolean fileIsECW(String fileName) {
        String extension = getExtension(fileName);
        return extension.equalsIgnoreCase("ecw");
    }

    public static Boolean fileIsRASTER(String fileName) {
        return fileIsGeotif(fileName) || fileIsECW(fileName);
    }

    /**
     * Returns the names of the vector layers (Shapefiles) in the geographic file.
     *
     * @param onlyOneFileAllowed Return exception if more than one shapefile found
     * @return a collection of layer names
     * @throws IllegalArgumentException If more than on shapefile is found and onlyOneFileAllowed is
     *                                  true or if Shapefile name is not equal to zip file base
     *                                  name
     */
    public Collection<String> getVectorLayers(final boolean onlyOneFileAllowed) throws IOException {
        final LinkedList<String> layers = new LinkedList<String>();
        if (zipFile != null) {
            for (Path path : zipFile.getRootDirectories()) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path vfile, BasicFileAttributes attrs) throws IOException {
                        String fileName = vfile.getFileName().toString();
                        if (fileIsShp(fileName)) {
                            format = "shp";
                            String base = getBase(fileName);

                            if (onlyOneFileAllowed) {
                                if (layers.size() > 1)
                                    throw new IllegalArgumentException(
                                        "Only one shapefile per zip is allowed. "
                                            + layers.size()
                                            + " shapefiles found.");

                                if (base.equals(getBase(file.getFileName().toString()))) {
                                    layers.add(base);
                                } else
                                    throw new IllegalArgumentException(
                                        "Shapefile name ("
                                            + base
                                            + ") is not equal to ZIP file name ("
                                            + file.getFileName() + ").");
                            } else {
                                layers.add(base);
                            }
                        }
                        if (fileIsGpkg(fileName)) {
                            format = "gpkg";
                            String base = getBase(fileName);

                            if (onlyOneFileAllowed) {
                                if (layers.size() > 1)
                                    throw new IllegalArgumentException(
                                        "Only one geopackage per zip is allowed. "
                                            + layers.size()
                                            + " geopackage found.");

                                if (base.equals(getBase(file.getFileName().toString()))) {
                                    layers.add(base);
                                } else
                                    throw new IllegalArgumentException(
                                        "Geopackage name ("
                                            + base
                                            + ") is not equal to ZIP file name ("
                                            + file.getFileName() + ").");
                            } else {
                                layers.add(base);
                            }
                        }
                        if (fileIsSld(fileName)) {
                            _containsSld = true;
                            _sldBody = fileName;
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            if (_containsSld) {
                ZipFile zf = new ZipFile(new File(this.file.toString()));
                InputStream is = zf.getInputStream(zf.getEntry(_sldBody));
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line;
                _sldBody = "";
                while ((line = br.readLine()) != null) {
                    _sldBody += line;
                }
                br.close();
                is.close();
                zf.close();
            }
        } else {
            String fileName = file.getFileName().toString();
            if (fileIsGpkg(fileName)) {
                format = "gpkg";
                layers.add(getBase(fileName));
            }
        }

        return layers;
    }

    /**
     * Returns the names of the raster layers (GeoTIFFs) in the geographic file.
     *
     * @return a collection of layer names
     */
    public Collection<String> getRasterLayers() throws IOException {
        final LinkedList<String> layers = new LinkedList<String>();
        if (zipFile != null) {
            for (Path path : zipFile.getRootDirectories()) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String fileName = file.getFileName().toString();
                        if (fileIsGeotif(fileName)) {
                            format = "tiff";
                            layers.add(getBase(fileName));
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } else {
            String fileName = file.getFileName().toString();
            if (fileIsGeotif(fileName)) {
                format = "tiff";
                layers.add(getBase(fileName));
            }
        }
        return layers;
    }

    private String getBase(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    private Boolean fileIsShp(String fileName) {
        String extension = getExtension(fileName);
        return extension.equalsIgnoreCase("shp");
    }

    private Boolean fileIsGpkg(String fileName) {
        String extension = getExtension(fileName);
        return extension.equalsIgnoreCase("gpkg");
    }

    private Boolean fileIsSld(String fileName) {
        String extension = getExtension(fileName);
        return extension.equalsIgnoreCase("sld");
    }

    public Boolean containsSld() {
        return this._containsSld;
    }

    public String getSld() {
        return this._sldBody;
    }

    public String getFormat() {
        return this.format;
    }

    @Override
    public void close() throws IOException {
        if (zipFile != null) {
            zipFile.close();
        }
    }
}
