/**
 * TempFiles.java
 *
 */

package org.wfp.vam.intermap.kernel;

import java.io.*;
import java.util.*;

public abstract class TempFiles
{
//	private static File dir;
	private int minutes;
	private Timer timer;

	/**
	 * Periodically starts a process that cleans up the temporary files
	 * every n minutes
	 *
	 */
	protected TempFiles(int minutes) throws Exception
	{
//		dir = new File(tempDir);
//		if (!dir.isDirectory())
//			throw new Exception("Invalid temp directory '"+tempDir+"'");

		timer = new Timer();
		timer.schedule(new RemindTask(),
					   0,
					   minutes * 60 * 1000);
	}

	public void end()
	{
		timer.cancel();
	}

	abstract public File getDir();
//	{
//		return dir;
//	}

	/**
	 * Creates a temporary File
	 *
	 * @return   a tempoarary File
	 *
	 * @throws   If a file could not be created
	 *
	 */
	public File getFile() throws IOException
	{
		return getFile(".tmp");
	}

	public File getFile(String extension) throws IOException
	{
		if( ! extension.startsWith("."))
			extension = "."+extension;

		File tf = File.createTempFile("temp", extension, getDir());
		tf.deleteOnExit();
		return tf;
	}

	// Delete all the files in the temp directory
	class RemindTask extends TimerTask {

		public void run()
		{
			for (File f: getDir().listFiles())
			{
				Calendar last = Calendar.getInstance();
				last.add(Calendar.MINUTE, -minutes);
				// Only files whose name start with ".temp" are deleted
				if (f.getName().startsWith("temp") && last.getTime().after( new Date(f.lastModified())) )
					f.delete();
			}
		}
	}

}

