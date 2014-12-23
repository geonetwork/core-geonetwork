package iso19139;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;
import java.util.List;

/**
 * @author Jesse on 10/17/2014.
 */
public class FullViewFormatterServiceTest extends AbstractFullViewFormatterTest {

    @Test @DirtiesContext
    public void testServiceMdFormatting() throws Exception {
        super.testPrintFormat();
    }

    protected List<String> excludes() {
        return Lists.newArrayList(
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > gmd:citation > gmd:CI_Citation > gmd:edition > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > gmd:graphicOverview > gmd:MD_BrowseGraphic > gmd:fileDescription > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > gmd:graphicOverview > gmd:MD_BrowseGraphic > gmd:fileType > gco:CharacterString > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > gmd:descriptiveKeywords > gmd:MD_Keywords > gmd:thesaurusName > gmd:CI_Citation > gmd:date > gmd:CI_Date > gmd:date > gco:Date > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > gmd:descriptiveKeywords > gmd:MD_Keywords > gmd:thesaurusName > gmd:CI_Citation > gmd:identifier > gmd:MD_Identifier > gmd:code > gmx:Anchor > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > gmd:aggregationInfo > gmd:MD_AggregateInformation > gmd:aggregateDataSetIdentifier > gmd:RS_Identifier > gmd:code > gmx:FileName > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > srv:serviceType > gco:LocalName > Text",
            "> gmd:MD_Metadata > gmd:identificationInfo > srv:SV_ServiceIdentification > gmd:descriptiveKeywords > gmd:MD_Keywords > gmd:type > gmd:MD_KeywordTypeCode> @codeListValue"
         );
    }

    @Override
    protected File getTestMetadataFile() {
        final String mdFile = FullViewFormatterServiceTest.class.getResource("/iso19139/example-service.xml").getFile();
        return new File(mdFile);
    }
}
