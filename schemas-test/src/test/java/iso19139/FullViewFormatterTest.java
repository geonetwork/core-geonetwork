package iso19139;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

/**
 * @author Jesse on 10/17/2014.
 */
public class FullViewFormatterTest extends AbstractFullViewFormatterTest {

    @Test  @DirtiesContext
    public void testServiceMdFormatting() throws Exception {
        super.testPrintFormat();
    }

    protected List<String> excludes() {
        return Lists.newArrayList(
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
