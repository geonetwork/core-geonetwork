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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class KeyPairGeneratorUtil {
    private KeyPairGeneratorUtil() {
        throw new UnsupportedOperationException();
    }

    public static KeyPair generatePrivatePublicKeys(int type) throws JSchException {
        JSch jsch = new JSch();

        if (type == KeyPair.ECDSA) {
            return KeyPair.genKeyPair(jsch, KeyPair.ECDSA, 256);
        } else {
            return KeyPair.genKeyPair(jsch, KeyPair.RSA, 4096);
        }

    }

    public static void savePrivateKey(KeyPair keyPair, Path filePath) throws IOException {
        try (FileOutputStream fout = new FileOutputStream(filePath.toFile())) {
            keyPair.writePrivateKey(fout);
        }
    }

    public static void savePublicKey(KeyPair keyPair, Path filePath, String comment) throws IOException {
        try (FileOutputStream fout = new FileOutputStream(filePath.toFile())) {
            keyPair.writePublicKey(fout, comment);
        }
    }
}
