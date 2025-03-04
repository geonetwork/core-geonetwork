/*
 * =============================================================================
 * ===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
 * ===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * ===	and United Nations Environment Programme (UNEP)
 * ===
 * ===	This program is free software; you can redistribute it and/or modify
 * ===	it under the terms of the GNU General Public License as published by
 * ===	the Free Software Foundation; either version 2 of the License, or (at
 * ===	your option) any later version.
 * ===
 * ===	This program is distributed in the hope that it will be useful, but
 * ===	WITHOUT ANY WARRANTY; without even the implied warranty of
 * ===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * ===	General Public License for more details.
 * ===
 * ===	You should have received a copy of the GNU General Public License
 * ===	along with this program; if not, write to the Free Software
 * ===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * ===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * ===	Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */

package org.fao.geonet.api.records.attachments;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;

public class StoreFolderConfig {
    public enum FolderStructureType {
        DEFAULT, CUSTOM
    }

    public enum FolderPrivilegesStrategy {
        DEFAULT, NONE
    }

    @Value("${datastore.folderStructureType}")
    private FolderStructureType folderStructureType = FolderStructureType.DEFAULT;

    @Value("${datastore.folderPrivilegesStrategy}")
    private FolderPrivilegesStrategy folderPrivilegesStrategy = FolderPrivilegesStrategy.DEFAULT;

    @Value("${datastore.folderStructure}")
    private String folderStructure;

    @Value("${datastore.folderStructureFallback}")
    private String folderStructureFallback;

    @Value("${datastore.folderStructureNonPublic}")
    private String folderStructureNonPublic = "";

    @Value("${datastore.folderStructureFallbackNonPublic}")
    private String folderStructureFallbackNonPublic = "";

    public StoreFolderConfig() {

    }

    public FolderStructureType getFolderStructureType() {
        return folderStructureType;
    }

    public FolderPrivilegesStrategy getFolderPrivilegesStrategy() {
        return folderPrivilegesStrategy;
    }

    public String getFolderStructure() {
        return folderStructure;
    }

    public String getFolderStructureFallback() {
        return folderStructureFallback;
    }

    public String getFolderStructureNonPublic() {
        return StringUtils.isNotBlank(folderStructureNonPublic) ? folderStructureNonPublic : getFolderStructure();
    }

    public String getFolderStructureFallbackNonPublic() {
        return StringUtils.isNotBlank(folderStructureFallbackNonPublic) ? folderStructureFallbackNonPublic : getFolderStructureFallback();
    }

    public boolean hasCustomFolderStructureNonPublic() {
        return getFolderStructureNonPublic() != getFolderStructure();
    }

    public boolean hasFolderStructureFallbackNonPublic() {
        return getFolderStructureFallbackNonPublic() != getFolderStructureFallback();
    }
}
