//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.harvest.harvester.sftp.client;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

public class SftpClient {
    private static final String UTF8_BOM = "\uFEFF";

    private final String      host;
    private final int         port;
    private final String      username;
    private final JSch jsch;
    private ChannelSftp channel;
    private Session session;

    /**
     * @param host     remote host
     * @param port     remote port
     * @param username remote username
     */
    public SftpClient(String host, int port, String username) {
        this.host     = host;
        this.port     = port;
        this.username = username;
        jsch          = new JSch();
    }

    /**
     * Use default port 22
     *
     * @param host     remote host.
     * @param username username on host.
     */
    public SftpClient(String host, String username) {
        this(host, 22, username);
    }

    /**
     * Authenticate the user using a password.
     *
     * @param password user password.
     * @throws JSchException If there is problem with credentials or connection.
     */
    public void authPassword(String password) throws JSchException {
        session = jsch.getSession(username, host, port);
        //disable known hosts checking
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setPassword(password);
        session.connect();
        channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
    }

    /**
     * Authenticate the user using a private/public key.
     *
     * @param keyPath Path of the private key file.
     * @throws JSchException If there is problem with credentials or connection.
     */
    public void authKey(String keyPath, String pass) throws JSchException {
        jsch.addIdentity(keyPath, pass);
        session = jsch.getSession(username, host, port);
        //disable known hosts checking
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
    }

    /**
     * List all files including directories.
     *
     * @param remoteDir Directory on remote server from which files will be listed.
     * @throws SftpException If there is any problem with listing files related to permissions etc
     */
    public List<SftpFileInfo> listFiles(String remoteDir, String fileExtension, boolean recurse) throws SftpException {
        checkChannel();
        List<SftpFileInfo> remoteFiles = new ArrayList<>();
        channel.cd(remoteDir);
        Vector<ChannelSftp.LsEntry> files = channel.ls(".");
        for (ChannelSftp.LsEntry file : files) {
            if (file.getFilename().equals(".") || file.getFilename().equals("..")) continue;

            if (file.getAttrs().isDir()) {
                if (recurse) {
                    remoteFiles.addAll(listFiles(remoteDir + file.getFilename() + "/", fileExtension, true));
                }
            } else if (FilenameUtils.getExtension(file.getFilename()).endsWith(fileExtension)) {
                remoteFiles.add(new SftpFileInfo(remoteDir, file.getFilename(), file.getAttrs().getMTime()));
            }

        }

        return remoteFiles;
    }

    /**
     * Download a file from remote.
     *
     * @param remotePath full path of the remote file.
     * @param localPath  local full path where to save file.
     * @throws SftpException If there is any problem with downloading file related permissions etc
     */
    public void downloadFile(String remotePath, String localPath) throws SftpException {
        checkChannel();
        channel.get(remotePath, localPath);
    }

    /**
     * Retrieve a file from remote and return the text value.
     *
     * @param remotePath full path of remote file.
     * @throws SftpException If there is any problem with downloading file related permissions etc
     */
    public String getFileAsText(String remotePath) throws SftpException, IOException {
        checkChannel();

        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            channel.get(remotePath, byteArrayOutputStream);

            return removeUTF8BOM(byteArrayOutputStream.toString(StandardCharsets.UTF_8.toString()));
        }
    }

    /**
     * Disconnect from remote
     */
    public void close() {
        if (channel != null) {
            channel.exit();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    private void checkChannel() {
        if (channel == null) {
            throw new IllegalArgumentException("Connection is not available");
        }
    }

    private String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }
}
