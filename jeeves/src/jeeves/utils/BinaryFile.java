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

package jeeves.utils;

import jeeves.constants.ConfigFile;
import java.io.*;
import sun.misc.*;
import org.jdom.*;

import com.jcraft.jsch.*;

import org.globus.ftp.Buffer;
import org.globus.ftp.DataSink;
import org.globus.ftp.FTPClient;
import org.globus.ftp.Session;

import org.apache.commons.lang.StringEscapeUtils;


//=============================================================================

/** class to encode/decode binary files to base64 strings
  */

public class BinaryFile
{
	private static final int BUF_SIZE = 8192;
	private static boolean remoteFile = false;
	private static String remoteUser = "";
	private static String remotePassword = "";
	private static String remoteSite = "";
	private static String remotePath = "";
	private static String remoteProtocol = "";

	//---------------------------------------------------------------------------
	// Read the first 2000 chars from the file to get the info we want if the
	// file is remote

	static String readInput(String path) {
    StringBuffer buffer = new StringBuffer();
    try {
			FileInputStream fis = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(fis,"UTF8");
			Reader in = new BufferedReader(isr);
			int ch;
			int numRead = 0;
			while (((ch = in.read()) > -1) && (numRead < 2000))  {
				buffer.append((char)ch);
				numRead++;
			}
			in.close();
			return buffer.toString();
    } catch (IOException e) {
			e.printStackTrace();
			return null;
    }
	}	

	//---------------------------------------------------------------------------

	private static String getRemoteProtocol(String header) {
		String remoteProtocol;

		if (header.startsWith("#geonetworkremotescp")) { 
			remoteProtocol = "scp";
		} else if (header.startsWith("#geonetworkremoteftp")) {
			remoteProtocol = "ftp";
		} else {
			remoteProtocol = "unknown";
		}
		return remoteProtocol;
	}

	//---------------------------------------------------------------------------

	private static void checkForRemoteFile(String path) {
		String fileContents = readInput(path);
		if ((fileContents != null) && (fileContents.toLowerCase().startsWith("#geonetworkremotescp") || fileContents.toLowerCase().startsWith("#geonetworkremoteftp"))) {
				String[] tokens = fileContents.split("\n");
				if (tokens.length == 5) {
					remoteUser = tokens[1].trim();
					remotePassword = tokens[2].trim();
					remoteSite = tokens[3].trim();
					remotePath = tokens[4].trim();
					remoteProtocol = getRemoteProtocol(fileContents.toLowerCase());
					remoteFile = true;
					Log.debug(Log.RESOURCES, "REMOTE: "+remoteUser+":********:"+remoteSite+":"+remotePath+":"+remoteProtocol);	
				} else {
					Log.debug(Log.RESOURCES, "ERROR: remote file details were not valid");
					remoteFile = false;
				}
		} else {
			remoteFile = false;
		}
	}

	//---------------------------------------------------------------------------

	public static Element encode(int responseCode, String path, String name, boolean remove)
	{
		Element response = encode(responseCode, path, remove);
		response.setAttribute("name", name);
		return response;
	}

	//---------------------------------------------------------------------------
	
	public static Element encode(int responseCode, String path)
	{
		return encode(responseCode, path, false);
	}

	//---------------------------------------------------------------------------

	public static Element encode(int responseCode, String path, boolean remove)
	{
		Element response = new Element("response");
		checkForRemoteFile(path);

		response.setAttribute("responseCode", responseCode + "");
		response.setAttribute("path", path);
		response.setAttribute("remove", remove ? "y" : "n");
		if (remoteFile) {
			response.setAttribute("remotepath", remoteUser+"@"+remoteSite+":"+remotePath);
			response.setAttribute("remotefile", new File(remotePath).getName());
		}
		return response;
	}

	//---------------------------------------------------------------------------

	public static String getContentType(Element response)
	{
		String path = response.getAttributeValue("path");
		if (path == null) return null;
		return getContentType(path);
	}

	//---------------------------------------------------------------------------

	public static String getContentLength(Element response)
	{
		String path = response.getAttributeValue("path");
		if (path == null) return null;
		String length = "-1";
		if (!remoteFile) {
			File f = new File(path);
			length = f.length() + "";
		}
		return length;
	}

	//---------------------------------------------------------------------------

	public static void removeIfTheCase(Element response)
	{
		boolean remove = "y".equals(response.getAttributeValue("remove"));

		if (remove)
		{
			String path = response.getAttributeValue("path");
			new File(path).delete();
		}
	}

	//---------------------------------------------------------------------------

	public static String getContentDisposition(Element response)
	{
		String name = response.getAttributeValue("name");
		if (name == null)
		{
			name = response.getAttributeValue("path");
			if (name == null) return null;
			name = new File(name).getName();
		}

		return org.apache.commons.lang.StringEscapeUtils.escapeHtml("attachment;filename=" + name);
	}

	//---------------------------------------------------------------------------

	public static int getResponseCode(Element response)
	{
		return Integer.parseInt(response.getAttributeValue("responseCode"));
	}

	//---------------------------------------------------------------------------

	public static void write(Element response, OutputStream output) throws IOException
	{
		//----------------------------------------------------------------------
		// Local class required by jsch for scp
		class MyUserInfo implements UserInfo {
			String passwd = remotePassword;

			public String getPassword() { 
				return passwd; 
			}
			public String getPassphrase() { 
				return passwd; 
			}
			public void showMessage(String message){ }
			public boolean promptYesNo(String message){ return true; }
			public boolean promptPassword(String message){ return true; }
			public boolean promptPassphrase(String message){ return true; }
		}

		//---------------------------------------------------------------------
		// Local class needed by globus ftpclient for ftp 
		class DataSinkStream implements DataSink {
   		protected OutputStream out;
    	protected boolean autoFlush;
    	protected boolean ignoreOffset;
    	protected long offset = 0;

    	public DataSinkStream(OutputStream out) {
				this(out, false, false);
    	}

    	public DataSinkStream(OutputStream out,
			  	boolean autoFlush,
			  	boolean ignoreOffset) {
					this.out = out;
					this.autoFlush = autoFlush;
					this.ignoreOffset = ignoreOffset;
    	}

    	public void write(org.globus.ftp.Buffer buffer) throws IOException {
				long bufOffset = buffer.getOffset();
				if (ignoreOffset ||
	   		  	bufOffset == -1 ||
	    			bufOffset == offset) {
	    				out.write(buffer.getBuffer(), 0, buffer.getLength());
	    				if (autoFlush) out.flush();
	    				offset += buffer.getLength();
				} else {
	    		throw new IOException("Random offsets not supported.");
				}
   		}
    
    	public void close() { // don't close the output stream
    	}
		}

		String path = response.getAttributeValue("path");
		if (path == null) return;
		if (!remoteFile) {
			File f = new File(path);
			InputStream input = new FileInputStream(f);
			copy(input, output, true, false);
		} else {
			if (remoteProtocol.equals("scp")) {
				try {
					// set up JSch: channel to scp
					JSch jsch=new JSch();
					com.jcraft.jsch.Session session=jsch.getSession(remoteUser, remoteSite, 22);
					UserInfo ui=new MyUserInfo();
					session.setUserInfo(ui);
					session.connect();
				
					String command="scp -f "+remotePath;
					Channel channel=session.openChannel("exec");
					((ChannelExec)channel).setCommand(command);
	
					// get I/O streams for remote scp
					OutputStream outScp=channel.getOutputStream();
					InputStream inScp=channel.getInputStream();
					channel.connect();
				
					copy(inScp,outScp,output);
				
					session.disconnect();
				} catch (Exception e) {
					Log.error(Log.RESOURCES,"Problem with scp from site: "+remoteUser+"@"+remoteSite+":"+remotePath);
					e.printStackTrace();
				}
			} else if (remoteProtocol.equals("ftp")) {
			 	// set up globus FTP client
				try {
					FTPClient ftp = new FTPClient(remoteSite, 21);
					ftp.authorize(remoteUser, remotePassword);
					ftp.setType(Session.TYPE_IMAGE);
					DataSinkStream outputSink = new DataSinkStream(output);
					ftp.get(remotePath, outputSink, null);
				} catch (Exception e) {
					Log.error(Log.RESOURCES,"Problem with ftp from site: "+remoteUser+"@"+remoteSite+":"+remotePath);
					e.printStackTrace();
				}
			} else {
				Log.error(Log.RESOURCES,"Unknown remote protocol in config file");
			}
		}
	}

	//----------------------------------------------------------------------------
	// copies an input stream from a JSch object to an output stream

	private static int checkAck(InputStream in) throws IOException
  {
    int b=in.read();
    // b may be 0 for success,
    //          1 for error,
    //          2 for fatal error,
    //         -1
    if(b==0) return b;
    if(b==-1) return b;
    if(b==1 || b==2) {
    	StringBuffer sb=new StringBuffer();
    	int c;
    	do {
    		c=in.read();
    		sb.append((char)c);
    	}
    	while(c!='\n');
    	if(b==1) { // error
    		Log.error(Log.RESOURCES,"scp: Protocol error: "+sb.toString());
    	}
    	if(b==2) { // fatal error
    		Log.error(Log.RESOURCES,"scp: Protocol error: "+sb.toString());
    	}
    }
    return b;
   }

	//----------------------------------------------------------------------------
	// copies an input stream from a JSch object to an output stream

	private static void copy(InputStream inScp, OutputStream outScp, OutputStream output) throws IOException
	{
		byte[] buf=new byte[1024];

    // send '\0' to scp
    buf[0]=0; outScp.write(buf, 0, 1); outScp.flush();
    while(true){
      int c=checkAck(inScp);
      if(c!='C') break;
      // read '0644 ' from scp
      inScp.read(buf, 0, 5);

			// establish file size from scp
      long filesize=0L;
      while(true) {
      	if(inScp.read(buf, 0, 1)<0) {
      		// error from scp
      		break;
      	}
      	if(buf[0]==' ') break;
      	filesize=filesize*10L+(long)(buf[0]-'0');
      }
     
			// now get file name from scp
      String file=null;
      for(int i=0;;i++) {
      	inScp.read(buf, i, 1);
      	if(buf[i]==(byte)0x0a) {
      		file=new String(buf, 0, i);
      		break;
      	}
      }
      
			// now get file name from scp 
      Log.debug(Log.RESOURCES,"scp: file returned has filesize="+filesize+", file="+file);
      
      // send '\0'
      buf[0]=0; outScp.write(buf, 0, 1); outScp.flush();

			// read contents from scp
			int foo;
			while(true) {
				if(buf.length<filesize) foo=buf.length;
				else foo=(int)filesize;
				foo=inScp.read(buf, 0, foo);
				if(foo<0) {
					// error
					break;
				}
				output.write(buf, 0, foo);
				filesize-=foo;
				if(filesize==0L) break;
			}
			if(checkAck(inScp)==0) {
				// send '\0'
				buf[0]=0; outScp.write(buf, 0, 1); outScp.flush();
			}
		}
	}

	//----------------------------------------------------------------------------
	// copies an input stream (from a file) to an output stream

	public static void copy(InputStream in, OutputStream out, boolean closeInput,
									boolean closeOutput) throws IOException
	{
		BufferedInputStream input = new BufferedInputStream(in);

		try
		{
			byte buffer[] = new byte[BUF_SIZE];
			int nRead;

			while ((nRead = input.read(buffer)) > 0)
				out.write(buffer, 0, nRead);
		}
		finally
		{
			if (closeInput)
				in.close();

			if (closeOutput)
				out.close();
		}
	}

	//----------------------------------------------------------------------------
	// Returns the mime-type corresponding to the given file extension

	private static String getContentType(String fName)
	{
		// standard graphical formats
		if (fName.endsWith(".gif"))
			return "image/gif";
		else if (fName.endsWith(".jpg") || fName.endsWith(".jpeg"))
			return "image/jpeg";
		else if (fName.endsWith(".png"))
			return "application/png";
		else if (fName.endsWith(".bmp"))
			return "application/bmp";

		// compressed formats
		else if (fName.endsWith(".zip"))
			return "application/zip";

		// generic document formats
		else if (fName.endsWith(".pdf"))
			return "application/pdf";
		else if (fName.endsWith(".eps"))
			return "application/eps";
		else if (fName.endsWith(".ai"))
			return "application/ai";

		// arcinfo formats
		else if (fName.endsWith(".pmf"))
			return "application/pmf";
		else if (fName.endsWith(".e00"))
			return "application/e00";
		else
			return("application/binary");
	}

}

//=============================================================================

