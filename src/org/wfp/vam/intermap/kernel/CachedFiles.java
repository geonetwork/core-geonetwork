/**
 * CachedFiles.java
 *
 * @author Created by Omnicore CodeGuide
 */

package org.wfp.vam.intermap.kernel;

import java.io.File;

public class CachedFiles extends TempFiles
{
	private final File _dir;


	public CachedFiles(String dir, int minutes) throws Exception
	{
		super(minutes);

		_dir = new File(dir);
		if ( ! _dir.isDirectory())
			throw new Exception("Invalid temp directory '"+dir+"'");
	}

	public File getDir()
	{
		return _dir;
	}
}

