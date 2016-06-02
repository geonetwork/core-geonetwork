/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package iso19139;

import com.google.common.collect.Lists;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

/**
 * @author Jesse on 10/17/2014.
 */
public class FullViewFormatterTest extends AbstractFullViewFormatterTest {

    @Test
    public void testDummy() {
    }


    @Test
    @Ignore
    public void testServiceMdFormatting() throws Exception {
        super.testPrintFormat();
    }

    protected List<String> excludes() {
        return Lists.newArrayList(
            "> gmd:MD_Metadata > gmd:contentInfo > gmd:MD_CoverageDescription > gmd:dimension > gmd:MD_Band > gmd:sequenceIdentifier > gco:MemberName > gco:attributeType > gco:TypeName > gco:aName > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:contentInfo > gmd:MD_CoverageDescription > gmd:dimension > gmd:MD_Band > gmd:descriptor > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:dataQualityInfo > gmd:DQ_DataQuality > gmd:report > gmd:DQ_TemporalValidity > gmd:evaluationMethodDescription > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:dataQualityInfo > gmd:DQ_DataQuality > gmd:report > gmd:DQ_TemporalValidity > gmd:evaluationProcedure > gmd:CI_Citation > gmd:title > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:dataQualityInfo > gmd:DQ_DataQuality > gmd:lineage > gmd:LI_Lineage > gmd:processStep > gmd:LI_ProcessStep > gmd:processor > gmd:CI_ResponsibleParty > gmd:organisationName > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:dataQualityInfo > gmd:DQ_DataQuality > gmd:report > gmd:DQ_NonQuantitativeAttributeAccuracy > gmd:nameOfMeasure > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:metadataConstraints > gmd:MD_LegalConstraints > gmd:otherConstraints > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:applicationSchemaInfo > gmd:MD_ApplicationSchemaInformation > gmd:name > gmd:CI_Citation > gmd:title > gco:PT_FreeText > gco:textGroup > gmd:LocalisedCharacterString > Text",
            "> gmd:MD_Metadata > gmd:applicationSchemaInfo > gmd:MD_ApplicationSchemaInformation > gmd:constraintLanguage > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:applicationSchemaInfo > gmd:MD_ApplicationSchemaInformation > gmd:schemaAscii > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:applicationSchemaInfo > gmd:MD_ApplicationSchemaInformation > gmd:softwareDevelopmentFileFormat > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:metadataMaintenance > gmd:MD_MaintenanceInformation > gmd:maintenanceNote > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:title > gco:PT_FreeText > gco:textGroup > gmd:LocalisedCharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:date > gmd:CI_Date > gmd:date > gco:DateTime > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:identifier > gmd:RS_Identifier > gmd:authority > gmd:CI_Citation > gmd:title > gco:PT_FreeText > gco:textGroup > gmd:LocalisedCharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:identifier > gmd:RS_Identifier > gmd:authority > gmd:CI_Citation > gmd:date > gmd:CI_Date > gmd:date > gco:DateTime > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:identifier > gmd:RS_Identifier > gmd:authority > gmd:CI_Citation > gmd:series > gmd:CI_Series > gmd:name > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:identifier > gmd:RS_Identifier > gmd:authority > gmd:CI_Citation > gmd:series > gmd:CI_Series > gmd:issueIdentification > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:identifier > gmd:RS_Identifier > gmd:authority > gmd:CI_Citation > gmd:series > gmd:CI_Series > gmd:page > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:identifier > gmd:RS_Identifier > gmd:authority > gmd:CI_Citation > gmd:collectiveTitle > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:identifier > gmd:RS_Identifier > gmd:authority > gmd:CI_Citation > gmd:ISBN > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:identifier > gmd:RS_Identifier > gmd:authority > gmd:CI_Citation > gmd:ISSN > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:identifier > gmd:RS_Identifier > gmd:version > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:graphicOverview > gmd:MD_BrowseGraphic > gmd:fileType > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:resourceFormat > gmd:MD_Format > gmd:formatDistributor > gmd:MD_Distributor > gmd:distributionOrderProcess > gmd:MD_StandardOrderProcess > gmd:orderingInstructions > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:resourceFormat > gmd:MD_Format > gmd:formatDistributor > gmd:MD_Distributor > gmd:distributionOrderProcess > gmd:MD_StandardOrderProcess > gmd:turnaround > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:resourceFormat > gmd:MD_Format > gmd:formatDistributor > gmd:MD_Distributor > gmd:distributorTransferOptions > gmd:MD_DigitalTransferOptions > gmd:unitsOfDistribution > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:resourceFormat > gmd:MD_Format > gmd:formatDistributor > gmd:MD_Distributor > gmd:distributorTransferOptions > gmd:MD_DigitalTransferOptions > gmd:onLine > gmd:CI_OnlineResource > gmd:protocol > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:resourceFormat > gmd:MD_Format > gmd:formatDistributor > gmd:MD_Distributor > gmd:distributorTransferOptions > gmd:MD_DigitalTransferOptions > gmd:onLine > gmd:CI_OnlineResource > gmd:applicationProfile > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:resourceFormat > gmd:MD_Format > gmd:formatDistributor > gmd:MD_Distributor > gmd:distributorTransferOptions > gmd:MD_DigitalTransferOptions > gmd:onLine > gmd:CI_OnlineResource > gmd:description > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:resourceFormat > gmd:MD_Format > gmd:formatDistributor > gmd:MD_Distributor > gmd:distributorTransferOptions > gmd:MD_DigitalTransferOptions > gmd:offLine > gmd:MD_Medium > gmd:densityUnits > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:resourceFormat > gmd:MD_Format > gmd:formatDistributor > gmd:MD_Distributor > gmd:distributorTransferOptions > gmd:MD_DigitalTransferOptions > gmd:offLine > gmd:MD_Medium > gmd:mediumNote > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:descriptiveKeywords > gmd:MD_Keywords > gmd:thesaurusName > gmd:CI_Citation > gmd:alternateTitle > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:descriptiveKeywords > gmd:MD_Keywords > gmd:thesaurusName > gmd:CI_Citation > gmd:date > gmd:CI_Date > gmd:date > gco:DateTime > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:topicCategory > gmd:MD_TopicCategoryCode > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:extent > gmd:EX_Extent > gmd:temporalElement > gmd:EX_TemporalExtent > gmd:extent > gml:TimePeriod > gml:beginPosition > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:extent > gmd:EX_Extent > gmd:temporalElement > gmd:EX_TemporalExtent > gmd:extent > gml:TimePeriod > gml:endPosition > Text",
            "> gmd:MD_Metadata > gmd:distributionInfo > gmd:MD_Distribution > gmd:transferOptions > gmd:MD_DigitalTransferOptions > gmd:onLine > gmd:CI_OnlineResource > gmd:protocol > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:distributionInfo > gmd:MD_Distribution > gmd:transferOptions > gmd:MD_DigitalTransferOptions > gmd:onLine > gmd:CI_OnlineResource > gmd:applicationProfile > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:distributionInfo > gmd:MD_Distribution > gmd:transferOptions > gmd:MD_DigitalTransferOptions > gmd:onLine > gmd:CI_OnlineResource > gmd:linkage > gmd:URL > Text",
            "> gmd:MD_Metadata > gmd:distributionInfo > gmd:MD_Distribution > gmd:transferOptions > gmd:MD_DigitalTransferOptions > gmd:onLine > gmd:CI_OnlineResource > gmd:protocol > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:locale > gmd:PT_Locale > gmd:country > gmd:Country> @codeListValue",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:date > gmd:CI_Date > gmd:dateType > gmd:CI_DateTypeCode> @codeListValue",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:identifier > gmd:RS_Identifier > gmd:authority > gmd:CI_Citation > gmd:date > gmd:CI_Date > gmd:dateType > gmd:CI_DateTypeCode> @codeListValue",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:identifier > gmd:RS_Identifier > gmd:authority > gmd:CI_Citation > gmd:presentationForm > gmd:CI_PresentationFormCode> @codeListValue",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:resourceFormat > gmd:MD_Format > gmd:formatDistributor > gmd:MD_Distributor > gmd:distributorTransferOptions > gmd:MD_DigitalTransferOptions > gmd:onLine > gmd:CI_OnlineResource > gmd:function > gmd:CI_OnLineFunctionCode> @codeListValue",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:resourceFormat > gmd:MD_Format > gmd:formatDistributor > gmd:MD_Distributor > gmd:distributorTransferOptions > gmd:MD_DigitalTransferOptions > gmd:offLine > gmd:MD_Medium > gmd:name > gmd:MD_MediumNameCode> @codeListValue",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:resourceFormat > gmd:MD_Format > gmd:formatDistributor > gmd:MD_Distributor > gmd:distributorTransferOptions > gmd:MD_DigitalTransferOptions > gmd:offLine > gmd:MD_Medium > gmd:mediumFormat > gmd:MD_MediumFormatCode> @codeListValue",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:descriptiveKeywords > gmd:MD_Keywords > gmd:type > gmd:MD_KeywordTypeCode> @codeListValue",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:descriptiveKeywords > gmd:MD_Keywords > gmd:thesaurusName > gmd:CI_Citation > gmd:date > gmd:CI_Date > gmd:dateType > gmd:CI_DateTypeCode> @codeListValue",
            "> gmd:MD_Metadata > gmd:portrayalCatalogueInfo > gmd:MD_PortrayalCatalogueReference > gmd:portrayalCatalogueCitation > gmd:CI_Citation > gmd:date > gmd:CI_Date > gmd:dateType > gmd:CI_DateTypeCode> @codeListValue",
            "> gmd:MD_Metadata > gmd:applicationSchemaInfo > gmd:MD_ApplicationSchemaInformation > gmd:name > gmd:CI_Citation > gmd:date > gmd:CI_Date > gmd:dateType > gmd:CI_DateTypeCode> @codeListValue",
            "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:identifier > gmd:RS_Identifier > gmd:authority > gmd:CI_Citation > gmd:alternateTitle > gco:CharacterString > Text"
        );
    }
}
