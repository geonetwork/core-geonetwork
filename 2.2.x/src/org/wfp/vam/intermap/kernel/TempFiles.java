//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.wfp.vam.intermap.kernel;

import java.io.*;
import java.util.*;

public class TempFiles
{
	protected File _dir;
	private int minutes;
	private Timer timer;

	/**
	 * Periodically starts a process that cleans up the temporary files
	 * every n minutes
	 *
	 */
	
    public TempFiles(String servletEnginePath, String path, int minutes) throws Exception
    {
        File p = new File(path);
        
        String finalPath;
        if (p.isAbsolute())
            finalPath = path;
        else
            finalPath = servletEnginePath + path;
        
        _dir = new File(finalPath);
        if (!_dir.isDirectory())
            throw new Exception("Invalid temp directory '" + finalPath + "'");

        timer = new Timer();
        timer.schedule(new RemindTask(),
                       0,
                       minutes * 60 * 1000);
   }
    
	public void end()
	{
		timer.cancel();
	}

	public File getDir()
	{
		return _dir;
	}

	/**
	 * Creates a temporary File
	 *
	 * @return   a temporary File
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

