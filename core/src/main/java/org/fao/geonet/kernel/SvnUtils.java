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
     *  Adds a directory item to the subversion repository.
     *
     * @param editor ISVNEditor opened on repository
     * @param filePath The path of the file to be added eg. 10/metadata.xml
     * @param data The data contents of the file as a byte array
     * @throws SVNException
     */
	public static void addDir(ISVNEditor editor, String dirPath) throws SVNException {

		editor.addDir(dirPath, null ,-1);

		// Closes the directory.
		editor.closeDir();

	}	

    /**
     *  Adds a file item to the subversion repository.
     *
     * @param editor ISVNEditor opened on repository
     * @param filePath The path of the file to be added eg. 10/metadata.xml
     * @param data The data contents of the file as a byte array
     * @throws SVNException
     */
	public static void addFile(ISVNEditor editor, String filePath, byte[] data) throws SVNException {

		editor.addFile(filePath, null ,-1);

		editor.applyTextDelta(filePath, null);

		SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
		String checksum = deltaGenerator.sendDelta(filePath, new ByteArrayInputStream(data), editor, true);

		editor.closeFile(filePath, checksum);

	}	

    /**
     *  Modify properties on a file item in the subversion repository.
     *
     * @param editor ISVNEditor opened on repository
     * @param filePath The path name of the file item to be updated eg. 10/metadata.xml
     * @param props The updated properties to set on the file item 
     * @throws SVNException
     */
	public static void modifyFileProps(ISVNEditor editor, String filePath, Map<String,String> props) throws SVNException {

		// add properties
		for (Map.Entry<String,String> entry : props.entrySet()) {
			String propertyValue = entry.getValue();
            editor.changeFileProperty(filePath, entry.getKey(), SVNPropertyValue.create(propertyValue));
		}

	}

    /**
     *  Modify/Update content of file item in the subversion repository.
     *
     * @param editor ISVNEditor opened on repository
     * @param filePath The path name of the file item to be updated eg. 10/metadata.xml
     * @param props The updated properties to set on the file item 
     * @param oldData The data that is to be updated
     * @param newData The updated data
     * @throws SVNException
     */
	public static void modifyFile(ISVNEditor editor, String filePath, byte[] oldData, byte[] newData) throws SVNException {

		editor.applyTextDelta(filePath, null);
        
		SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
		deltaGenerator.sendDelta(filePath, new ByteArrayInputStream(oldData), 0, new ByteArrayInputStream(newData), editor, true);
	}

    /**
     *  Delete file item and parent directory from the subversion repository.
     *
     * @param editor ISVNEditor opened on repository
     * @param dirPath The directory containing the file item. eg. 10. Both the directory and the file item it contains will be deleted.
     * @throws SVNException
     */
	public static void deleteDir(ISVNEditor editor, String dirPath) throws SVNException {
		editor.openRoot(-1);

		editor.deleteEntry(dirPath, -1);

		// Closes the root directory.
		editor.closeDir();
	}

}
