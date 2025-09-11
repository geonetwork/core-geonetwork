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
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.utils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.UserInfo;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.Constants;
import org.globus.ftp.DataSink;
import org.globus.ftp.FTPClient;
import org.globus.ftp.Session;
import org.jdom.Element;
import org.springframework.core.io.Resource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

//=============================================================================

/**
 * class to encode/decode binary files to base64 strings
 */

public final class BinaryFile {
    private final static int BUF_SIZE = 8192;
    private Element element = new Element("response");
    private boolean remoteFile = false;
    private String remoteUser = "";
    private String remotePassword = "";
    private String remoteSite = "";
    private String remotePath = "";
    private String remoteProtocol = "";
    /**
     * Default constructor. Builds a BinaryFile.
     */
    public BinaryFile() {
    }

    public BinaryFile(Element element) {
        this.element = element;
    }

    public static BinaryFile encode(int responseCode, Path path, String name, boolean remove) {
        BinaryFile response = encode(responseCode, path, remove);
        response.getElement().setAttribute("name", name);
        return response;
    }

    //---------------------------------------------------------------------------
    // Read the first 2000 chars from the file to get the info we want if the
    // file is remote

    public static BinaryFile encode(int responseCode, Path path) {
        return encode(responseCode, path, false);
    }

    //---------------------------------------------------------------------------

    public static BinaryFile encode(int responseCode, Path path, boolean remove) {
        final BinaryFile binaryFile = new BinaryFile();
        binaryFile.doEncode(responseCode, path, remove);
        return binaryFile;
    }

    /*
     * TODO: Implement resources/streams instead of temporary files or remove this method along with the unused services that call it:
     *     - org.fao.geonet.services.feedback.AddLimitations
     *     - org.fao.geonet.services.resources.DownloadArchive
     *   See the following issues:
     *     - https://github.com/geonetwork/core-geonetwork/issues/3308
     *     - https://github.com/geonetwork/core-geonetwork/issues/4459
     */
    public static BinaryFile encode(int responseCode, Resource resource, int metadataId, String filename, boolean remove) throws IOException {
        Path path;
        if (resource.isFile()) {
            path = resource.getFile().toPath();
        } else {
            // Create a temporary file
            // Preserve filename by putting the files into a temporary folder and using the same filename.
            Path tempFolderPath = Files.createTempDirectory("gn-meta-res-" + metadataId + "-");
            tempFolderPath.toFile().deleteOnExit();
            path = tempFolderPath.resolve(filename);
            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        return encode(responseCode, path, remove);
    }

    //---------------------------------------------------------------------------

    private static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //         -1
        if (b == 0) return b;
        if (b == -1) return b;
        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                Log.error(Log.RESOURCES, "scp: Protocol error: " + sb.toString());
            }
            if (b == 2) { // fatal error
                Log.error(Log.RESOURCES, "scp: Protocol error: " + sb.toString());
            }
        }
        return b;
    }

    //---------------------------------------------------------------------------

    private static void copy(InputStream inScp, OutputStream outScp, OutputStream output) throws IOException {
        byte[] buf = new byte[1024];

        // send '\0' to scp
        buf[0] = 0;
        outScp.write(buf, 0, 1);
        outScp.flush();
        while (true) {
            int c = checkAck(inScp);
            if (c != 'C') break;
            // read '0644 ' from scp
            if (inScp.read(buf, 0, 5) == -1) {
                throw new IllegalStateException("Expected 0664 but got nothing");
            }

            // establish file size from scp
            long filesize = 0L;
            while (true) {
                if (inScp.read(buf, 0, 1) < 0) {
                    // error from scp
                    break;
                }
                if (buf[0] == ' ') break;
                filesize = filesize * 10L + (long) (buf[0] - '0');
            }

            // now get file name from scp
            String file = null;
            for (int i = 0; ; i++) {
                if (inScp.read(buf, i, 1) == -1) {
                    throw new IllegalStateException("Unable to read file name");
                }
                if (buf[i] == (byte) 0x0a) {
                    file = new String(buf, 0, i, Charset.forName(Constants.ENCODING));
                    break;
                }
            }

            // now get file name from scp
            if (Log.isDebugEnabled(Log.RESOURCES))
                Log.debug(Log.RESOURCES, "scp: file returned has filesize=" + filesize + ", file=" + file);

            // send '\0'
            buf[0] = 0;
            outScp.write(buf, 0, 1);
            outScp.flush();

            // read contents from scp
            int foo;
            while (true) {
                if (buf.length < filesize) foo = buf.length;
                else foo = (int) filesize;
                foo = inScp.read(buf, 0, foo);
                if (foo < 0) {
                    // error
                    break;
                }
                output.write(buf, 0, foo);
                filesize -= foo;
                if (filesize == 0L) break;
            }
            if (checkAck(inScp) == 0) {
                // send '\0'
                buf[0] = 0;
                outScp.write(buf, 0, 1);
                outScp.flush();
            }
        }
    }

    //---------------------------------------------------------------------------

    /**
     * Copies an input stream (from a file) to an output stream
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        if (in instanceof FileInputStream) {
            FileInputStream fin = (FileInputStream) in;
            WritableByteChannel outChannel;
            if (out instanceof FileOutputStream) {
                outChannel = ((FileOutputStream) out).getChannel();
            } else {
                outChannel = Channels.newChannel(out);
            }
            fin.getChannel().transferTo(0, Long.MAX_VALUE, outChannel);
        } else {
            BufferedInputStream input = new BufferedInputStream(in);

            byte buffer[] = new byte[BUF_SIZE];
            int nRead;

            while ((nRead = input.read(buffer)) > 0)
                out.write(buffer, 0, nRead);
        }
    }

    //---------------------------------------------------------------------------

    private static String getContentType(String fName) {
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
            return ("application/binary");
    }

    /**
     * Gets the remotePassword.
     *
     * @return the remotePassword.
     */
    private String getRemotePassword() {
        return remotePassword;
    }

    //---------------------------------------------------------------------------

    private String readInput(Path path) {

        try {
            return new String(Files.readAllBytes(path), Constants.CHARSET);
        } catch (IOException e) {
            Log.error("geonetwork", "Error reading file: " + path);
            return null;
        }
    }

    //---------------------------------------------------------------------------

    private String getRemoteProtocol(String header) {
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

    private void checkForRemoteFile(Path path) {
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
                if (Log.isDebugEnabled(Log.RESOURCES))
                    Log.debug(Log.RESOURCES, "REMOTE: " + remoteUser + ":********:" + remoteSite + ":" + remotePath + ":" + remoteProtocol);
            } else {
                if (Log.isDebugEnabled(Log.RESOURCES))
                    Log.debug(Log.RESOURCES, "ERROR: remote file details were not valid");
                remoteFile = false;
            }
        } else {
            remoteFile = false;
        }
    }

    //---------------------------------------------------------------------------

    private void doEncode(int responseCode, Path path, boolean remove) {
        checkForRemoteFile(path);

        element.setAttribute("responseCode", responseCode + "");
        element.setAttribute("path", path.toString());
        element.setAttribute("remove", remove ? "y" : "n");
        if (remoteFile) {
            element.setAttribute("remotepath", remoteUser + "@" + remoteSite + ":" + remotePath);
            element.setAttribute("remotefile", new File(remotePath).getName());
        }
    }

    //---------------------------------------------------------------------------

    public String getContentType() {
        String path = element.getAttributeValue("path");
        if (path == null) return null;
        return getContentType(path);
    }

    //----------------------------------------------------------------------------
    // copies an input stream from a JSch object to an output stream

    public String getContentLength() {
        String path = element.getAttributeValue("path");
        if (path == null) return null;
        String length = "-1";
        if (!remoteFile) {
            File f = new File(path);
            length = f.length() + "";
        }
        return length;
    }

    //----------------------------------------------------------------------------
    // copies an input stream from a JSch object to an output stream

    public void removeIfTheCase() {
        boolean remove = "y".equals(element.getAttributeValue("remove"));

        if (remove) {
            String path = element.getAttributeValue("path");
            File file = new File(path);
            if (!file.delete() && file.exists()) {
                Log.warning(Log.JEEVES, "[" + BinaryFile.class.getName() + "#removeIfTheCase]" + "Unable to remove binary file after sending to user.");
            }
        }
    }

    public String getContentDisposition() {
        String name = element.getAttributeValue("name");
        if (name == null) {
            name = element.getAttributeValue("path");
            if (name == null) return null;
            name = new File(name).getName();
        }

        return org.apache.commons.lang.StringEscapeUtils.escapeHtml("attachment;filename=" + name);
    }

    //----------------------------------------------------------------------------
    // Returns the mime-type corresponding to the given file extension

    public void write(OutputStream output) throws IOException {
        //----------------------------------------------------------------------
        // Local class required by jsch for scp
        class MyUserInfo implements UserInfo {
            String passwd = getRemotePassword();

            public String getPassword() {
                return passwd;
            }

            public String getPassphrase() {
                return passwd;
            }

            public void showMessage(String message) {
            }

            public boolean promptYesNo(String message) {
                return true;
            }

            public boolean promptPassword(String message) {
                return true;
            }

            public boolean promptPassphrase(String message) {
                return true;
            }
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

        String path = element.getAttributeValue("path");
        if (path == null) return;
        if (!remoteFile) {
            File f = new File(path);
            InputStream input = null;
            try {
                input = new FileInputStream(f);
                copy(input, output);
            } finally {
                IOUtils.closeQuietly(input);
            }
        } else {
            if (remoteProtocol.equals("scp")) {
                try {
                    // set up JSch: channel to scp
                    JSch jsch = new JSch();
                    com.jcraft.jsch.Session session = jsch.getSession(remoteUser, remoteSite, 22);
                    UserInfo ui = new MyUserInfo();
                    session.setUserInfo(ui);
                    try {
                        session.connect();

                        String command = "scp -f " + remotePath;
                        Channel channel = session.openChannel("exec");
                        ((ChannelExec) channel).setCommand(command);

                        // get I/O streams for remote scp
                        OutputStream outScp = channel.getOutputStream();
                        InputStream inScp = channel.getInputStream();
                        channel.connect();

                        copy(inScp, outScp, output);
                    } finally {
                        session.disconnect();
                    }
                } catch (Exception e) {
                    Log.error(Log.RESOURCES, "Problem with scp from site: " + remoteUser + "@" + remoteSite + ":" + remotePath, e);
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
                    Log.error(Log.RESOURCES, "Problem with ftp from site: " + remoteUser + "@" + remoteSite + ":" + remotePath, e);
                }
            } else {
                Log.error(Log.RESOURCES, "Unknown remote protocol in config file");
            }
        }
    }

    public Element getElement() {
        return element;
    }
}

//=============================================================================

