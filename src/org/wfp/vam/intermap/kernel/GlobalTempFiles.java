/**
 * CachedFiles.java
 *
 * @author Created by Omnicore CodeGuide
 */

package org.wfp.vam.intermap.kernel;


import java.io.File;

public class GlobalTempFiles extends TempFiles
{
	private final File _dir;
	private static GlobalTempFiles _instance = null;

	public static GlobalTempFiles getInstance()
	{
		return _instance;
	}

	public static synchronized void init(String dir, int minutes) throws Exception
	{
		if( _instance != null)
			throw new IllegalStateException("This singleton has already been initialized.");

		_instance = new GlobalTempFiles(dir, minutes);
	}

	private GlobalTempFiles(String dir, int minutes) throws Exception
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

