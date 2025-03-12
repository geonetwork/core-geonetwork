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
package org.fao.geonet.kernel.harvest.harvester.sftp;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.Logger;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.sftp.client.KeyPairGeneratorUtil;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.resources.Resources;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class SftpHarvester extends AbstractHarvester<HarvestResult, SftpParams> {
    @Override
    protected SftpParams createParams() {
        return new SftpParams(dataMan);
    }

    //---------------------------------------------------------------------------
    @Override
    protected void storeNodeExtra(SftpParams params, String path, String siteId, String optionsId) throws SQLException {
        harvesterSettingsManager.add("id:" + siteId, "server", params.server);
        harvesterSettingsManager.add("id:" + siteId, "port", params.port);
        harvesterSettingsManager.add("id:" + siteId, "icon", params.icon);
        harvesterSettingsManager.add("id:" + optionsId, "folder", params.folder);
        harvesterSettingsManager.add("id:" + optionsId, "recurse", params.recurse);
        harvesterSettingsManager.add("id:" + optionsId, "useAuthKey", params.useAuthKey);
        harvesterSettingsManager.add("id:" + optionsId, "validate", params.getValidate());
        harvesterSettingsManager.add("id:" + siteId, "xslfilter", params.xslfilter);

        if (params.useAuthKey && (!StringUtils.hasLength(params.publicKey) || !getParams().typeAuthKey.equals(params.typeAuthKey))) {
                try {
                    int keyType = "ECDSA".equals(params.typeAuthKey) ? KeyPair.ECDSA : KeyPair.RSA;
                    KeyPair keyPair = KeyPairGeneratorUtil.generatePrivatePublicKeys(keyType);

                    String serverHost = settingManager.getValue(Settings.SYSTEM_SERVER_HOST);
                    KeyPairGeneratorUtil.savePrivateKey(keyPair, SftpHarvesterUtil.getPrivateKeyFilePath(context, params.getUuid()));
                    KeyPairGeneratorUtil.savePublicKey(keyPair, SftpHarvesterUtil.getPublicKeyFilePath(context, params.getUuid()), serverHost);

                    params.publicKey = FileUtils.readFileToString(SftpHarvesterUtil.getPublicKeyFilePath(context, params.getUuid()).toFile(), StandardCharsets.UTF_8);
                } catch (IOException | JSchException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }

        harvesterSettingsManager.add("id:" + optionsId, "publicKey", params.publicKey);
        harvesterSettingsManager.add("id:" + optionsId, "typeAuthKey", params.typeAuthKey);
    }

    public void doHarvest(Logger log) throws Exception {
        log.info("Sftp doHarvest start");
        org.fao.geonet.kernel.harvest.harvester.sftp.Harvester h = new org.fao.geonet.kernel.harvest.harvester.sftp.Harvester(cancelMonitor, log, context, params, errors);
        result = h.harvest(log);
        log.info("Sftp doHarvest end");
    }

    @Override
    protected void doDestroy(final Resources resources) {
        super.doDestroy(resources);

        // Cleanup SFTP harvester private/public key files
        FileUtils.deleteQuietly(SftpHarvesterUtil.getPrivateKeyFilePath(context, params.getUuid()).toFile());
        FileUtils.deleteQuietly(SftpHarvesterUtil.getPublicKeyFilePath(context, params.getUuid()).toFile());
    }



}
