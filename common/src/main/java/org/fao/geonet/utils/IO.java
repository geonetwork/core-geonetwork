//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.Constants;
import org.fao.geonet.Logger;

//=============================================================================

/**
 * A container of I/O methods. <P>
 * 
 */
public final class IO
{
	/**
    * Default constructor.
    * Builds a IO.
    */
   private IO() {}
   
   /**
	 * Loads a text file, handling the exceptions
	 * @param name
	 * @return
	 */
	public static String loadFile(String name)
	{
		StringBuffer sb = new StringBuffer();

		FileInputStream in = null;
		BufferedReader	rdr = null;
		try
		{
            in = new FileInputStream(name);
            rdr = new BufferedReader(new InputStreamReader(in, Charset.forName(Constants.ENCODING)));

			String inputLine;

			while ((inputLine = rdr.readLine()) != null) {
				sb.append(inputLine);
				sb.append('\n');
			}

			return sb.toString();
		}
		catch (IOException e)
		{
			return null;
		} finally {
		    if (in != null) {
		        IOUtils.closeQuietly(in);
		    }
		    if (rdr != null) {
		        IOUtils.closeQuietly(rdr);
		    }
		}
	}
	
	/**
	 * Make a directory (and parent directories) if it does not exist.
	 * If the directory cannot be made and does not exist or is a file an exception is thrown 
	 * 
	 * @param dir the directory to make
	 * @param desc A short description of the directory being made.
	 */
	public static void mkdirs(File dir, String desc) throws IOException {
        if(!dir.mkdirs()) {
            if (!dir.exists()) {
                String msg = "Unable to make '"+desc+"': "+dir.getAbsolutePath()+". Check permissions of parent directory";
                throw new IOException(msg);
            }
            if (dir.isFile()){
                String msg = "Unable to make '"+desc+"': "+dir.getAbsolutePath()+". The file already exists and is a file";
                throw new IOException(msg);
                
            }
        }

	}

	/**
	 * Set lastModified time if a failure log a warning.
	 * 
	 * @param file the file to set the time on
	 * @param timeMillis the time in millis
	 * @param loggerModule the module to log to
	 */
    public static void setLastModified(File file, long timeMillis, String loggerModule) {
        if (!file.setLastModified(timeMillis)) {
            Log.warning(loggerModule, "Unable to set the last modified time on: "+file.getAbsolutePath()+".  Check file permissions");
        }
    }

    public static void delete(File file, boolean throwException, String loggerModule) {
        if (!file.delete() && file.exists()) {
            if(throwException) {
                throw new RuntimeException("Unable to delete "+file.getAbsolutePath());
            } else {
                Log.warning(loggerModule, "Unable to delete "+file.getAbsolutePath());
            }
        }
    }

    public static void delete(File file, boolean throwException, Logger context) {
        if (!file.delete() && file.exists()) {
            if(throwException) {
                throw new RuntimeException("Unable to delete "+file.getAbsolutePath());
            } else {
                context.warning("Unable to delete "+file.getAbsolutePath());
            }
        }
    }

    public static void closeQuietly(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    public static void closeQuietly(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    /**
     * Returns a list of all file names in a directory - if recurse is true, 
     * processes all subdirectories too.
     * @param directory
     * @param recurse
     * @return
     */
    public static List<File> getFilesInDirectory(File directory, boolean recurse, FilenameFilter filter) throws IOException {
    	List<File> fileList = new ArrayList<File>();
    	if(! directory.exists()) {
    		throw new IOException("Directory does not exist: "+ directory.getAbsolutePath());
    	}
    	if(! directory.canRead()) {
    		throw new IOException("Cannot read directory: "+ directory.getAbsolutePath());
    	}
    	if(! directory.isDirectory()) {
    		throw new IOException("Directory is not a directory: "+ directory.getAbsolutePath());
    	}
    	for(File file : directory.listFiles(filter)) {
    		if(file.isDirectory()) {
    			if(recurse) { 
    				// recurse
    				fileList.addAll(getFilesInDirectory(file, recurse, filter));
    			}
    		}
    		else {
    			if(! file.canRead()) {
    				throw new IOException("Cannot read file "+ file.getAbsolutePath());
    			}
    			else {
    				fileList.add(file);
    			}
    		}
    	}		
    	return fileList;
    }
}

//=============================================================================

