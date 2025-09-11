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

import org.fao.geonet.Util;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.jdom.Element;

public class SftpParams extends AbstractParams {
    public String server;
    public String port;
    public String folder;
    public String icon;
    public boolean useAuthKey;
    public String publicKey;
    public String typeAuthKey;

    /**
     * If true recurse into directories.
     */
    public boolean recurse;

    /**
     * The filter is a process (see schema/process folder) which depends on the schema. It could be
     * composed of parameter which will be sent to XSL transformation using the following syntax :
     * <pre>
     * anonymizer?protocol=MYLOCALNETWORK:FILEPATH&email=gis@organisation.org&thesaurus=MYORGONLYTHEASURUS
     * </pre>
     */
    public String xslfilter;

    public SftpParams(DataManager dm) {
        super(dm);
    }

    @Override
    public String getIcon() {
        return icon;
    }

    /**
     * called when a new entry must be added. Reads values from the provided entry, providing
     * default values.
     */
    @Override
    public void create(Element node) throws BadInputEx {
        super.create(node);

        Element site = node.getChild("site");
        Element opt = node.getChild("options");

        server = Util.getParam(site, "server", "");
        port = Util.getParam(site, "port", "22");
        xslfilter = Util.getParam(site, "xslfilter", "");
        icon = Util.getParam(site, "icon", "default.gif");

        folder = Util.getParam(opt, "folder", "");
        recurse = Util.getParam(opt, "recurse", false);
        useAuthKey = Util.getParam(opt, "useAuthKey", true);
        publicKey = Util.getParam(opt, "publicKey", "");
        typeAuthKey = Util.getParam(opt, "typeAuthKey", "RSA");
    }

    @Override
    public void update(Element node) throws BadInputEx {
        super.update(node);

        Element site = node.getChild("site");
        Element opt = node.getChild("options");

        server = Util.getParam(site, "server", server);
        port = Util.getParam(site, "port", port);
        icon = Util.getParam(site, "icon", icon);
        xslfilter = Util.getParam(site, "xslfilter", "");

        folder = Util.getParam(opt, "folder", folder);
        recurse = Util.getParam(opt, "recurse", recurse);
        useAuthKey = Util.getParam(opt, "useAuthKey", useAuthKey);
        publicKey = Util.getParam(opt, "publicKey", publicKey);
        typeAuthKey = Util.getParam(opt, "typeAuthKey", typeAuthKey);
    }

    @Override
    public AbstractParams copy() {
        SftpParams copy = new SftpParams(dm);
        copyTo(copy);

        copy.server = server;
        copy.port = port;
        copy.folder = folder;
        copy.recurse = recurse;
        copy.useAuthKey = useAuthKey;
        copy.publicKey = publicKey;
        copy.typeAuthKey = typeAuthKey;
        copy.icon = icon;
        copy.xslfilter = xslfilter;

        copy.setValidate(getValidate());

        return copy;
    }
}
