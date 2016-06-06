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

// Modified from examples provided by MateSoft for SVNKit on
// http://wiki.svnkit.com/Committing_To_A_Repository

package org.fao.geonet.kernel;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;

import java.io.ByteArrayInputStream;
import java.util.Map;

public class SvnUtils {

    /**
     * Adds a directory item to the subversion repository.
     *
     * @param editor   ISVNEditor opened on repository
     * @param filePath The path of the file to be added eg. 10/metadata.xml
     * @param data     The data contents of the file as a byte array
     */
    public static void addDir(ISVNEditor editor, String dirPath) throws SVNException {

        editor.addDir(dirPath, null, -1);

        // Closes the directory.
        editor.closeDir();

    }

    /**
     * Adds a file item to the subversion repository.
     *
     * @param editor   ISVNEditor opened on repository
     * @param filePath The path of the file to be added eg. 10/metadata.xml
     * @param data     The data contents of the file as a byte array
     */
    public static void addFile(ISVNEditor editor, String filePath, byte[] data) throws SVNException {

        editor.addFile(filePath, null, -1);

        editor.applyTextDelta(filePath, null);

        SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
        String checksum = deltaGenerator.sendDelta(filePath, new ByteArrayInputStream(data), editor, true);

        editor.closeFile(filePath, checksum);

    }

    /**
     * Modify properties on a file item in the subversion repository.
     *
     * @param editor   ISVNEditor opened on repository
     * @param filePath The path name of the file item to be updated eg. 10/metadata.xml
     * @param props    The updated properties to set on the file item
     */
    public static void modifyFileProps(ISVNEditor editor, String filePath, Map<String, String> props) throws SVNException {

        // add properties
        for (Map.Entry<String, String> entry : props.entrySet()) {
            String propertyValue = entry.getValue();
            editor.changeFileProperty(filePath, entry.getKey(), SVNPropertyValue.create(propertyValue));
        }

    }

    /**
     * Modify/Update content of file item in the subversion repository.
     *
     * @param editor   ISVNEditor opened on repository
     * @param filePath The path name of the file item to be updated eg. 10/metadata.xml
     * @param props    The updated properties to set on the file item
     * @param oldData  The data that is to be updated
     * @param newData  The updated data
     */
    public static void modifyFile(ISVNEditor editor, String filePath, byte[] oldData, byte[] newData) throws SVNException {

        editor.applyTextDelta(filePath, null);

        SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
        deltaGenerator.sendDelta(filePath, new ByteArrayInputStream(oldData), 0, new ByteArrayInputStream(newData), editor, true);
    }

    /**
     * Delete file item and parent directory from the subversion repository.
     *
     * @param editor  ISVNEditor opened on repository
     * @param dirPath The directory containing the file item. eg. 10. Both the directory and the
     *                file item it contains will be deleted.
     */
    public static void deleteDir(ISVNEditor editor, String dirPath) throws SVNException {
        editor.openRoot(-1);

        editor.deleteEntry(dirPath, -1);

        // Closes the root directory.
        editor.closeDir();
    }

}
