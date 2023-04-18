//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.datamanager;

import java.util.List;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.Pair;
import org.jdom.Element;
import org.jdom.Namespace;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

/**
 * Interface to handle all operations related to validations of records
 *
 * @author delawen
 *
 */
public interface IMetadataValidator {
    /**
     * Validates metadata against XSD and schematron files related to metadata schema throwing XSDValidationErrorEx if xsd errors or
     * SchematronValidationErrorEx if schematron rules fails.
     *
     * Method used when importing/harvesting metadata.
     */
    void validateExternalMetadata(String schema, Element xml, ServiceContext context, String fileName, Integer groupOwner) throws Exception;

    /**
     *
     * if the metadata has no namespace or already has a namespace then we must skip this phase
     *
     * @param md
     */
    void setNamespacePrefix(Element md);

    /**
     * Use this validate method for XML documents with xsd validation.
     */
    void validate(String schema, Element md) throws Exception;

    /**
     * Creates XML schematron report.
     */
    Element doSchemaTronForEditor(String schema, Element md, String lang, Integer groupOwner) throws Exception;

    /**
     * Used by harvesters that need to validate metadata.
     *
     * @param metadata metadata
     * @param lang     Language from context
     */
    Pair<Element, Boolean> doValidate(AbstractMetadata metadata, String lang);

    /**
     * Used by the validate embedded service. The validation report is stored in the session.
     *
     */
    Pair<Element, String> doValidate(UserSession session, String schema, String metadataId, Element md, String lang, boolean forEditing)
            throws Exception;

    /**
     * Creates XML schematron report for each set of rules defined in schema directory. This method assumes that you've run enumerateTree on
     * the metadata
     *
     * Returns null if no error on validation.
     */
    Element applyCustomSchematronRules(String schema, int metadataId, Element md, String lang, List<MetadataValidation> validations);

    /**
     * Validates an xml document, using autodetectschema to determine how.
     *
     * @return true if metadata is valid
     */
    boolean validate(Element xml);

    /**
     * Adds the namespace to the element
     *
     * @param md
     * @param ns
     */
    void setNamespacePrefix(Element md, Namespace ns);

    /**
     * Helper function to prevent loop on dependencies
     *
     * @param metadataManager
     */
    void setMetadataManager(IMetadataManager metadataManager);
}
