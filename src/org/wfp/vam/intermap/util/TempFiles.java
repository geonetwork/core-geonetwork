/**
 * TempFiles.java
 *
 * @author Created by Omnicore CodeGuide
 */

package org.wfp.vam.intermap.util;

import java.io.*;
import java.util.*;

public class TempFiles
{
	private File dir;
	private int minutes;
	private Timer timer;

	/**
	 * Periodically starts a process that cleans up the temporary files
	 * every n minutes
	 *
	 */
	public TempFiles() {
		timer = new Timer();
		timer.schedule(new RemindTask(),
					   0,
					   minutes * 60 * 1000);
	}

	public TempFiles(String tempDir, int minutes) throws Exception {
		this.dir = new File(tempDir);
		if (!dir.isDirectory())
			throw new Exception("Invalid temp directory: " + dir);

		timer = new Timer();
		timer.schedule(new RemindTask(),
					   0,
					   minutes * 60 * 1000);
	}
    
    public void end() {
		timer.cancel();
	}

	public File getDir() { return dir; }

	/**
	 * Creates a temporary File
	 *
	 * @return   a tempoarary File
	 *
	 * @throws   If a file could not be created
	 *
	 */
	public File getFile() throws IOException {
		File tf = File.createTempFile("temp", ".tmp", dir);
		tf.deleteOnExit();
		return tf;
	}

	// Delete all the files in the temp directory
	class RemindTask extends TimerTask {

		public void run() {
			File files[] = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File f = files[i];
				Calendar last = Calendar.getInstance();
				last.add(Calendar.MINUTE, -minutes);
				
				// Only files who's name ends with ".tmp" are deleted
				if (f.getName().endsWith(".tmp") && last.getTime().after( new Date(f.lastModified())) )
					f.delete();
			}
		}
	}

}

